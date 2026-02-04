package com.fernandez.backend.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class AuthTokenSteps {

    @Autowired
    private TestContexts testContext;

    @And("la respuesta contiene un access token y un refresh token")
    public void validar_tokens() {
        assertThat(testContext.getAccessToken(), notNullValue());
        assertThat(testContext.getRefreshToken(), notNullValue());
    }

    @When("el usuario accede a su perfil usando el access token")
    public void elUsuarioAccedeASuPerfilUsandoElAccessToken() {
        // Write code here that turns the phrase above into concrete actions
        assertThat(testContext.getAccessToken(), notNullValue());
    }
}
