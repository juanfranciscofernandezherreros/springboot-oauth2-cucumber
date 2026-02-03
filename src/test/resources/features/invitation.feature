@admin @invitations @state-machine
Feature: Gestión del Ciclo de Vida de Invitaciones

  Como administrador del sistema
  Quiero gestionar los estados de las invitaciones y su historial
  Para controlar el acceso de nuevos usuarios de forma segura

  Background:
    Given el administrador está autenticado

  # =====================================================
  # CREACIÓN Y DUPLICADOS
  # =====================================================

  Scenario: Crear una invitación nueva correctamente
    When creo una nueva invitación para el email "nuevo-usuario@test.com"
    Then la respuesta tiene código 201
    And la invitación se crea correctamente

  Scenario: Evitar duplicados de invitaciones pendientes
    Given existe una invitación pendiente
    When creo una nueva invitación para el email "repetido@test.com"
    And creo una nueva invitación para el email "repetido@test.com"
    Then la respuesta tiene código 409

  # =====================================================
  # TRANSICIONES VÁLIDAS (MÁQUINA DE ESTADOS)
  # =====================================================

  Scenario: Flujo de aprobación: de Pendiente a Aceptada y luego a Aprobada
    Given existe una invitación con estado "PENDING"
    When el administrador cambia el estado a "ACCEPTED"
    Then la respuesta tiene código 200
    And la invitación tiene el estado "ACCEPTED"

    When el administrador cambia el estado a "APPROVED"
    Then la respuesta tiene código 200
    And la invitación tiene el estado "APPROVED"

  Scenario: Rechazar una invitación directamente desde pendiente
    Given existe una invitación con estado "PENDING"
    When el administrador cambia el estado a "REJECTED"
    Then la respuesta tiene código 200
    And la invitación tiene el estado "REJECTED"

  # =====================================================
  # TRANSICIONES INVÁLIDAS (REGLAS DEL GRAFO)
  # =====================================================

  Scenario Outline: Bloquear movimientos ilegales en el grafo de estados
    Given existe una invitación con estado "<estado_actual>"
    When el administrador intenta cambiar el estado a "<nuevo_estado>"
    Then la respuesta tiene código 409
    And el mensaje de error indica "Transición no permitida"

    Examples:
      | estado_actual | nuevo_estado | motivo                             |
      | REJECTED      | ACCEPTED     | No se puede reabrir un rechazo     |
      | APPROVED      | PENDING      | No se puede volver atrás de aprobado|
      | PENDING       | APPROVED     | Debe pasar por ACCEPTED primero    |
      | REJECTED      | APPROVED     | Un rechazado no puede ser aprobado |

  # =====================================================
  # LISTADOS E HISTÓRICO
  # =====================================================

  Scenario: Consultar el listado de invitaciones pendientes
    Given existe una invitación con estado "PENDING"
    When el administrador consulta las invitaciones pendientes
    Then la respuesta tiene código 200
    And se devuelve una lista de invitaciones pendientes

  Scenario: Consultar el histórico de invitaciones (No pendientes)
    Given existen invitaciones aceptadas o expiradas
    When el administrador consulta el histórico de invitaciones
    Then la respuesta tiene código 200
    And la invitación tiene el estado "ACCEPTED"