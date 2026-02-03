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

    @Autowired
    private TestContexts testContext;

    private static final String INV_PATH = "/api/v1/admin/invitations";
    private Long currentInvitationId;

    // =====================================================
    // GIVEN - SETUP AUTOMÁTICO DE DATOS
    // =====================================================

    @Given("existe una invitación con estado {string}")
    public void existeInvitacionConEstado(String estado) {
        configureRestAssured();

        // 1. Siempre empezamos creando una en PENDING (es el único estado inicial válido)
        String email = "flow-test-" + System.currentTimeMillis() + "@test.com";
        crearNuevaInvitacionEnServidor(email);

        // 2. Transicionamos según lo que pida el test respetando el grafo
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
    // WHEN - ACCIONES
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

    @When("el administrador acepta la invitación")
    public void aceptarInvitacion() {
        ejecutarCambioEstado(currentInvitationId, "ACCEPTED");
    }

    @When("el administrador rechaza la invitación")
    public void rechazarInvitacion() {
        ejecutarCambioEstado(currentInvitationId, "REJECTED");
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

    // =====================================================
    // THEN - VALIDACIONES
    // =====================================================

    @Then("la invitación se crea correctamente")
    public void verificarCreacion() {
        SerenityRest.then().statusCode(201).body("status", equalTo("PENDING"));
    }

    @Then("la invitación tiene el estado {string}")
    public void verificarEstadoActual(String estadoEsperado) {
        configureRestAssured();
        // Buscamos en toda la base de datos (pendientes + histórico)
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(INV_PATH + "/pending");

        boolean estaEnPendientes = SerenityRest.lastResponse().jsonPath().getList("id").contains(currentInvitationId.intValue());

        if (!estaEnPendientes) {
            SerenityRest.given()
                    .header("Authorization", "Bearer " + testContext.getAccessToken())
                    .get(INV_PATH + "/history");
        }

        SerenityRest.then().body("find { it.id == " + currentInvitationId + " }.status", equalTo(estadoEsperado));
    }

    @And("se devuelve una lista de invitaciones pendientes")
    public void verificarListaPendientes() {
        SerenityRest.then()
                .statusCode(200)
                // 1. Validamos que la respuesta es una lista (no nula)
                .body("$", is(notNullValue()))
                .body("$", instanceOf(java.util.List.class))
                // 2. Validamos que, si hay elementos, todos sean PENDING
                // Usamos everyItem para asegurar la integridad del filtro del controlador
                .body("status", everyItem(equalTo("PENDING")));

        // Log para depuración en tu consola de Windows
        int count = SerenityRest.lastResponse().jsonPath().getList("$").size();
        System.out.println("DEBUG: Se han encontrado " + count + " invitaciones pendientes.");
    }

    @Then("el mensaje de error indica {string}")
    public void el_mensaje_de_error_indica(String mensajeEsperado) {
        // Validamos que el cuerpo de la respuesta contenga el texto esperado
        // Tu controlador devuelve un String plano en el .body(), así que esto funcionará perfecto
        SerenityRest.then()
                .body(containsString(mensajeEsperado));

        // Opcional: Imprime en consola para ver qué está llegando realmente
        System.out.println("Mensaje real recibido: " + SerenityRest.lastResponse().asString());
    }

    // =====================================================
    // LÓGICA PRIVADA DE APOYO
    // =====================================================

    private void crearNuevaInvitacionEnServidor(String email) {
        configureRestAssured();
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .contentType("application/json")
                .body("""
                    {
                      "email": "%s",
                      "name": "User Automation",
                      "description": "Test Invitación"
                    }
                    """.formatted(email))
                .post(INV_PATH);

        if (SerenityRest.lastResponse().statusCode() == 201) {
            currentInvitationId = SerenityRest.lastResponse().jsonPath().getLong("id");
        }
    }

    private void ejecutarCambioEstado(Long id, String estado) {
        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .pathParam("id", id)
                .queryParam("newStatus", estado)
                .patch(INV_PATH + "/{id}/status");
    }
}