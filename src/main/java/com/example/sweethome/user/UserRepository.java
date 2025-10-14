package com.example.sweethome.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
	Optional<User> findByEmail(String email);
	boolean existsByEmail(String email);
	boolean existsByNickname(String nickname);
	void deleteByEmail(String email);
}