package com.example.sweethome.mypage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.sweethome.user.UserRepository;

@Service
public class MypageService {
	@Autowired
	UserRepository userRepo;
	
	@Transactional
	public void deleteUserByEmail(String email) {
		userRepo.deleteByEmail(email);
	}
}