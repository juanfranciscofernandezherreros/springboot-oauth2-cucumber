package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import com.sixgroup.refit.ejemplo.usuario.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.Matchers.*;

public class ResetPasswordSteps extends BaseRestConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestContexts testContext;

    @When("solicito restablecer la contraseña del email {string} a la nueva clave {string}")
    public void solicito_restablecer_contrasena(String email, String newPassword) {
        configureRestAssured();
        String body = """
            { "email": "%s", "newPassword": "%s" }
            """.formatted(email, newPassword);

        SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .post("/auth/reset-password");

        String accessToken  = SerenityRest.lastResponse().jsonPath().getString("access_token");
        String refreshToken = SerenityRest.lastResponse().jsonPath().getString("refresh_token");

        testContext.setAccessToken(accessToken);
        testContext.setRefreshToken(refreshToken);
    }

    @And("la respuesta contiene el mensaje {string}")
    public void la_respuesta_contiene_mensaje(String mensajeEsperado) {
        SerenityRest.then().body("mensaje", equalTo(mensajeEsperado));
    }

    @And("la respuesta contiene el error {string}")
    public void la_respuesta_contiene_error(String errorEsperado) {
        SerenityRest.then().body("error", containsString(errorEsperado));
    }

    // Paso específico para pruebas de reset password (caso negativo)
    @Given("la cuenta del usuario {string} se encuentra bloqueada por seguridad")
    public void forzar_bloqueo_cuenta(String email) {
        var user = userRepository.findByEmail(email).orElseThrow();
        user.setAccountNonLocked(false); // Bloqueo manual
        user.setLockCount(3);
        userRepository.save(user);
    }
}