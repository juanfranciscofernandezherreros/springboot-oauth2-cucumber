@auth
Feature: User Profile

  Scenario Outline: Usuario autenticado accede a su perfil
    Given reinicio el entorno y me autentico con usuario "<email>" y password "<password>" y rol "<role>"
    When el usuario accede a su perfil usando el access token
    Then el sistema responde con código 200

    Examples:
      | email                          | password       | role  |
      | usuario_profile_1@test.com     | passLogin123   | USER  |