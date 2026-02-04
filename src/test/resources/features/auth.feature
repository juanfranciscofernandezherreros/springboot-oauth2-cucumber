@auth
Feature: Autenticación y Seguridad de Usuarios

  Background:
    # Este paso prepara la BD y obtiene el token necesario
    Given reinicio el entorno y me autentico con usuario "" y password "Pass123!" y rol "ADMIN"

  Scenario Outline: Inicio de sesión exitoso y recepción de tokens
    Given reinicio el entorno y me autentico con usuario "<email>" y password "<password>" y rol "<role>"
    Then el sistema responde con código 200
    And la respuesta contiene un access token y un refresh token

    Examples:
      | email             | password     | role  |
      | usuario@login.com | passLogin123 | USER  |

  Scenario: Cambio de contraseña satisfactorio desde el perfil
    Given reinicio el entorno y me autentico con usuario "reset@test.com" y password "OldPass123" y rol "USER"
    When solicito restablecer la contraseña del email "reset@test.com" a la nueva clave "NewPass456!"
    Then el sistema responde con código 200
    And la respuesta contiene el mensaje "Contraseña actualizada"