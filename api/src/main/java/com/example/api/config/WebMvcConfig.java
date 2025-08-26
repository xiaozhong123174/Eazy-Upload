package com.example.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	@Value("${upload.base-dir:uploads}")
	private String uploadBaseDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path basePath = Paths.get(uploadBaseDir).toAbsolutePath().normalize();
		String location = basePath.toUri().toString();
		registry.addResourceHandler("/files/**")
				.addResourceLocations(location);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins("http://localhost:5173")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*")
				.allowCredentials(false)
				.maxAge(3600);
		registry.addMapping("/files/**")
				.allowedOrigins("http://localhost:5173")
				.allowedMethods("GET", "HEAD", "OPTIONS")
				.allowedHeaders("*")
				.allowCredentials(false)
				.maxAge(3600);
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.favorParameter(false)
				.favorPathExtension(true)
				.mediaType("docx", org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
				.mediaType("xlsx", org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.mediaType("pptx", org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"));
	}
} 