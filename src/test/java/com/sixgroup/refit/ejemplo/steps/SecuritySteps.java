package com.sixgroup.refit.ejemplo.steps;

import com.sixgroup.refit.ejemplo.config.BaseRestConfig;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Assert;

public class SecuritySteps extends BaseRestConfig {

    private String emailGuardado;
    // Guardamos la contraseña correcta para usarla en el paso final
    private String passwordCorrectaGuardada;

    // --------------------------------------------------------------------------------
    // 1. REGISTRO (PREPARACIÓN)
    // --------------------------------------------------------------------------------
    @Given("que existe un usuario para pruebas de seguridad con email {string} y password {string}")
    public void registrar_usuario_seguridad(String email, String password) throws InterruptedException {
        configureRestAssured();
        this.emailGuardado = email;
        this.passwordCorrectaGuardada = password;

        String registerJson = """
        {
            "name": "Redis Security Tester",
            "email": "%s",
            "password": "%s",
            "role": "USER"
        }
        """.formatted(email, password);

        System.out.println("\n🔵 [SETUP] Registrando usuario para pruebas: " + email);

        SerenityRest
                .given()
                .contentType("application/json")
                .body(registerJson)
                .post("/auth/register");

        // Pausa obligatoria para evitar conflicto de tokens idénticos en BD
        Thread.sleep(1000);
    }

    // --------------------------------------------------------------------------------
    // 2. BOMBARDEO (EL BUCLE FOR)
    // --------------------------------------------------------------------------------
    @When("realizo múltiples intentos fallidos de login con password {string} hasta obtener el bloqueo")
    public void bombardeo_login(String wrongPassword) throws InterruptedException {
        String loginBadJson = """
        {
            "email": "%s",
            "password": "%s"
        }
        """.formatted(this.emailGuardado, wrongPassword);

        System.out.println("🟡 [ATAQUE] Iniciando BOMBARDEO de logins fallidos...");

        boolean bloqueoConseguido = false;

        // Intentamos hasta 10 veces (ajusta este número según tu config de Redis. Ej: si bloquea a los 5, pon 10)
        for (int i = 1; i <= 15; i++) {

            SerenityRest
                    .given()
                    .contentType("application/json")
                    .body(loginBadJson)
                    .post("/auth/login");

            int statusCode = SerenityRest.lastResponse().statusCode();

            if (statusCode == 401) {
                // 401 significa que la contraseña es mala, pero aún no estamos bloqueados.
                System.out.println("   ➡ Intento #" + i + ": 401 Unauthorized (Redis contando...)");
            } else if (statusCode == 423) {
                // 423 significa LOCKED. ¡Redis ha saltado!
                System.out.println("🔴 [EXITO] ¡BLOQUEO DETECTADO! en el intento #" + i + " (HTTP 423)");
                bloqueoConseguido = true;
                break; // Salimos del bucle inmediatamente
            } else {
                System.out.println("   ❓ Intento #" + i + ": Status inesperado " + statusCode);
            }

            // Pequeña pausa para no saturar los logs
            Thread.sleep(100);
        }

        // Si salimos del bucle y no se activó el booleano, fallamos el test
        if (!bloqueoConseguido) {
            Assert.fail("❌ ERROR CRÍTICO: Se realizaron los intentos y el sistema NUNCA devolvió 423 Locked. ¿Redis está funcionando?");
        }
    }

    // --------------------------------------------------------------------------------
    // 3. VERIFICACIÓN DEL CÓDIGO 423
    // --------------------------------------------------------------------------------
    @Then("el sistema debe haber respondido con código {int} Locked")
    public void verificar_codigo_bloqueo(int codigoEsperado) {
        // Validamos que la última respuesta capturada sea 423
        SerenityRest.then().statusCode(codigoEsperado);
    }

    // --------------------------------------------------------------------------------
    // 4. PRUEBA FINAL (VERIFICAR PERSISTENCIA DEL BLOQUEO)
    // --------------------------------------------------------------------------------
    @When("intento hacer login con la contraseña CORRECTA {string}")
    public void intento_login_correcto(String passwordCorrecta) {
        // Validamos que el feature no nos pase una contraseña distinta a la registrada
        if (!passwordCorrecta.equals(this.passwordCorrectaGuardada)) {
            System.out.println("⚠️ Warning: La contraseña del paso When no coincide con la del Given inicial.");
        }

        System.out.println("🔵 [PRUEBA FINAL] Intentando login con contraseña REAL (" + passwordCorrecta + ")...");

        String loginGoodJson = """
        {
            "email": "%s",
            "password": "%s"
        }
        """.formatted(this.emailGuardado, passwordCorrecta);

        SerenityRest
                .given()
                .contentType("application/json")
                .body(loginGoodJson)
                .post("/auth/login");
    }

    @Then("el sistema sigue respondiendo con código {int} Locked debido al bloqueo activo")
    public void verificar_bloqueo_persistente(int codigoEsperado) {
        int status = SerenityRest.lastResponse().statusCode();

        if (status == codigoEsperado) {
            System.out.println("✅ [VERIFICADO] El usuario sigue bloqueado (423) incluso con credenciales válidas.");
        } else {
            System.out.println("❌ [FALLO] El bloqueo desapareció. Código recibido: " + status);
        }

        SerenityRest.then().statusCode(codigoEsperado);
    }

    @When("espero {int} segundos para que expire el bloqueo")
    public void esperar_segundos(int segundos) throws InterruptedException {
        System.out.println("⏳ Esperando " + segundos + " segundos...");
        Thread.sleep(segundos * 1000L);
    }
}