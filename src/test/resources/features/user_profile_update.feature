@profile @update
Feature: Actualización de perfil de usuario

  Scenario Outline: Un usuario autenticado puede actualizar su perfil
    Given reinicio el entorno y me autentico con usuario "<email>" y password "<password>" y rol "<role>"
    When actualizo mi perfil con nombre "<nuevoNombre>"
    Then el sistema responde con código 200
    And el perfil actualizado tiene el nombre "<nuevoNombre>"

    Examples:
      | email               | password | role   | nuevoNombre        |
      | user@test.com       | 123      | USER   | Usuario Modificado |