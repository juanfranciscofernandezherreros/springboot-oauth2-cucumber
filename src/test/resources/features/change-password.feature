@change-password
Feature: Cambio de contraseña desde perfil con JWT

  Scenario: Usuario cambia su contraseña y sigue autenticado
    Given reinicio el entorno y me autentico con usuario "admin@test.com" y password "admin1234" y rol "ADMIN"
    When cambio mi contraseña a la nueva clave "admin5678"
    Then el sistema responde con código 200
    And la respuesta contiene el mensaje "Contraseña actualizada"
    And puedo acceder a mi perfil usando el nuevo access token
