package com.sixgroup.refit.ejemplo.utils;

public final class AdminApiPaths {

    private AdminApiPaths() {
        // Evita instanciación
    }

    public static final String BASE = "/api/v1/admin";
    // -------- CREACIÓN --------
    public static final String CREATE_USER = BASE + "/create-user";
    // -------- LISTADOS --------
    public static final String USERS = BASE + "/users";
    public static final String LOCKED_USERS = BASE + "/locked-users";
    // -------- CONSULTAS --------
    public static final String USER_STATUS = BASE + "/user-status";
    // -------- ACCIONES --------
    public static final String UNLOCK_USER = BASE + "/unlock";
    public static final String UPDATE_ROLE = BASE + "/update-role";
    public static final String DELETE_USER = BASE + "/delete";
}
