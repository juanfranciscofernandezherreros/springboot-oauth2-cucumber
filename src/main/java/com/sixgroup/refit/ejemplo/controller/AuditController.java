package com.sixgroup.refit.ejemplo.controller;

import com.sixgroup.refit.ejemplo.dto.AuditLogDto;
import com.sixgroup.refit.ejemplo.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogDto>> getFullHistory() {
        return ResponseEntity.ok(auditService.getGlobalAuditHistory());
    }
}
