package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;

import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;

import org.springframework.beans.factory.annotation.Autowired;

public class LogoutSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    @When("hago una petición de logout")
    public void hago_una_peticion_de_logout() {
        configureRestAssured();

        SerenityRest.given()
                .auth()
                .oauth2(testContext.getAccessToken())
                .post("/auth/logout");
    }

    @When("cierro la sesión")
    public void cierroLaSesion() {
        configureRestAssured();

        SerenityRest.given()
                .auth()
                .oauth2(testContext.getAccessToken())
                .post("/auth/logout");
    }
}
