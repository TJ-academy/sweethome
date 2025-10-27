package com.example.sweethome.user;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
	private String email;
	private String password;
	private String username;
	private String nickname;
	private String phone;
    private LocalDate birthday;
    
	// 프로필 관련
    private MultipartFile profileImgFile; // 업로드 파일
    private String kakaoProfileUrl;       // 카카오에서 제공된 URL

    // 카카오 여부
    private boolean kakaoUser = false;
    
	// ✅ DTO → Entity 변환 메서드
    public User toEntity() {
        return User.builder()
                .email(this.email)
                .password(this.password) // 서비스단에서 암호화 예정
                .username(this.username)
                .nickname(this.nickname)
                .phone(this.phone)
                .birthday(this.birthday)
                .build();
    }
}
