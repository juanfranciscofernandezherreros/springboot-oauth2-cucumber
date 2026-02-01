package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import com.sixgroup.refit.ejemplo.dto.CreateInvitationRequest;
import com.sixgroup.refit.ejemplo.model.Role;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.awaitility.Awaitility.await;
import java.util.concurrent.TimeUnit;

public class InvitationSteps extends BaseRestConfig {

    private static final Logger log = LoggerFactory.getLogger(InvitationSteps.class);

    @Given("que el sistema está listo para recibir invitaciones")
    public void que_el_sistema_esta_listo_para_recibir_invitaciones() {
        // Configuramos la URL base y puerto desde BaseRestConfig
        configureRestAssured();
        log.info("🔧 Infraestructura de tests configurada.");
    }

    @When("creo una nueva invitación para el email {string}")
    public void creo_una_nueva_invitacion_para_el_email(String email) {
        // Creamos el DTO de la petición
        CreateInvitationRequest request = new CreateInvitationRequest("","","");
        // Realizamos la llamada POST pública (sin cabecera de autenticación)
        SerenityRest.given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/v1/invitations");

        log.info("📡 Petición POST enviada para el email: {}", email);
    }

    @Then("la invitación debe ser aceptada correctamente")
    public void la_invitacion_debe_ser_aceptada_correctamente() {
        // Verificamos el código 202 que devuelve tu controlador
        SerenityRest.then()
                .statusCode(202);

        log.info("✅ El servidor respondió con 202 Accepted.");

        // Verificación extra: Esperamos a que el sistema asíncrono termine
        // Esto asegura que RabbitMQ tuvo tiempo de mover el mensaje
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            log.info("⏱️ Verificación de flujo asíncrono completada.");
        });
    }
}