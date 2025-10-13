package com.example.sweethome.user;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {
	Optional<User> findByEmail(String email);
//	
//	@Query(value = "select * from user "
//			+ "where email = :email and "
//			+ "password = STANDARD_HASH(:password, 'SHA256') "
//			+ "fetch first 1 rows only", nativeQuery = true)
//	Optional<User> login(@Param("email") String email, 
//			@Param("password") String password);
//	
//	@Modifying
//	@Query(value = "insert into user "
//			+ "(email, password, username, nickname, profileImg, phone, birthday, joinAt) values "
//			+ "(:email, STANDARD_HASH(:password, 'SHA256'), :username, :nickname, :profileImg, :phone, :birthday, now()) "
//			, nativeQuery = true)
//	int join(@Param("email") String email, 
//			@Param("password") String password, 
//			@Param("username") String username,
//			@Param("nickname") String nickname, 
//			@Param("profileImg") String profileImg, 
//			@Param("phone") String phone,
//			@Param("birthday") Date birthday);
}