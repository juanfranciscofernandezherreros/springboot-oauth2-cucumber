package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@ActiveProfiles("test")
public class AuthSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // =========================================================
// SETUP LIMPIEntornoO + REGISTRO (SIN LOGIN)
// =========================================================
    @Given("reinicio el entorno y me registro con usuario {string} y password {string} y rol {string}")
    public void setup_y_registro_con_rol(String email, String password, String role) {

        configureRestAssured();

        // 1. Limpiar Redis
        if (redisTemplate != null) {
            try {
                Objects.requireNonNull(redisTemplate.getConnectionFactory())
                        .getConnection()
                        .serverCommands()
                        .flushAll();
            } catch (Exception ignored) {}
        }

        // 2. Registro
        SerenityRest.given()
                .contentType("application/json")
                .body("""
                {
                  "name": "Auto Test User",
                  "email": "%s",
                  "password": "%s",
                  "role": "%s"
                }
                """.formatted(email, password, role))
                .post("/auth/register")
                .then()
                .statusCode(201);
    }


    // =========================================================
    // SETUP LIMPIentornoO + REGISTRO + LOGIN (CON ROL)
    // =========================================================
    @Given("reinicio el entorno y me autentico con usuario {string} y password {string} y rol {string}")
    public void setup_y_login_con_rol(String email, String password, String role) {

        configureRestAssured();

        // 1. Limpiar Redis
        if (redisTemplate != null) {
            try {
                Objects.requireNonNull(redisTemplate.getConnectionFactory())
                        .getConnection()
                        .serverCommands()
                        .flushAll();
            } catch (Exception ignored) {}
        }

        // 2. Registro
        SerenityRest.given()
                .contentType("application/json")
                .body("""
                    {
                      "name": "Auto Test User",
                      "email": "%s",
                      "password": "%s",
                      "role": "%s"
                    }
                    """.formatted(email, password, role))
                .post("/auth/register")
                .then()
                .statusCode(201);

        // 3. Login
        SerenityRest.given()
                .contentType("application/json")
                .body("""
                    {
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(email, password))
                .post("/auth/login")
                .then()
                .statusCode(200);

        // 4. Guardar tokens (snake_case)
        String accessToken  = SerenityRest.lastResponse().jsonPath().getString("access_token");
        String refreshToken = SerenityRest.lastResponse().jsonPath().getString("refresh_token");

        testContext.setAccessToken(accessToken);
        testContext.setRefreshToken(refreshToken);
    }

    // =========================================================
    // VALIDACIÓN DE TOKENS
    // =========================================================
    @Then("la respuesta contiene un access token y un refresh token")
    public void verificar_tokens_en_contexto() {

        assertThat("El access token no debe ser nulo",
                testContext.getAccessToken(), notNullValue());

        assertThat("El refresh token no debe ser nulo",
                testContext.getRefreshToken(), notNullValue());
    }

    // =========================================================
    // PERFIL
    // =========================================================
    @When("el usuario accede a su perfil usando el access token")
    public void accede_perfil_con_token() {

        SerenityRest.given()
                .auth()
                .oauth2(testContext.getAccessToken())
                .get("/api/v1/user/me");
    }

    @When("el usuario intenta acceder a su perfil sin token")
    public void accede_perfil_sin_token() {
        SerenityRest.given().get("/api/v1/user/me");
    }

    @Then("la respuesta contiene los datos del usuario autenticado")
    public void perfil_devuelto_correctamente() {

        SerenityRest.then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("email", notNullValue())
                .body("role", notNullValue());
    }

    // =========================================================
// REGISTRO (WHEN)
// =========================================================
    @When("registro un usuario con email {string} y password {string} y rol {string}")
    public void registro_un_usuario(String email, String password, String role) {

        SerenityRest.given()
                .contentType("application/json")
                .body("""
                {
                  "name": "Auto Test User",
                  "email": "%s",
                  "password": "%s",
                  "role": "%s"
                }
                """.formatted(email, password, role))
                .post("/auth/register");
    }

    // =========================================================
// SETUP: REINICIO DEL ENTORNO
// =========================================================
    @Given("reinicio el entorno")
    public void reinicio_el_entorno() {

        configureRestAssured();

        // Limpiar Redis
        if (redisTemplate != null) {
            try {
                Objects.requireNonNull(redisTemplate.getConnectionFactory())
                        .getConnection()
                        .serverCommands()
                        .flushAll();
            } catch (Exception ignored) {}
        }
    }

    // =========================================================
    // GENÉRICO
    // =========================================================
    @Then("el sistema responde con código {int}")
    public void recibe_codigo(int codigo) {
        SerenityRest.then().statusCode(codigo);
    }
}
