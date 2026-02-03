@auth
Feature: Autenticación de Usuarios (Login)

  Scenario Outline: Usuario inicia sesión y recibe tokens correctamente
    Given reinicio el entorno y me autentico con usuario "<email>" y password "<password>" y rol "<role>"
    And la respuesta contiene un access token y un refresh token
    Then el sistema responde con código 200

    Examples:
      | email             | password     | role       |
      | usuario@login.com | passLogin123 | USER  |

  Scenario: Cambio de contraseña exitoso
    Given reinicio el entorno y me autentico con usuario "reset_test@test.com" y password "PassActual123" y rol "USER"
    When solicito restablecer la contraseña del email "reset_test@test.com" a la nueva clave "NuevaPass456!"
    Then el sistema responde con código 200
    And la respuesta contiene el mensaje "Contraseña actualizada"