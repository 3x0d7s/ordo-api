package com.kyut.ordo.dsl;

import dev.kyut.dsl.spring.BusinessLogicDslConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(BusinessLogicDslConfiguration.class)
public class DslEngineConfiguration {
}