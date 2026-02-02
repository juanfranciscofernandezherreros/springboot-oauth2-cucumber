@auth @reset-password
Feature: Restablecimiento de Contraseña
  Como usuario registrado
  Quiero poder cambiar mi contraseña si la he olvidado
  Para recuperar el acceso a mi cuenta

  Scenario: Cambio de contraseña exitoso
    Given reinicio el entorno y me autentico con usuario "reset_test@test.com" y password "PassActual123" y rol "USER"
    When solicito restablecer la contraseña del email "reset_test@test.com" a la nueva clave "NuevaPass456!"
    Then el sistema responde con código 200
    And la respuesta contiene el mensaje "Contraseña actualizada"
