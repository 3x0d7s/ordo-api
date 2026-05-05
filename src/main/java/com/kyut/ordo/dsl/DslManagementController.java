package com.kyut.ordo.dsl;

import dev.kyut.dsl.loader.RuleSetValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal/dsl")
@RequiredArgsConstructor
public class DslManagementController {
    private final DslLifecycleService dslLifecycleService;

    @GetMapping("/status")
    public ResponseEntity<DslLifecycleService.DslStatus> status() {
        return ResponseEntity.ok(dslLifecycleService.status());
    }

    @PostMapping("/reload")
    public ResponseEntity<DslLifecycleService.DslStatus> reload() {
        dslLifecycleService.reloadRules();
        return ResponseEntity.ok(dslLifecycleService.status());
    }

    @ExceptionHandler(RuleSetValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationError(RuleSetValidationException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "DSL_RULESET_VALIDATION_FAILED",
                "message", ex.getMessage()
        ));
    }
}
