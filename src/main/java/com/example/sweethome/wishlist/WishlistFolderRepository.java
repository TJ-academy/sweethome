package com.example.sweethome.wishlist;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sweethome.user.User;

public interface WishlistFolderRepository extends JpaRepository<WishlistFolder, Long> {
    // 유저별 폴더를 조회하는 쿼리 메서드 등 추가
	
	// User 엔티티를 기준으로 해당 사용자의 모든 폴더를 조회하는 메서드 추가
    List<WishlistFolder> findByUser(User user);
    
    
}