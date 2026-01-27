package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.*;

public class AdminUserSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    @When("el administrador solicita el listado de usuarios")
    public void el_administrador_solicita_el_listado() {

        configureRestAssured();

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get("/api/v1/admin/users");
    }

    @When("el administrador solicita el listado de usuarios bloqueados")
    public void el_administrador_solicita_el_listado_de_usuarios_bloqueados() {

        configureRestAssured();

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get("/api/v1/admin/locked-users");
    }

    @Then("la respuesta tiene código {int}")
    public void la_respuesta_tiene_codigo(int status) {
        SerenityRest.then().statusCode(status);
    }


    @And("el total de usuarios devueltos es igual que {int}")
    public void el_total_de_usuarios_es_igual_que(int min) {
        SerenityRest.then()
                .body("size()", equalTo(min));
    }


    @And("el total de usuarios devueltos es mayor que {int}")
    public void el_total_de_usuarios_es_mayor_que(int min) {
        SerenityRest.then()
                .body("size()", greaterThan(min));
    }

    @And("se devuelve una lista de usuarios bloqueados")
    public void se_devuelve_una_lista_de_usuarios_bloqueados() {

        SerenityRest.then()
                .body("$", notNullValue())
                // Puede estar vacía, así que NO usamos not(empty())
                .body("$", is(instanceOf(List.class)));
    }
}
