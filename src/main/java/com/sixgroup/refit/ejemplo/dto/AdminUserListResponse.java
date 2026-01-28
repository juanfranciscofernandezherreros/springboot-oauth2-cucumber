package com.sixgroup.refit.ejemplo.dto;

import com.sixgroup.refit.ejemplo.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;

    private boolean accountNonLocked;
    private int failedAttempt;
}

