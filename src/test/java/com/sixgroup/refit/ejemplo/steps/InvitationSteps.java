package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.*;

public class InvitationSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    private static final String INV_PATH = "/api/v1/admin/invitations";

    @When("creo una nueva invitación para el email {string}")
    public void crearInvitacion(String email) {
        configureRestAssured();
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .contentType("application/json")
                .body("""
                    {
                      "email": "%s",
                      "name": "Test User",
                      "description": "Invitación de prueba"
                    }
                    """.formatted(email))
                .post(INV_PATH);
    }

    @Then("la invitación se crea correctamente")
    public void verificarCreacion() {
        SerenityRest.then().statusCode(201)
                .body("status", equalTo("PENDING"));
    }

    @Then("el sistema rechaza la invitación por duplicada")
    public void verificarDuplicado() {
        SerenityRest.then().statusCode(409);
    }

    @When("el administrador consulta las invitaciones pendientes")
    public void consultarPendientes() {
        configureRestAssured();
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(INV_PATH + "/pending");
    }

    @When("el administrador acepta la invitación")
    public void aceptarInvitacion() {
        Long id = obtenerPrimerIdPendiente();
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .pathParam("id", id)
                .patch(INV_PATH + "/{id}/accept");
    }

    @When("el administrador rechaza la invitación")
    public void rechazarInvitacion() {
        Long id = obtenerPrimerIdPendiente();
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .pathParam("id", id)
                .patch(INV_PATH + "/{id}/deny");
    }

    @When("el administrador consulta el histórico de invitaciones")
    public void consultarHistorico() {
        configureRestAssured();
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(INV_PATH + "/history");
    }

    // --- Métodos Auxiliares ---

    private Long obtenerPrimerIdPendiente() {
        configureRestAssured();
        return SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(INV_PATH + "/pending")
                .jsonPath().getLong("[0].id");
    }

    @Given("existe una invitación pendiente")
    public void asegurarInvitacionPendiente() {
        // Podrías llamar al método de crear si la lista está vacía
        consultarPendientes();
        if (SerenityRest.lastResponse().jsonPath().getList("$").isEmpty()) {
            crearInvitacion("auto-generated@test.com");
        }
    }

    @Given("existen invitaciones aceptadas o expiradas")
    public void existen_invitaciones_aceptadas_o_expiradas() {
        configureRestAssured();
        // Verificamos si hay algo en el historial
        var response = SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(INV_PATH + "/history");

        // Si el historial está vacío, creamos una y la aceptamos para forzar que exista historial
        if (response.jsonPath().getList("$").isEmpty()) {
            asegurarInvitacionPendiente();
            aceptarInvitacion();
        }
    }

    @Then("se devuelve una lista de invitaciones pendientes")
    public void se_devuelve_una_lista_de_invitaciones_pendientes() {
        SerenityRest.then()
                .body("$", is(notNullValue()))
                .body("$", instanceOf(java.util.List.class))
                .body("status", everyItem(equalTo("PENDING")));
    }
}