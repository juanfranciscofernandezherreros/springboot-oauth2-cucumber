package com.sixgroup.refit.ejemplo.config;

import io.restassured.RestAssured;
import org.springframework.boot.test.web.server.LocalServerPort;

public abstract class BaseRestConfig {

    @LocalServerPort
    protected int port;

    protected void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }
}