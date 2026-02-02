@user @profile
Feature: Gestión del perfil de usuario

  Background:
    Given reinicio el entorno y me autentico con usuario "new.user@test.com" y password "User123!" y rol "USER"

  Scenario: Consultar perfil (GET)
    When envío una petición "GET" a "/api/v1/user/me" con los datos:
      | vacio | true |
    Then la respuesta tiene código 200
    And la respuesta contiene los siguientes datos:
      | email | new.user@test.com |
      | role  | USER              |

  Scenario: Actualizar perfil (PUT)
    When envío una petición "PUT" a "/api/v1/user/update" con los datos:
      | name | Nuevo Nombre |
    Then la respuesta tiene código 200
    And la respuesta contiene los siguientes datos:
      | name | Nuevo Nombre |