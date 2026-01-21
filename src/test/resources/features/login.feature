@auth
Feature: Autenticación de Usuarios (Login)

  Scenario Outline: Usuario inicia sesión y recibe tokens correctamente
    Given reinicio el entorno y me autentico con usuario "<email>" y password "<password>" y rol "<role>"
    And la respuesta contiene un access token y un refresh token
    Then el sistema responde con código 200

    Examples:
      | email             | password     | role       |
      | usuario@login.com | passLogin123 | USER  |