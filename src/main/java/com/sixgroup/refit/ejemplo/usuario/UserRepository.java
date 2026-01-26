package com.sixgroup.refit.ejemplo.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su email.
     * Retorna un Optional para manejar de forma segura si el usuario no existe.
     */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}