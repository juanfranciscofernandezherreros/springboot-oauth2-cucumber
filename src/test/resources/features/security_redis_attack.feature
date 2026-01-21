@security
Feature: Seguridad Avanzada - Protección Anti-Fuerza Bruta (Redis)

  Como administrador de seguridad
  Quiero que el sistema bloquee temporalmente las IPs que realicen múltiples intentos fallidos
  Para evitar ataques de diccionario o fuerza bruta automatizados

  Scenario: Detección automática de ataque y persistencia del bloqueo
    Given que existe un usuario para pruebas de seguridad con email "security_audit_v1@test.com" y password "Segura123!"
    When realizo múltiples intentos fallidos de login con password "PassINCORRECTA" hasta obtener el bloqueo
    Then el sistema debe haber respondido con código 423 Locked
    When intento hacer login con la contraseña CORRECTA "Segura123!"
    Then el sistema sigue respondiendo con código 423 Locked debido al bloqueo activo