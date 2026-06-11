package net.zalduaxa.backend.webConfig;

import java.net.URI;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${storage.path}")
    private String storagePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/storage/**")
                .addResourceLocations(normalizeStorageLocation(storagePath));
    }

    private String normalizeStorageLocation(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return ensureTrailingSlash(
                    Paths.get("./storage").toAbsolutePath().normalize().toUri().toString()
            );
        }

        try {
            URI uri = URI.create(storagePath);
            if (uri.getScheme() != null) {
                return ensureTrailingSlash(uri.toString());
            }
        } catch (Exception ignored) {
            // Fall back to normal filesystem path.
        }

        return ensureTrailingSlash(
                Paths.get(storagePath).toAbsolutePath().normalize().toUri().toString()
        );
    }

    private String ensureTrailingSlash(String location) {
        return location.endsWith("/") ? location : location + "/";
    }
}