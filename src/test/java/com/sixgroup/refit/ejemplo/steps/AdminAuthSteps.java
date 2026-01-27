package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import io.cucumber.java.en.Given;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.notNullValue;

public class AdminAuthSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    @Given("el administrador está autenticado")
    public void el_administrador_esta_autenticado() {

        configureRestAssured();

        // LOGIN DIRECTO (admin ya existe en profile test)
        SerenityRest.given()
                .contentType("application/json")
                .body("""
                    {
                      "email": "admin1@test.com",
                      "password": "Admin123!"
                    }
                    """)
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        // Guardar tokens
        String accessToken = SerenityRest.lastResponse()
                .jsonPath()
                .getString("access_token");

        String refreshToken = SerenityRest.lastResponse()
                .jsonPath()
                .getString("refresh_token");

        testContext.setAccessToken(accessToken);
        testContext.setRefreshToken(refreshToken);
    }
}

