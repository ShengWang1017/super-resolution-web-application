package com.ws.common.config;

import com.ws.common.utils.FileUpload;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OSSConfig.class)
public class CommonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FileUpload fileUpload() {
        return new FileUpload();
    }
} 