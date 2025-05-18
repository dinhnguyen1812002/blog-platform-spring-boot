package com.Nguyen.blogplatform.docs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class ApiDoc implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private OpenAPI openAPI;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        var yamlMapper = new ObjectMapper(new YAMLFactory());
        try {
            yamlMapper.writeValue(new File("build/apiDoc.yaml"), openAPI);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
