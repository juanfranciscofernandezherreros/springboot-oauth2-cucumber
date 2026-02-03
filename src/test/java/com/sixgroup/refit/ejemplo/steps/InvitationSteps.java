package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.*;

public class InvitationSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    private static final String INV_PATH = "/api/v1/admin/invitations";
    private Long currentInvitationId;

    // =====================================================
    // GIVEN
    // =====================================================

    @Given("existe una invitación con estado {string}")
    public void existeInvitacionConEstado(String estado) {
        configureRestAssured();
        String email = "flow-test-" + System.currentTimeMillis() + "@test.com";
        crearNuevaInvitacionEnServidor(email);

        if (estado.equals("ACCEPTED") || estado.equals("APPROVED")) {
            ejecutarCambioEstado(currentInvitationId, "ACCEPTED");
        }
        if (estado.equals("APPROVED")) {
            ejecutarCambioEstado(currentInvitationId, "APPROVED");
        }
        if (estado.equals("REJECTED")) {
            ejecutarCambioEstado(currentInvitationId, "REJECTED");
        }
        if (estado.equals("EXPIRED")) {
            ejecutarCambioEstado(currentInvitationId, "EXPIRED");
        }
    }

    @Given("existe una invitación pendiente")
    public void asegurarInvitacionPendiente() {
        configureRestAssured();
        crearNuevaInvitacionEnServidor("pending-" + System.currentTimeMillis() + "@test.com");
    }

    @Given("existen invitaciones aceptadas o expiradas")
    public void asegurarHistorial() {
        existeInvitacionConEstado("ACCEPTED");
    }

    // =====================================================
    // WHEN
    // =====================================================

    @When("creo una nueva invitación para el email {string}")
    public void crearInvitacionManual(String email) {
        crearNuevaInvitacionEnServidor(email);
    }

    @When("el administrador cambia el estado a {string}")
    @When("el administrador intenta cambiar el estado a {string}")
    public void cambiarEstado(String nuevoEstado) {
        ejecutarCambioEstado(currentInvitationId, nuevoEstado);
    }

    @When("el administrador consulta las invitaciones pendientes")
    public void consultarPendientes() {
        configureRestAssured();
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(INV_PATH + "/pending");
    }

    @When("el administrador consulta el histórico de invitaciones")
    public void consultarHistorico() {
        configureRestAssured();
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(INV_PATH + "/history");
    }

    @When("el administrador consulta todas las invitaciones filtrando por {string}")
    public void consultarConFiltro(String estados) {
        configureRestAssured();
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .queryParam("statuses", estados)
                .get(INV_PATH + "/all");
    }

    @When("el administrador consulta los estados disponibles")
    public void consultarEstadosDisponibles() {
        configureRestAssured();
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(INV_PATH + "/statuses");
    }

    // =====================================================
    // THEN
    // =====================================================

    @Then("la invitación se crea correctamente")
    public void verificarCreacion() {
        SerenityRest.then().statusCode(201).body("status", equalTo("PENDING"));
    }

    @Then("la invitación tiene el estado {string}")
    public void verificarEstadoActual(String estadoEsperado) {
        configureRestAssured();
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(INV_PATH + "/all");

        SerenityRest.then()
                .body("find { it.id == " + currentInvitationId + " }.status", equalTo(estadoEsperado));
    }

    @And("se devuelve una lista de invitaciones pendientes")
    public void verificarListaPendientes() {
        SerenityRest.then()
                .statusCode(200)
                .body("status", everyItem(equalTo("PENDING")));
    }

    @And("se devuelve una lista de invitaciones del histórico")
    public void verificarListaHistorico() {
        SerenityRest.then()
                .statusCode(200)
                .body("status", everyItem(not(equalTo("PENDING"))));
    }

    @And("todas las invitaciones devueltas tienen el estado {string} o {string}")
    public void verificarFiltroMultiple(String s1, String s2) {
        SerenityRest.then()
                .statusCode(200)
                .body("status", everyItem(anyOf(equalTo(s1), equalTo(s2))));
    }

    @And("la lista contiene los enums de estado")
    public void verificarEnums() {
        SerenityRest.then()
                .body("$", hasItems("PENDING", "ACCEPTED", "REJECTED", "EXPIRED", "APPROVED"));
    }

    @Then("el mensaje de error indica {string}")
    public void el_mensaje_de_error_indica(String mensajeEsperado) {
        SerenityRest.then().body(containsString(mensajeEsperado));
    }

    // =====================================================
    // APOYO
    // =====================================================

    private void crearNuevaInvitacionEnServidor(String email) {
        configureRestAssured();
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .contentType("application/json")
                .body(String.format("{\"email\": \"%s\", \"name\": \"QA\", \"description\": \"Test\"}", email))
                .post(INV_PATH);

        if (SerenityRest.lastResponse().statusCode() == 201) {
            currentInvitationId = SerenityRest.lastResponse().jsonPath().getLong("id");
        }
    }

    @And("la lista contiene {string}, {string}, {string}, {string} y {string}")
    public void verificarListaCompletaDeEstados(String s1, String s2, String s3, String s4, String s5) {
        SerenityRest.then()
                .statusCode(200)
                .body("$", hasItems(s1, s2, s3, s4, s5))
                .body("size()", is(5)); // Validamos que están todos los que definiste en el Enum

        System.out.println("DEBUG: Estados verificados correctamente en el endpoint /statuses");
    }

    private void ejecutarCambioEstado(Long id, String estado) {
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .pathParam("id", id)
                .queryParam("newStatus", estado)
                .patch(INV_PATH + "/{id}/status");
    }
}