@auth @logout
Feature: Logout de sesión

  Como usuario autenticado
  Quiero cerrar sesión
  Para invalidar mi access token

  Background:
    Given el administrador está autenticado

  Scenario: El usuario hace logout correctamente
    When hago una petición de logout
    Then la respuesta tiene código 200

  Scenario: El token no es válido después del logout
    Given el administrador está autenticado
    When hago una petición de logout
    And intento acceder a un endpoint protegido
    Then la respuesta tiene código 401
