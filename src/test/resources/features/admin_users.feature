@admin_list
Feature: Listado de usuarios por el administrador
  Como administrador del sistema
  Quiero obtener listados de usuarios
  Para poder supervisar y gestionar las cuentas

  Background:
    Given el administrador está autenticado

  Scenario: Obtener el listado completo de usuarios
    When el administrador solicita el listado de usuarios
    Then la respuesta tiene código 200

  Scenario: Obtener el listado de usuarios bloqueados
    When el administrador solicita el listado de usuarios bloqueados
    Then la respuesta tiene código 200
    And se devuelve una lista de usuarios bloqueados
    And el total de usuarios devueltos es igual que 1