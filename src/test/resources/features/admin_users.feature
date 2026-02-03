# language: es
@admin @users
Característica: Gestión de Usuarios por Administrador

  Como administrador del sistema
  Quiero gestionar las cuentas de usuario, sus roles y estados de bloqueo
  Para controlar los accesos y mantener la seguridad del sistema

  Antecedentes:
    Dado el administrador está autenticado

  # =====================================================
  # CREACIÓN Y CONSULTA GENERAL
  # =====================================================

  Escenario: El administrador crea un usuario con rol USER
    Cuando el administrador crea un usuario con rol USER
    Entonces la respuesta tiene código 201

  Escenario: El administrador obtiene el listado de usuarios
    Cuando el administrador solicita el listado de usuarios
    Entonces la respuesta tiene código 200
    Y el total de usuarios devueltos es mayor que 0

  # =====================================================
  # GESTIÓN DE BLOQUEOS Y SEGURIDAD
  # =====================================================

  Escenario: El administrador obtiene el listado de usuarios bloqueados
    Cuando el administrador solicita el listado de usuarios bloqueados
    Entonces la respuesta tiene código 200
    Y se devuelve una lista de usuarios bloqueados

  Escenario: El administrador consulta el estado de un usuario bloqueado
    Cuando el administrador consulta el estado del usuario con email "locked.user@test.com"
    Entonces la respuesta tiene código 200

  Escenario: El administrador desbloquea un usuario bloqueado
    Cuando el administrador desbloquea al usuario con email "locked.user@test.com"
    Entonces la respuesta tiene código 200

  Escenario: El administrador bloquea un usuario activo
    Cuando el administrador bloquea al usuario con email "active.user@test.com"
    Entonces la respuesta tiene código 200

  # =====================================================
  # ACTUALIZACIÓN DE DATOS Y ROLES
  # =====================================================

  Escenario: El administrador actualiza el rol de un usuario
    Cuando el administrador actualiza el rol del usuario "usuario@login.com" a "ADMIN"
    Entonces la respuesta tiene código 200

  Escenario: El administrador actualiza los datos de un usuario por ID
    Cuando el administrador actualiza el usuario con id 7
      | name             | Nuevo Nombre Usuario |
      | role             | USER                 |
      | accountNonLocked | false                |
    Entonces la respuesta tiene código 200

  # =====================================================
  # MÉTRICAS Y REPORTES
  # =====================================================

  Escenario: El administrador consulta las estadísticas globales de usuarios
    Cuando el administrador solicita las estadísticas de usuarios
    Entonces la respuesta tiene código 200
    Y las estadísticas muestran un total de usuarios mayor o igual a 0
    Y se visualiza el conteo de bloqueados e invitaciones pendientes