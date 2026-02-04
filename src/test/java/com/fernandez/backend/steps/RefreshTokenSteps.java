package com.fernandez.backend.steps;

import com.fernandez.backend.config.BaseRestConfig;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.notNullValue;

public class RefreshTokenSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    @When("renuevo el access token con el refresh token")
    public void renuevo_el_access_token() {

        configureRestAssured();

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getRefreshToken())
                .post("/auth/refresh-token");
    }

    @When("intento renovar el token con un refresh token inválido")
    public void intento_renovar_token_invalido() {

        SerenityRest.given()
                .header("Authorization", "Bearer invalid-refresh-token")
                .post("/auth/refresh-token");
    }

    @Then("se devuelve un nuevo access token")
    public void se_devuelve_un_nuevo_access_token() {

        SerenityRest.then()
                .body("access_token", notNullValue());
    }
}
