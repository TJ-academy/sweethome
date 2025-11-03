package com.example.sweethome.ssong;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	//private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    //private final long expiration = 1000 * 60 * 60; // 1시간
	
	//application.properties에 설정된 값
    //"THIS-IS-A-VERY-LONG-SECRET-KEY-AT-LEAST-32-BYTES-123456"
	@Value("${app.jwt.secret}")
	private String secret;
	
    //sweethome  토큰 발행자
	@Value("${app.jwt.issuer}")
	private String issuer;
	
	//3600초 = 1시간
	@Value("${app.jwt.valid-seconds}")
	private long validSeconds;


	// JWT 서명에 사용할 비밀키(SecretKey)를 생성
 	private Key key() {
 		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
 	}

 	//토큰 생성
    public String generateToken(String email) {
    	long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .setIssuer(issuer)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + validSeconds * 1000)) //토큰 만료시간 설정
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

	// 토큰을 분석하고 검증
    // 사용자 식별하는 식별자이기도 함.
    public Jws<Claims> validateTokenAndGetEmail(String token) {
        try {
        	return Jwts.parserBuilder()
        			.setSigningKey(key())
					.build()
					.parseClaimsJws(token);
        } catch (JwtException e) {
            return null;
        }
    }
}