package com.example.sweethome.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-chat").setAllowedOriginPatterns("*").withSockJS();   //chatList 접속 주소
		registry.addEndpoint("/ws-notify").setAllowedOriginPatterns("*").withSockJS();   //header 접속 주소
		registry.addEndpoint("/ws-flutter").setAllowedOriginPatterns("*").withSockJS();  //플러터 채팅 접속 주소
	}
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/topic");   //서버 => 클라이언트 전송 주소
		registry.setApplicationDestinationPrefixes("/app");   //클라이언트 => 서버 전송 주소
	}
}