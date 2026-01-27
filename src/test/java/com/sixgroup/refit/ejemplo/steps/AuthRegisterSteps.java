package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.Objects;

public class AuthRegisterSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // =========================================================
    // SETUP: RESET DEL ENTORNO
    // =========================================================
    @Given("reinicio el entorno")
    public void reinicio_el_entorno() {

        configureRestAssured();

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
    // SETUP + REGISTRO CORRECTO (USER)
    // =========================================================
    @Given("reinicio el entorno y me registro con usuario {string} y password {string}")
    public void setup_y_registro_usuario(String email, String password) {

        reinicio_el_entorno();

        SerenityRest.given()
                .contentType("application/json")
                .body("""
                    {
                      "name": "Auto Test User",
                      "email": "%s",
                      "password": "%s",
                      "role": "USER"
                    }
                    """.formatted(email, password))
                .post("/auth/register")
                .then()
                .statusCode(201);
    }

    // =========================================================
    // REGISTRO (WHEN) CON ROL EXPLÍCITO
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

}
