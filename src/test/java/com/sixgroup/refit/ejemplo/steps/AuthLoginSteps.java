package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import com.sixgroup.refit.ejemplo.model.Role;
import com.sixgroup.refit.ejemplo.model.User;
import com.sixgroup.refit.ejemplo.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

import static org.hamcrest.Matchers.*;

public class AuthLoginSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // =====================================================
    // GIVEN: PREPARACIÓN Y LOGIN
    // =====================================================
    @Given("reinicio el entorno y me autentico con usuario {string} y password {string} y rol {string}")
    public void prepararYAutenticar(String email, String password, String roleStr) {
        configureRestAssured();

        // 1. Limpieza de Redis (para resetear contadores de fuerza bruta)
        if (redisTemplate != null) {
            try {
                Objects.requireNonNull(redisTemplate.getConnectionFactory())
                        .getConnection()
                        .serverCommands()
                        .flushAll();
            } catch (Exception ignored) {}
        }

        // 2. Preparar el usuario directamente en BD para evitar el 423 Locked
        Role role = Role.valueOf(roleStr.toUpperCase()); // Aseguramos mayúsculas para el Enum
        userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    user.setPassword(passwordEncoder.encode(password));
                    user.setRole(role);
                    user.setAccountNonLocked(true); // 🔓 Desbloqueo manual
                    user.setFailedAttempt(0);
                    user.setLockCount(0);
                    user.setLockTime(null);
                    userRepository.save(user);
                },
                () -> {
                    User newUser = User.builder()
                            .email(email)
                            .password(passwordEncoder.encode(password))
                            .role(role)
                            .accountNonLocked(true)
                            .failedAttempt(0)
                            .build();
                    userRepository.save(newUser);
                }
        );

        // 3. Login contra tu controlador
        SerenityRest.given()
                .contentType("application/json")
                .body("{ \"email\": \"%s\", \"password\": \"%s\" }".formatted(email, password))
                .post("/auth/login");

        // 4. Guardar token si es exitoso
        if (SerenityRest.lastResponse().statusCode() == 200) {
            testContext.setAccessToken(SerenityRest.lastResponse().jsonPath().getString("access_token"));
            testContext.setRefreshToken(SerenityRest.lastResponse().jsonPath().getString("refresh_token"));
        }
    }

    // =====================================================
    // WHEN: RESET PASSWORD (Según Swagger POST /auth/reset-password)
    // =====================================================
    @When("solicito restablecer la contraseña del email {string} a la nueva clave {string}")
    public void solicitarReset(String email, String nuevaPass) {
        configureRestAssured();

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .contentType("application/json")
                .body("""
                    {
                      "email": "%s",
                      "newPassword": "%s"
                    }
                    """.formatted(email, nuevaPass))
                .post("/auth/reset-password");
    }

    @And("la respuesta contiene el mensaje {string}")
    public void verificarMensajeJSON(String mensajeEsperado) {
        // Validamos la clave "mensaje" del JSON devuelto
        SerenityRest.then()
                .body("mensaje", equalTo(mensajeEsperado));
    }
}