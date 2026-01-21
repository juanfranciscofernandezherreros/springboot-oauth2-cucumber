package com.sixgroup.refit.ejemplo.config;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Objects;

public class RedisCleanerHook {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Se ejecuta ANTES de cualquier Scenario.
    // Esto garantiza que, aunque el test anterior haya bloqueado la IP,
    // este test empieza con la memoria limpia.
    @Before
    public void limpiarRedisAntesDelTest() {
        if (redisTemplate != null) {
            System.out.println("🧹 [HOOK] Limpiando Redis para inicio fresco...");
            Objects.requireNonNull(redisTemplate.getConnectionFactory())
                    .getConnection()
                    .serverCommands()
                    .flushAll();
        }
    }

    // Opcional: Limpiar también al terminar por si acaso
    @After
    public void limpiarRedisDespuesDelTest() {
        if (redisTemplate != null) {
            Objects.requireNonNull(redisTemplate.getConnectionFactory())
                    .getConnection()
                    .serverCommands()
                    .flushAll();
        }
    }
}