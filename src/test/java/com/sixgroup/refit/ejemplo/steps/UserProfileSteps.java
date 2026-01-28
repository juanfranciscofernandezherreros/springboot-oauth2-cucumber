package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import com.sixgroup.refit.ejemplo.repository.TokenRepository;
import com.sixgroup.refit.ejemplo.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.Matchers.*;

public class UserProfileSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @When("intento acceder a mi perfil")
    public void intento_acceder_a_mi_perfil() {
        configureRestAssured();

        SerenityRest.given()
                .auth()
                .oauth2(testContext.getAccessToken())
                .get("/api/v1/user/me");
    }

    @When("intento acceder a mi perfil en {string}")
    public void intento_acceder_a_mi_perfil_en(String endpoint) {
        configureRestAssured();

        SerenityRest.given()
                .auth()
                .oauth2(testContext.getAccessToken())
                .get(endpoint);
    }

    @When("intento acceder a mi perfil de nuevo")
    public void intento_acceder_a_mi_perfil_de_nuevo() {
        configureRestAssured();

        SerenityRest.given()
                .auth()
                .oauth2(testContext.getAccessToken()) // Se usa el token que fue revocado
                .get("/api/v1/user/me");
    }

    @Then("recibo mi perfil de usuario correctamente")
    public void recibo_mi_perfil_correctamente() {
        SerenityRest.then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("email", notNullValue())
                .body("name", notNullValue())
                .body("role", notNullValue());
    }

    // =========================================================
    // PERFIL - ACTUALIZACIÓN
    // =========================================================

    @When("actualizo mi perfil con nombre {string}")
    public void actualizo_mi_perfil(String nuevoNombre) {
        configureRestAssured();

        // Construimos el JSON manualmente para no depender de DTOs externos en el test
        String body = """
            {
              "name": "%s"
            }
            """.formatted(nuevoNombre);

        SerenityRest.given()
                .auth()
                .oauth2(testContext.getAccessToken())
                .contentType("application/json")
                .body(body)
                .put("/api/v1/user/update");
    }

    @Then("el perfil actualizado tiene el nombre {string}")
    public void el_perfil_actualizado_tiene_el_nombre(String nombreEsperado) {
        configureRestAssured();

        // 1. Hacemos GET de nuevo para verificar persistencia
        SerenityRest.given()
                .auth()
                .oauth2(testContext.getAccessToken())
                .get("/api/v1/user/me");

        // 2. Validamos
        SerenityRest.then()
                .statusCode(200)
                .body("name", equalTo(nombreEsperado));
    }

    @And("puedo iniciar sesión exitosamente con usuario {string} y password {string}")
    public void puedo_iniciar_sesion_exitosamente(String email, String password) {
        configureRestAssured();

        // Construimos el JSON de login
        String loginBody = """
            {
              "email": "%s",
              "password": "%s"
            }
            """.formatted(email, password);

        // Hacemos la petición POST a /login y verificamos que sea 200 OK
        SerenityRest.given()
                .contentType("application/json")
                .body(loginBody)
                .post("/auth/login")
                .then()
                .statusCode(200); // Si la contraseña nueva no funciona, esto devolverá 401 o 403 y el test fallará
    }
}