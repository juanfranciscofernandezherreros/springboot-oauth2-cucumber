package com.fernandez.backend.steps;

import io.cucumber.java.en.Then;
import net.serenitybdd.rest.SerenityRest;

public class CommonResponseSteps {

    @Then("el sistema responde con código {int}")
    public void el_sistema_responde_con_codigo(int statusCode) {
        SerenityRest.then().statusCode(statusCode);
    }
}
