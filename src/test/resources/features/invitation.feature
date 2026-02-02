@admin @invitations
Feature: Gestión de Invitaciones

  Como administrador del sistema
  Quiero gestionar invitaciones de usuarios
  Para controlar el acceso a la plataforma

  Background:
    Given el administrador está autenticado

  # =====================================================
  # CREAR INVITACIÓN (POST /api/v1/admin/invitations)
  # =====================================================

  Scenario: El administrador crea una invitación correctamente
    When creo una nueva invitación para el email "test-invite@sixgroup.com"
    Then la invitación se crea correctamente

  Scenario: No permitir crear dos invitaciones pendientes para el mismo email
    When creo una nueva invitación para el email "duplicate@sixgroup.com"
    And creo una nueva invitación para el email "duplicate@sixgroup.com"
    Then el sistema rechaza la invitación por duplicada

  # =====================================================
  # LISTAR INVITACIONES PENDIENTES
  # GET /api/v1/admin/invitations/pending
  # =====================================================

  Scenario: El administrador obtiene las invitaciones pendientes
    Given existe una invitación pendiente
    When el administrador consulta las invitaciones pendientes
    Then la respuesta tiene código 200
    And se devuelve una lista de invitaciones pendientes

  # =====================================================
  # ACEPTAR INVITACIÓN
  # PATCH /api/v1/admin/invitations/{id}/accept
  # =====================================================

  Scenario: El administrador acepta una invitación pendiente
    Given existe una invitación pendiente
    When el administrador acepta la invitación
    Then la respuesta tiene código 200

  # =====================================================
  # HISTÓRICO DE INVITACIONES
  # GET /api/v1/admin/invitations/history
  # =====================================================

  Scenario: El administrador consulta el histórico de invitaciones
    Given existen invitaciones aceptadas o expiradas
    When el administrador consulta el histórico de invitaciones
    Then la respuesta tiene código 200
