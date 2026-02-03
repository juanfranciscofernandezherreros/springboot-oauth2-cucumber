@admin @users
Feature: Gestión de usuarios por administrador
  Como administrador del sistema
  Quiero gestionar usuarios
  Para controlar accesos y estados del sistema

  Background:
    Given el administrador está autenticado

  # =====================================================
  # CREAR USUARIO
  # =====================================================
  Scenario: El administrador crea un usuario con rol USER
    When el administrador crea un usuario con rol USER
    Then la respuesta tiene código 201

  # =====================================================
  # LISTAR USUARIOS
  # =====================================================
  Scenario: El administrador obtiene el listado de usuarios
    When el administrador solicita el listado de usuarios
    Then la respuesta tiene código 200
    And el total de usuarios devueltos es mayor que 0

  # =====================================================
  # LISTAR USUARIOS BLOQUEADOS
  # =====================================================
  Scenario: El administrador obtiene el listado de usuarios bloqueados
    When el administrador solicita el listado de usuarios bloqueados
    Then la respuesta tiene código 200
    And se devuelve una lista de usuarios bloqueados

  # =====================================================
  # CONSULTAR ESTADO DE USUARIO
  # =====================================================
  Scenario: El administrador consulta el estado de un usuario bloqueado
    When el administrador consulta el estado del usuario con email "locked.user@test.com"
    Then la respuesta tiene código 200

  # =====================================================
  # DESBLOQUEAR USUARIO
  # =====================================================
  Scenario: El administrador desbloquea un usuario bloqueado
    When el administrador desbloquea al usuario con email "locked.user@test.com"
    Then la respuesta tiene código 200

    # =====================================================
  # BLOQUEAR USUARIO
  # =====================================================
  Scenario: El administrador bloquea un usuario activo
    When el administrador bloquea al usuario con email "active.user@test.com"
    Then la respuesta tiene código 200

  # =====================================================
  # ACTUALIZAR ROL
  # =====================================================
  Scenario: El administrador actualiza el rol de un usuario
    When el administrador actualiza el rol del usuario "usuario@login.com" a "ADMIN"
    Then la respuesta tiene código 200

      # =====================================================
  # ACTUALIZAR USUARIO
  # =====================================================
  Scenario: El administrador actualiza los datos de un usuario
    When el administrador actualiza el usuario con id 7
      | name             | Nuevo Nombre Usuario |
      | role             | USER                 |
      | accountNonLocked | false                 |
    Then la respuesta tiene código 200

  # =====================================================
  # CONSULTAR ESTADÍSTICAS
  # =====================================================
  Scenario: El administrador consulta las estadísticas globales de usuarios
    When el administrador solicita las estadísticas de usuarios
    Then la respuesta tiene código 200
    And las estadísticas muestran un total de usuarios mayor o igual a 0
    And se visualiza el conteo de bloqueados e invitaciones pendientes

