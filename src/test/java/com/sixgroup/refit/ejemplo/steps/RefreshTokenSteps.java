package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.Matchers.notNullValue;

public class RefreshTokenSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    @And("guardo el refresh token de la sesión actual")
    public void guardo_el_refresh_token_actual() {
        // Recuperamos de la última respuesta (que vino del Login en CommonAuthSteps)
        String refreshToken = SerenityRest.lastResponse().jsonPath().getString("refresh_token");
        testContext.setRefreshToken(refreshToken);
    }

    @When("solicito refrescar el token enviando un token inválido {string}")
    public void solicito_refrescar_token_invalido(String tokenFalso) {
        configureRestAssured();
        SerenityRest.given()
                .header("Authorization", "Bearer " + tokenFalso)
                .post("/auth/refresh-token");
    }

    @And("recibo un nuevo par de access token y refresh token")
    public void recibo_nuevo_par_tokens() {
        SerenityRest.then()
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        // Actualizamos el contexto para que otros pasos puedan usar el nuevo token
        String newAccessToken = SerenityRest.lastResponse().jsonPath().getString("access_token");
        String newRefreshToken = SerenityRest.lastResponse().jsonPath().getString("refresh_token");
        testContext.setAccessToken(newAccessToken);
        testContext.setRefreshToken(newRefreshToken);

    }
}