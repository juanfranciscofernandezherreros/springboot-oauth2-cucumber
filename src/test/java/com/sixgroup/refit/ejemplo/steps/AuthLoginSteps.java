package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import io.cucumber.java.en.Given;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Objects;

import static org.hamcrest.Matchers.notNullValue;

public class AuthLoginSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Given("reinicio el entorno y me autentico con usuario {string} y password {string} y rol {string}")
    public void login_usuario_existente(String email, String password, String rolIgnorado) {

        configureRestAssured();

        // 🧹 Limpiar Redis (seguridad / intentos)
        if (redisTemplate != null) {
            try {
                Objects.requireNonNull(redisTemplate.getConnectionFactory())
                        .getConnection()
                        .serverCommands()
                        .flushAll();
            } catch (Exception ignored) {}
        }

        // 🔐 LOGIN (el usuario YA EXISTE)
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
                .statusCode(200)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        testContext.setAccessToken(
                SerenityRest.lastResponse().jsonPath().getString("access_token")
        );
        testContext.setRefreshToken(
                SerenityRest.lastResponse().jsonPath().getString("refresh_token")
        );
    }
}
