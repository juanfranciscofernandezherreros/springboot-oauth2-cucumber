package com.fernandez.backend.steps;

import com.fernandez.backend.config.BaseRestConfig;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.hamcrest.Matchers.*;

public class UserProfileSteps extends BaseRestConfig {

    @Autowired
    private TestContexts testContext;

    @When("envío una petición {string} a {string} con los datos:")
    public void envioPeticionGenerica(String metodo, String endpoint, DataTable dataTable) {
        configureRestAssured();

        // Verificación de seguridad para el token
        String token = testContext.getAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("ERROR: El token de acceso es nulo. Revisa el Background de tu feature.");
        }

        Map<String, String> data = dataTable.asMap();

        var request = SerenityRest.given()
                .header("Authorization", "Bearer " + token) // <--- Aquí se envía el token
                .contentType("application/json");

        // Ejecución dinámica según el método
        switch (metodo.toUpperCase()) {
            case "GET" -> request.get(endpoint);
            case "PUT" -> request.body(data).put(endpoint);
            case "POST" -> request.body(data).post(endpoint);
            case "DELETE" -> request.delete(endpoint);
            default -> throw new IllegalArgumentException("Método HTTP no soportado: " + metodo);
        }
    }

    @Then("la respuesta contiene los siguientes datos:")
    public void validarRespuesta(DataTable dataTable) {
        Map<String, String> expectedData = dataTable.asMap();
        var response = SerenityRest.then();

        expectedData.forEach((key, value) -> {
            // Esto valida que el JSON de respuesta tenga la clave y el valor esperado
            response.body(key, equalTo(value));
        });
    }
}