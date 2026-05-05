package com.kyut.ordo.dsl;

import dev.kyut.dsl.core.RuleSet;
import dev.kyut.dsl.loader.RuleSetManager;
import dev.kyut.dsl.registry.RuleRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class DslLifecycleService {
    private final RuleSetManager ruleSetManager;
    private final RuleRegistry ruleRegistry;

    @Value("${ordo.dsl.enabled:true}")
    private boolean enabled;

    @Value("${ordo.dsl.rules-path:classpath:rules/ordo-rules.dsl}")
    private String rulesPath;

    private final AtomicReference<Instant> lastReloadAt = new AtomicReference<>();

    public RuleSet reloadRules() {
        RuleSet ruleSet = loadRuleSetByPath(rulesPath);
        lastReloadAt.set(Instant.now());
        return ruleSet;
    }

    public DslStatus status() {
        List<String> ruleNames = ruleRegistry.getRules().stream()
                .map(rule -> rule.name())
                .toList();
        return new DslStatus(
                enabled,
                rulesPath,
                ruleNames.size(),
                ruleNames,
                lastReloadAt.get()
        );
    }

    private RuleSet loadRuleSetByPath(String path) {
        if (path.startsWith("classpath:")) {
            return ruleSetManager.loadAndReplaceFromClasspath(path);
        }
        return ruleSetManager.loadAndReplaceFromFile(Path.of(path));
    }

    public record DslStatus(
            boolean enabled,
            String rulesPath,
            int loadedRuleCount,
            List<String> loadedRuleNames,
            Instant lastReloadAt
    ) {
    }
}
