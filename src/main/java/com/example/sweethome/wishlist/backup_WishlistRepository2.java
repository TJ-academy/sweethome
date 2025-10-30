/*
package com.example.sweethome.wishlist;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.sweethome.home.Home;
import com.example.sweethome.user.User;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
	// 유저별, 폴더별로 위시리스트를 조회하는 쿼리 메서드 등 추가

	boolean existsByHomeAndUser(Home home, User user);

	List<Wishlist> findByFolder(WishlistFolder folder);

	@Query("SELECT w FROM Wishlist w JOIN FETCH w.home WHERE w.folder = :folder")
	List<Wishlist> findByFolderWithHome(@Param("folder") WishlistFolder folder);

	@Query("SELECT w.folder.idx, COUNT(w) FROM Wishlist w WHERE w.user = :user GROUP BY w.folder.idx")
	List<Object[]> countWishlistsByFolderByUser(@Param("user") User user);
	
    @Transactional // 삭제는 트랜잭션이 필요합니다.
    long deleteByHomeAndUser(Home home, User user);
    
    @Query("SELECT w.home.idx, COUNT(w) FROM Wishlist w GROUP BY w.home.idx")
    List<Object[]> countWishlistsByHome();
    
    @Query("SELECT w.home.idx, COUNT(w) FROM Wishlist w WHERE w.home.idx IN :homeIds GROUP BY w.home.idx")
    List<Object[]> countWishlistsByHomeIds(@Param("homeIds") List<Integer> homeIds);
    
    @Modifying
    void deleteByHome_Idx(int homeIdx);
}
*/