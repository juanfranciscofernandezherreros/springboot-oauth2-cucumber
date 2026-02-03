# language: es
@admin @invitations @state-machine
Característica: Gestión del Ciclo de Vida de Invitaciones

  Como administrador del sistema
  Quiero gestionar los estados de las invitaciones y filtrar su historial
  Para controlar el acceso de nuevos usuarios de forma segura

  Antecedentes:
    Dado el administrador está autenticado

  # =====================================================
  # CREACIÓN Y DUPLICADOS
  # =====================================================

  Escenario: Crear una invitación nueva correctamente
    Cuando creo una nueva invitación para el email "nuevo-usuario@test.com"
    Entonces la respuesta tiene código 201
    Y la invitación se crea correctamente

  Escenario: Evitar duplicados de invitaciones pendientes
    Dado existe una invitación pendiente
    Cuando creo una nueva invitación para el email "repetido@test.com"
    Y creo una nueva invitación para el email "repetido@test.com"
    Entonces la respuesta tiene código 409

  # =====================================================
  # TRANSICIONES DE LA MÁQUINA DE ESTADOS
  # =====================================================

  Escenario: Flujo de aprobación: de Pendiente a Aceptada y luego a Aprobada
    Dado existe una invitación con estado "PENDING"
    Cuando el administrador cambia el estado a "ACCEPTED"
    Entonces la respuesta tiene código 200
    Y la invitación tiene el estado "ACCEPTED"

    Cuando el administrador cambia el estado a "APPROVED"
    Entonces la respuesta tiene código 200
    Y la invitación tiene el estado "APPROVED"

  Escenario: Rechazar una invitación directamente desde pendiente
    Dado existe una invitación con estado "PENDING"
    Cuando el administrador cambia el estado a "REJECTED"
    Entonces la respuesta tiene código 200
    Y la invitación tiene el estado "REJECTED"

  # =====================================================
  # REGLAS DE NEGOCIO (TRANSICIONES INVÁLIDAS)
  # =====================================================

  Esquema del escenario: Bloquear movimientos ilegales en el grafo de estados
    Dado existe una invitación con estado "<estado_actual>"
    Cuando el administrador intenta cambiar el estado a "<nuevo_estado>"
    Entonces la respuesta tiene código 409
    Y el mensaje de error indica "Transición no permitida"

    Ejemplos:
      | estado_actual | nuevo_estado | motivo                              |
      | REJECTED      | ACCEPTED     | No se puede reabrir un rechazo      |
      | APPROVED      | PENDING      | No se puede volver atrás            |
      | PENDING       | APPROVED     | Debe pasar por ACCEPTED primero     |
      | REJECTED      | APPROVED     | Un rechazado no puede ser aprobado  |

  # =====================================================
  # LISTADOS, FILTROS E HISTÓRICO
  # =====================================================

  Escenario: Consultar el listado de invitaciones pendientes
    Dado existe una invitación con estado "PENDING"
    Cuando el administrador consulta las invitaciones pendientes
    Entonces la respuesta tiene código 200
    Y se devuelve una lista de invitaciones pendientes

  Escenario: Consultar el histórico de invitaciones (Excluye pendientes)
    Dado existen invitaciones aceptadas o expiradas
    Cuando el administrador consulta el histórico de invitaciones
    Entonces la respuesta tiene código 200
    Y se devuelve una lista de invitaciones del histórico

  Escenario: Filtrado dinámico por múltiples estados
    Dado existe una invitación con estado "ACCEPTED"
    Y existe una invitación con estado "REJECTED"
    Cuando el administrador consulta todas las invitaciones filtrando por "ACCEPTED,REJECTED"
    Entonces la respuesta tiene código 200
    Y todas las invitaciones devueltas tienen el estado "ACCEPTED" o "REJECTED"

  Escenario: Consultar metadatos de estados disponibles
    Cuando el administrador consulta los estados disponibles
    Entonces la respuesta tiene código 200
    Y la lista contiene "PENDING", "ACCEPTED", "REJECTED", "EXPIRED" y "APPROVED"