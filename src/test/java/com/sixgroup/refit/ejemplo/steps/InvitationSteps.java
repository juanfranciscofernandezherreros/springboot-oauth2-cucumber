package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import com.sixgroup.refit.ejemplo.dto.CreateInvitationRequest;
import com.sixgroup.refit.ejemplo.model.Invitation;
import com.sixgroup.refit.ejemplo.model.InvitationStatus;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.*;

public class InvitationSteps extends BaseRestConfig {

    private static final Logger log = LoggerFactory.getLogger(InvitationSteps.class);

    @Autowired
    private TestContexts testContext;

    private Long invitationId;
    private String lastEmailUsed;

    /* ======================================================
       GIVEN — ESTADO
       ====================================================== */

    @Given("existe una invitación pendiente")
    public void existe_una_invitacion_pendiente() {

        lastEmailUsed = "pending-" + System.currentTimeMillis() + "@sixgroup.com";

        CreateInvitationRequest request =
                new CreateInvitationRequest(
                        lastEmailUsed,
                        "Usuario Pendiente",
                        "Invitación pendiente"
                );

        Invitation invitation =
                SerenityRest.given()
                        .contentType("application/json")
                        .body(request)
                        .when()
                        .post("/api/v1/admin/invitations") // público
                        .then()
                        .statusCode(201)
                        .extract()
                        .as(Invitation.class);

        invitationId = invitation.getId();

        log.info("📌 Invitación pendiente creada con id {}", invitationId);
    }

    @Given("existen invitaciones aceptadas o expiradas")
    public void existen_invitaciones_aceptadas_o_expiradas() {

        existe_una_invitacion_pendiente();

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .when()
                .patch("/api/v1/admin/invitations/{id}/accept", invitationId)
                .then()
                .statusCode(200);

        log.info("📌 Invitación aceptada para histórico");
    }

    /* ======================================================
       WHEN — ACCIONES
       ====================================================== */

    @When("creo una nueva invitación para el email {string}")
    public void creo_una_nueva_invitacion_para_el_email(String email) {

        lastEmailUsed = email;

        CreateInvitationRequest request =
                new CreateInvitationRequest(
                        email,
                        "Usuario Test",
                        "Invitación de prueba"
                );

        SerenityRest.given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/v1/admin/invitations");

        log.info("📡 POST invitación enviada para {}", email);
    }

    @When("el administrador consulta las invitaciones pendientes")
    public void el_administrador_consulta_las_invitaciones_pendientes() {

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .when()
                .get("/api/v1/admin/invitations/pending");

        log.info("📡 GET invitaciones pendientes (admin)");
    }

    @When("el administrador acepta la invitación")
    public void el_administrador_acepta_la_invitacion() {

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .when()
                .patch("/api/v1/admin/invitations/{id}/accept", invitationId);

        log.info("📡 PATCH aceptar invitación {}", invitationId);
    }

    @When("el administrador consulta el histórico de invitaciones")
    public void el_administrador_consulta_el_historico_de_invitaciones() {

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .when()
                .get("/api/v1/admin/invitations/history");

        log.info("📡 GET histórico de invitaciones (admin)");
    }

    /* ======================================================
       THEN — ASSERTS
       ====================================================== */

    @Then("la invitación se crea correctamente")
    public void la_invitacion_se_crea_correctamente() {

        SerenityRest.then()
                .statusCode(201)
                .body("email", equalTo(lastEmailUsed))
                .body("status", equalTo(InvitationStatus.PENDING.name()));

        log.info("✅ Invitación creada correctamente");
    }

    @Then("el sistema rechaza la invitación por duplicada")
    public void el_sistema_rechaza_la_invitacion_por_duplicada() {

        SerenityRest.then()
                .statusCode(409);

        log.info("⛔ Invitación duplicada rechazada");
    }

    @Then("se devuelve una lista de invitaciones pendientes")
    public void se_devuelve_una_lista_de_invitaciones_pendientes() {

        SerenityRest.then()
                .statusCode(200)
                .body("$", not(empty()));

        log.info("📄 Lista de invitaciones pendientes devuelta");
    }

    @Then("el sistema devuelve el histórico correctamente")
    public void el_sistema_devuelve_el_historico_correctamente() {

        SerenityRest.then()
                .statusCode(200)
                .body("$", notNullValue());

        log.info("📚 Histórico de invitaciones devuelto");
    }
}
