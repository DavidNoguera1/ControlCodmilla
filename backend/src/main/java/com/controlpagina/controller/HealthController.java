package com.controlpagina.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new HashMap<>();
        body.put("status", "UP");

        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(3);
            body.put("database", valid ? "connected" : "disconnected");
            if (!valid) {
                body.put("status", "DEGRADED");
                return ResponseEntity.status(503).body(body);
            }
        } catch (Exception e) {
            body.put("database", "disconnected");
            body.put("error", e.getMessage());
            body.put("status", "DOWN");
            return ResponseEntity.status(503).body(body);
        }

        return ResponseEntity.ok(body);
    }
}
