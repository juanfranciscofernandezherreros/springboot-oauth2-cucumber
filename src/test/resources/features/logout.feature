@logout
Feature: Pruebas de Perfil de Usuario

  Scenario: Login, acceso al perfil, logout y nuevo acceso
    Given reinicio el entorno y me autentico con usuario "fresh_user@test.com" y password "123" y rol "USER"
    When intento acceder a mi perfil
    Then recibo mi perfil de usuario correctamente
    When cierro la sesión
    Then el sistema responde con código 200
    When intento acceder a mi perfil
    Then el sistema responde con código 401
