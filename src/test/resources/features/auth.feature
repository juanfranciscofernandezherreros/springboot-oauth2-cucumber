@auth
Feature: Autenticación y Seguridad de Usuarios

  Background:

  Scenario Outline: Inicio de sesión exitoso y recepción de tokens
    Given reinicio el entorno y me autentico con usuario "<email>" y password "<password>" y rol "<role>"
    Then el sistema responde con código 200
    And la respuesta contiene un access token y un refresh token

    Examples:
      | email             | password     | role  |
      | usuario@login.com | passLogin123 | USER  |