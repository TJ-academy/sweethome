package com.example.sweethome.config; 

import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement; // 🚩 이 import를 추가해야 합니다.
import org.apache.coyote.http11.AbstractHttp11Protocol;


@Configuration
public class MultipartConfig {

    // 🚩 1. MultipartConfigElement 빈을 직접 등록하여 파트 개수 제한을 설정합니다.
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        // maxFileSize와 maxRequestSize는 properties에서 설정한 값을 사용합니다.
        // location은 임시 저장 경로입니다 (선택 사항).
        // maxParts는 MultipartConfigElement의 생성자로는 직접 설정할 수 없습니다.

        // 따라서 maxFileSize와 maxRequestSize만 지정하고, 
        // Tomcat ContextCustomizer 설정을 유지하여 MAX_PARTS를 늘립니다.
        return new MultipartConfigElement("", 
                                          100 * 1024 * 1024L, // maxFileSize (100MB)
                                          150 * 1024 * 1024L, // maxRequestSize (150MB)
                                          0); // fileSizeThreshold
    }


    // StandardServletMultipartResolver 빈은 그대로 유지합니다.
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
    
    // 🚩 2. Tomcat Customizer는 MAX_PARTS를 강제로 주입하기 위해 유지합니다.
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            if (factory instanceof TomcatServletWebServerFactory tomcat) {
                // URI 인코딩 설정
                tomcat.setUriEncoding(java.nio.charset.StandardCharsets.UTF_8); 

                tomcat.addConnectorCustomizers(connector -> {
                    connector.setMaxPostSize(1024 * 1024 * 200); // 200MB

                    if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol<?> handler) {
                        handler.setMaxHttpHeaderSize(1024 * 1024); // 1MB
                    }
                });

                // 폼/파일 파트 개수 제한 (FileCountLimitExceededException 해결 코드)
                tomcat.addContextCustomizers(context -> {
                    // 이 설정이 작동하지 않는다면 Tomcat 10+ 환경에서 문제가 발생할 수 있습니다.
                    context.addParameter("org.apache.catalina.MAX_FORM_POST_PARAMETERS", "500");
                    context.addParameter("org.apache.catalina.connector.MAX_PARTS", "500"); 
                });
            }
        };
    }
}