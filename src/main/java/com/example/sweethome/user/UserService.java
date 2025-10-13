package com.example.sweethome.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

@Service
public class UserService {
	private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //회원가입 함수
    public void insertUser(User user) {
        // 비밀번호 암호화
        String encryptedPassword = encryptPassword(user.getPassword());
        user.setPassword(encryptedPassword);
        
        // 회원가입
        userRepository.save(user);
    }
    
    //로그인 함수
    public boolean loginUser(String email, String rawPwd) {
    	Optional<User> user = userRepository.findByEmail(email);
    	
    	if(user.get() == null) return false;
        
    	String encryptedPassword = encryptPassword(rawPwd);
        return user.get().getPassword().equals(encryptedPassword);
    }

    //비밀번호 암호화 함수
    private String encryptPassword(String password) {
        // SHA-256으로 암호화
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            // 16진수 문자열로 변환하여 반환
            return Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("암호화 실패", e);
        }
    }
}