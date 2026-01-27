mvn verify -Dcucumber.filter.tags="not @security"
mvn verify -Dcucumber.filter.tags="@security"
mvn verify -Dcucumber.filter.tags="@logout"
mvn verify -Dcucumber.filter.tags="@admin_create_user_role"
mvn verify -Dcucumber.filter.tags="@admin_list"


🔐 /auth/register
✅ Escenarios válidos

Registro exitoso

Usuario nuevo

Email no existente

Rol válido (USER, ADMIN, MANAGER)

✔ Devuelve 201 CREATED

Intento de registro con email duplicado

Email ya registrado

✔ Devuelve 400 / 409 (según cómo lo tengas en AuthService)

❗ Muy importante para BDD

Registro con rol inválido

Ej: "ROLE_USER"

✔ Devuelve 400

Ya lo has visto fallar → escenario obligatorio

🔑 /auth/login
✅ Escenarios válidos

Login correcto

Usuario existente

Password correcta

✔ Devuelve 200

✔ access_token

✔ refresh_token

Login con contraseña incorrecta

✔ Devuelve 401

✔ Registra intento fallido

Login con usuario bloqueado

Bloqueo por intentos

✔ Devuelve 423 LOCKED

Login con IP bloqueada (Redis)

Tras varios intentos

✔ Devuelve 423 LOCKED

✔ Persistencia del bloqueo

♻️ /auth/refresh-token
✅ Escenarios válidos

Refresh token válido

Authorization: Bearer <refresh>

✔ Devuelve 200

✔ Nuevos tokens

Refresh token inválido o caducado

✔ Devuelve 401 UNAUTHORIZED

Sin header Authorization

✔ Devuelve 401

🚪 /auth/logout
✅ Escenarios válidos

Logout correcto

Access token válido

✔ Devuelve 200

✔ Token revocado

Logout con token inválido

✔ Devuelve 200 o 401 (según diseño)

(Importante documentarlo en BDD)

🔁 /auth/reset-password (MUY IMPORTANTE)

Este método está muy bien diseñado 👍
Aquí tienes los escenarios clave:

✅ Escenarios HAPPY PATH

Reset de contraseña exitoso

Usuario existente

Usuario NO bloqueado

IP NO bloqueada

✔ Devuelve 200

✔ { "mensaje": "Contraseña actualizada" }

Login con nueva contraseña

✔ Devuelve 200

🚫 Escenarios de bloqueo (CRÍTICOS)

Reset en cuenta bloqueada

Usuario bloqueado

✔ Devuelve 423 LOCKED

✔ { "error": "La cuenta se encuentra bloqueada." }

Reset desde IP bloqueada

IP bloqueada en Redis

✔ Devuelve 423 LOCKED

❌ Escenarios de error

Reset con usuario inexistente

✔ Devuelve 400

Mensaje genérico (no filtrar info)

Reset con payload inválido

Password vacía / nula

✔ Devuelve 400

🧪 MATRIZ FINAL DE ESCENARIOS (resumen)
Endpoint	Escenarios
/register	3
/login	4
/refresh-token	3
/logout	2
/reset-password	6
TOTAL	18 escenarios BDD