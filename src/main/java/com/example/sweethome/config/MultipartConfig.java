package com.example.sweethome.config;

import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

@Configuration
public class MultipartConfig {

    // 1️⃣ MultipartConfigElement 빈 설정 (파일 크기 제한)
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofMegabytes(100));     // 파일 최대 100MB
        factory.setMaxRequestSize(DataSize.ofMegabytes(150));  // 요청 최대 150MB
        factory.setFileSizeThreshold(DataSize.ofMegabytes(1)); // 메모리 임계값
        return factory.createMultipartConfig();
    }

    // 2️⃣ MultipartResolver 빈
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    // 3️⃣ Tomcat Customizer
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            if (factory instanceof TomcatServletWebServerFactory tomcat) {

                // URI 인코딩
                tomcat.setUriEncoding(java.nio.charset.StandardCharsets.UTF_8);

                tomcat.addConnectorCustomizers(connector -> {
                    // POST 요청 최대 크기
                    connector.setMaxPostSize(1024 * 1024 * 200); // 200MB

                    // Tomcat 10+ 환경에서 파라미터 수/파트 수 제한 증가
                    connector.setProperty("maxParameterCount", "500");
                   // connector.setProperty("maxParts", "-1");
                    // HTTP Header 최대 크기
                    if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol<?> handler) {
                        handler.setMaxHttpHeaderSize(1024 * 1024); // 1MB
                    }
                });
            }
        };
    }
}
