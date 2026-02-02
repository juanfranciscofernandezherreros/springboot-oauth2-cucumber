@auth @refresh
Feature: Refresh de token JWT

  Como usuario autenticado
  Quiero renovar mi access token
  Para mantener la sesión activa sin volver a logarme

  Background:
    Given el administrador está autenticado

  Scenario: El usuario refresca el token correctamente
    When renuevo el access token con el refresh token
    Then la respuesta tiene código 200
    And se devuelve un nuevo access token

  Scenario: No se puede refrescar el token con un refresh token inválido
    When intento renovar el token con un refresh token inválido
    Then la respuesta tiene código 401
