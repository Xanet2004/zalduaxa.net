package net.zalduaxa.backend.webConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/storage/**")
                .addResourceLocations("file:C:/Users/xanet/SynologyDrive/Work/Personal/html/y/zalduaxa.net/storage/");
    }
}