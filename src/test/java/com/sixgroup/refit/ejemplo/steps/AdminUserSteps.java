package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import static com.sixgroup.refit.ejemplo.utils.constants.AdminApiPaths.*;
import static org.hamcrest.Matchers.*;

public class AdminUserSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    // =====================================================
    // CREATE USER  -> POST /api/v1/admin/create-user
    // =====================================================

    @When("el administrador crea un usuario con rol USER")
    public void el_administrador_crea_un_usuario_user() {

        configureRestAssured();

        String email = "user_" + System.currentTimeMillis() + "@test.com";

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .contentType("application/json")
                .body("""
                    {
                      "name": "Kiko",
                      "email": "%s",
                      "password": "User123!",
                      "role": "USER"
                    }
                    """.formatted(email))
                .post(BASE+CREATE_USER);
    }

    // =====================================================
    // LIST USERS -> GET /api/v1/admin/users
    // =====================================================

    @When("el administrador solicita el listado de usuarios")
    public void el_administrador_solicita_el_listado_de_usuarios() {

        configureRestAssured();

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(BASE+USERS);
    }

    // =====================================================
    // LIST LOCKED USERS -> GET /api/v1/admin/locked-users
    // =====================================================

    @When("el administrador solicita el listado de usuarios bloqueados")
    public void el_administrador_solicita_el_listado_de_usuarios_bloqueados() {

        configureRestAssured();

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .get(BASE+LOCKED_USERS);
    }

    // =====================================================
    // USER STATUS -> GET /api/v1/admin/user-status?email=
    // =====================================================

    @When("el administrador consulta el estado del usuario con email {string}")
    public void el_administrador_consulta_estado_usuario(String email) {

        configureRestAssured();

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .queryParam("email", email)
                .get(BASE+USER_STATUS);
    }

    // =====================================================
    // UNLOCK USER -> POST /api/v1/admin/unlock
    // =====================================================

    @When("el administrador desbloquea al usuario con email {string}")
    public void el_administrador_desbloquea_usuario(String email) {

        configureRestAssured();

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .contentType("application/json")
                .body("""
                    {
                      "email": "%s"
                    }
                    """.formatted(email))
                .post(BASE+UNLOCK_USER);
    }

    // =====================================================
    // LOCK USER -> POST /api/v1/admin/lock
    // =====================================================

    @When("el administrador bloquea al usuario con email {string}")
    public void el_administrador_bloquea_usuario(String email) {

        configureRestAssured();

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .contentType("application/json")
                .body("""
                    {
                      "email": "%s"
                    }
                    """.formatted(email))
                .post(BASE+LOCK_USER);
    }

    // =====================================================
    //  -> PUT /api/v1/admin/update-role
    // =====================================================

    @When("el administrador actualiza el rol del usuario {string} a {string}")
    public void el_administrador_actualiza_rol(String email, String role) {

        configureRestAssured();

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .contentType("application/json")
                .body("""
                    {
                      "email": "%s",
                      "role": "%s"
                    }
                    """.formatted(email, role))
                .put(BASE+UPDATE_ROLE);
    }

    // =====================================================
    // DELETE USER -> DELETE /api/v1/admin/delete?email=
    // =====================================================

    @When("el administrador elimina el usuario con email {string}")
    public void el_administrador_elimina_usuario(String email) {

        configureRestAssured();

        SerenityRest.given()
                .header("Authorization", "Bearer " + testContext.getAccessToken())
                .queryParam("email", email)
                .delete(BASE+DELETE_USER);
    }

    // =====================================================
    // ASSERTS GENERALES
    // =====================================================

    @Then("la respuesta tiene código {int}")
    public void la_respuesta_tiene_codigo(int status) {
        SerenityRest.then().statusCode(status);
    }

    @And("el total de usuarios devueltos es igual que {int}")
    public void el_total_de_usuarios_es_igual_que(int total) {
        SerenityRest.then().body("size()", equalTo(total));
    }

    @And("el total de usuarios devueltos es mayor que {int}")
    public void el_total_de_usuarios_es_mayor_que(int min) {
        SerenityRest.then().body("size()", greaterThan(min));
    }

    @And("se devuelve una lista de usuarios bloqueados")
    public void se_devuelve_una_lista_de_usuarios_bloqueados() {
        SerenityRest.then()
                .body("$", notNullValue())
                .body("$", is(instanceOf(List.class)));
    }
}
