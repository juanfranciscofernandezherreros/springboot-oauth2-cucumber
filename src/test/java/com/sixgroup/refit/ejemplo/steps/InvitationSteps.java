package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.*;

public class InvitationSteps extends BaseRestConfig {

    private static final String INV_PATH = "/api/v1/admin/invitations";
    private Long currentInvitationId;

    // Inyectamos el contexto para obtener el token (Asegúrate de tener esta clase o similar)
    @Autowired
    private TestContexts testContext;

    // =====================================================
    // GIVEN
    // =====================================================

    @Given("existe una invitación con estado {string}")
    public void anInvitationExistsWithStatus(String status) {
        String email = "status-test-" + System.currentTimeMillis() + "@test.com";
        createInvitationOnServer(email);

        if (!status.equals("PENDING")) {
            changeInvitationStatus(currentInvitationId, status);
        }
    }

    @Given("existe una invitación pendiente")
    public void aPendingInvitationExists() {
        createInvitationOnServer("pending-" + System.currentTimeMillis() + "@test.com");
    }

    @Given("existen invitaciones aceptadas o expiradas")
    public void historyInvitationsExist() {
        anInvitationExistsWithStatus("ACCEPTED");
        anInvitationExistsWithStatus("EXPIRED");
    }

    // =====================================================
    // WHEN (Acciones con Seguridad añadida)
    // =====================================================

    @When("creo una nueva invitación para el email {string}")
    public void iCreateANewInvitationForEmail(String email) {
        createInvitationOnServer(email);
    }

    @When("el administrador cambia el estado a {string}")
    @When("el administrador intenta cambiar el estado a {string}")
    public void adminChangesStatusTo(String newStatus) {
        changeInvitationStatus(currentInvitationId, newStatus);
    }

    @When("el administrador consulta las invitaciones pendientes")
    public void adminConsultsPendingInvitations() {
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(INV_PATH + "/pending");
    }

    @When("el administrador consulta el histórico de invitaciones")
    public void adminConsultsHistory() {
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(INV_PATH + "/history");
    }

    @When("el administrador consulta todas las invitaciones filtrando por {string}")
    public void adminConsultsWithFilter(String statuses) {
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .queryParam("statuses", statuses)
                .get(INV_PATH + "/all");
    }

    @When("el administrador consulta los estados disponibles")
    public void adminConsultsAvailableStatuses() {
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(INV_PATH + "/statuses");
    }

    @Then("la invitación se crea correctamente")
    public void invitationIsCreatedCorrectly() {
        SerenityRest.then()
                .statusCode(201)
                .body("status", equalTo("PENDING"))
                .body("id", notNullValue());
    }

    @Then("la invitación tiene el estado {string}")
    public void invitationHasStatus(String expectedStatus) {
        // Necesitamos el token también para verificar el estado final
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(INV_PATH + "/all");

        SerenityRest.then()
                .body("find { it.id == " + currentInvitationId + " }.status", equalTo(expectedStatus));
    }

    @Then("el mensaje de error indica {string}")
    public void errorMessageIndicates(String expectedMessage) {
        SerenityRest.then().body(containsString(expectedMessage));
    }

    @And("se devuelve una lista de invitaciones pendientes")
    public void returnsListOfPendingInvitations() {
        SerenityRest.then().body("status", everyItem(equalTo("PENDING")));
    }

    @And("se devuelve una lista de invitaciones del histórico")
    public void returnsListOfHistoryInvitations() {
        SerenityRest.then().body("status", everyItem(not(equalTo("PENDING"))));
    }

    @And("todas las invitaciones devueltas tienen el estado {string} o {string}")
    public void allReturnedInvitationsHaveStatus(String s1, String s2) {
        SerenityRest.then().body("status", everyItem(anyOf(equalTo(s1), equalTo(s2))));
    }

    @And("la lista contiene {string}, {string}, {string}, {string} y {string}")
    public void listContainsAllStatuses(String s1, String s2, String s3, String s4, String s5) {
        SerenityRest.then().body("$", hasItems(s1, s2, s3, s4, s5));
    }

    // =====================================================
    // PRIVATE HELPERS (Donde estaba el error de seguridad)
    // =====================================================

    private void createInvitationOnServer(String email) {
        String body = String.format("{\"email\": \"%s\", \"name\": \"QA\", \"description\": \"Test\"}", email);
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken()) // <--- TOKEN AÑADIDO
                .contentType("application/json")
                .body(body)
                .post(INV_PATH);

        if (SerenityRest.lastResponse().statusCode() == 201) {
            currentInvitationId = SerenityRest.lastResponse().jsonPath().getLong("id");
        }
    }

    private void changeInvitationStatus(Long id, String status) {
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken()) // <--- TOKEN AÑADIDO
                .pathParam("id", id)
                .queryParam("newStatus", status)
                .patch(INV_PATH + "/{id}/status");
    }
}