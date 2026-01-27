@auth @register
Feature: Registro de usuarios
  Como sistema de autenticación
  Quiero registrar nuevos usuarios
  Para que puedan iniciar sesión posteriormente

  # =========================================================
  # REGISTRO CORRECTO
  # =========================================================
  Scenario Outline: Registro exitoso de usuarios con rol USER
    Given reinicio el entorno y me registro con usuario "<email>" y password "<password>"
    Then el sistema responde con código 201

    Examples:
      | email                 | password      | role |
      | usuarioV1@login.com   | passLogin123  | USER |

  # =========================================================
  # ERRORES DE REGISTRO
  # =========================================================
  Scenario: Registro falla si se intenta registrar un ADMIN
    Given reinicio el entorno
    When registro un usuario con email "admin@login.com" y password "Admin123!" y rol "ADMIN"
    Then el sistema responde con código 400

  Scenario: Registro falla si el rol no pertenece al sistema
    Given reinicio el entorno
    When registro un usuario con email "rol_invalido@login.com" y password "Pass1234!" y rol "ROLE_USER"
    Then el sistema responde con código 400

  Scenario: Registro falla si el email ya existe
    Given reinicio el entorno y me registro con usuario "duplicado@login.com" y password "Password123!"
    When registro un usuario con email "duplicado@login.com" y password "Password123!" y rol "USER"
    Then el sistema responde con código 400
