package com.kyut.ordo.dsl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DslRulesBootstrap {
    private final DslLifecycleService dslLifecycleService;

    @Value("${ordo.dsl.enabled:true}")
    private boolean enabled;

    @PostConstruct
    void loadRules() {
        if (!enabled) {
            log.info("Ordo DSL is disabled by configuration.");
            return;
        }

        var loaded = dslLifecycleService.reloadRules();
        log.info("Loaded {} DSL rules on startup", loaded.rules().size());
    }
}
