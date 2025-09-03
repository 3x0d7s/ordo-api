package com.kyut.ordo.openApi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PageableOpenApiCustomizer implements OpenApiCustomizer {

    @Override
    public void customise(OpenAPI openApi) {
        Map<String, Schema> schemas = openApi.getComponents().getSchemas();
        if (schemas == null ) {
            return;
        }

        Schema<?> schema = schemas.get("Pageable");
        if (schema != null) {
            schema.getProperties().remove("sort");
            Schema<?> pageSchema = (Schema<?>) schema.getProperties().get("page");
            if (pageSchema != null) {
                pageSchema.setDefault(0);
            }
            Schema<?> sizeSchema = (Schema<?>) schema.getProperties().get("size");
            if (sizeSchema != null) {
                sizeSchema.setDefault(10);
            }
        }
    }

}
