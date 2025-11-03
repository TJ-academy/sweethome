package com.example.sweethome.ssong;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserFlutterDTO {
	@NotBlank(groups = {Login.class, Join.class})  //blank 허용X "" null이랑 다름.
	//로그인폼, 회원가입폼에서 아이디 blank 체크
	private String email;
	
	@NotBlank(groups = {Login.class, Join.class})
	private String password;
	
	private String nickname;
	private String profileImg;
	
	public interface Login {  //로그인 검증 그룹
	}
	
	public interface Join {}  //회원가입 검증 그룹
}