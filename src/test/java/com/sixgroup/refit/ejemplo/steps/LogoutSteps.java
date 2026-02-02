package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import io.cucumber.java.en.Then;
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
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .post("/auth/logout");
    }

    @When("intento acceder a un endpoint protegido")
    public void intento_acceder_a_un_endpoint_protegido() {

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get("/api/v1/admin/users"); // cualquier endpoint protegido
    }
}
