package com.kyut.ordo.dsl;

import dev.kyut.dsl.loader.RuleSetManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrdoDslRulesBootstrap {
    private final RuleSetManager ruleSetManager;
    private final ResourceLoader resourceLoader;

    @Value("${ordo.dsl.enabled:true}")
    private boolean enabled;

    @Value("${ordo.dsl.rules-path:classpath:rules/ordo-rules.dsl}")
    private String rulesPath;

    @PostConstruct
    void loadRules() {
        if (!enabled) {
            log.info("Ordo DSL is disabled by configuration.");
            return;
        }

        Resource resource = resourceLoader.getResource(rulesPath);
        if (!resource.exists()) {
            throw new IllegalStateException("DSL rules file does not exist: " + rulesPath);
        }

        String dslSource = readResource(resource);
        ruleSetManager.loadAndReplace(dslSource);
        log.info("Loaded DSL rules from {}", rulesPath);
    }

    private String readResource(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read DSL rules resource: " + resource.getDescription(), e);
        }
    }
}
