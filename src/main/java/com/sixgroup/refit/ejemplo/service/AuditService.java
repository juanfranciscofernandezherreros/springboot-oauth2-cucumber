package com.sixgroup.refit.ejemplo.service;

import com.sixgroup.refit.ejemplo.dto.AuditLogDto;
import com.sixgroup.refit.ejemplo.model.Invitation;
import com.sixgroup.refit.ejemplo.model.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final EntityManager entityManager;

    public List<AuditLogDto> getGlobalAuditHistory() {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<AuditLogDto> history = new ArrayList<>();

        // Entidades que queremos monitorizar
        List<Class<?>> monitoredEntities = List.of(User.class, Invitation.class);

        for (Class<?> entityClass : monitoredEntities) {
            // false = queremos la entidad completa, no solo el ID
            // true = incluimos las revisiones donde se borró la entidad
            List<Object[]> results = reader.createQuery()
                    .forRevisionsOfEntity(entityClass, false, true)
                    .getResultList();

            for (Object[] row : results) {
                Object entity = row[0];
                DefaultRevisionEntity revEntity = (DefaultRevisionEntity) row[1];
                RevisionType type = (RevisionType) row[2];

                history.add(AuditLogDto.builder()
                        .revInfo("REV-" + revEntity.getId())
                        .timestamp(revEntity.getRevisionDate().toString())
                        .author("admin") // Si tienes CustomRevisionEntity, saca el autor aquí
                        .operation(type.name())
                        .entityName(entityClass.getSimpleName())
                        .description(extractIdentifier(entity))
                        .snapshot(entity)
                        .build());
            }
        }

        // Ordenar: lo más nuevo arriba
        return history.stream()
                .sorted(Comparator.comparing(AuditLogDto::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    private String extractIdentifier(Object entity) {
        if (entity instanceof User u) return "User: " + u.getEmail();
        if (entity instanceof Invitation i) return "Inv: " + i.getEmail();
        return "ID: " + entity.hashCode();
    }

}
