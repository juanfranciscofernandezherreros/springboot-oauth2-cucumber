package com.sixgroup.refit.ejemplo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invitations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "token"),
                @UniqueConstraint(columnNames = {"email", "status"})
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Invitation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    private Instant createdAt;
    private Instant expiresAt;
}
