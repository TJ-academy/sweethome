package com.example.sweethome.wishlist;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.sweethome.home.Home;
import com.example.sweethome.user.User;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
	// 유저별, 폴더별로 위시리스트를 조회하는 쿼리 메서드 등 추가

	/**
	 * 특정 숙소(Home)에 대해 특정 유저(User)가 좋아요를 이미 눌렀는지 확인
	 * 
	 * @param home 좋아요 대상 숙소
	 * @param user 좋아요를 누른 유저
	 * @return 존재하면 true, 아니면 false
	 */
	boolean existsByHomeAndUser(Home home, User user);

	/**
	 * 특정 폴더(WishlistFolder)에 저장된 모든 위시리스트 항목을 조회합니다.
	 * 
	 * @param folder 조회 대상 폴더
	 * @return 해당 폴더에 속한 Wishlist 목록
	 */
	List<Wishlist> findByFolder(WishlistFolder folder);

	/**
	 * 특정 폴더에 속한 Wishlist 목록을 조회하며, 연결된 Home 엔티티를 즉시(Eagerly) 가져옵니다.
	 * 
	 * @param folder 조회 대상 폴더
	 * @return 해당 폴더에 속한 Wishlist 목록 (Home이 로드된 상태)
	 */
	@Query("SELECT w FROM Wishlist w JOIN FETCH w.home WHERE w.folder = :folder")
	List<Wishlist> findByFolderWithHome(@Param("folder") WishlistFolder folder);

	/**
	 * 특정 유저의 모든 폴더별 위시리스트 항목 개수를 조회합니다. 반환 값: List<Object[]> -> [folderIdx, count]
	 */
	@Query("SELECT w.folder.idx, COUNT(w) FROM Wishlist w WHERE w.user = :user GROUP BY w.folder.idx")
	List<Object[]> countWishlistsByFolderByUser(@Param("user") User user);
	
	/**
     * 특정 숙소와 유저에 해당하는 Wishlist 항목을 삭제합니다.
     * @param home 좋아요 대상 숙소
     * @param user 좋아요를 누른 유저
     * @return 삭제된 항목의 개수 (보통 1 또는 0)
     */
    @Transactional // 삭제는 트랜잭션이 필요합니다.
    long deleteByHomeAndUser(Home home, User user);
    
    @Query("SELECT w.home.idx, COUNT(w) FROM Wishlist w GROUP BY w.home.idx")
    List<Object[]> countWishlistsByHome();
    
    /**
     * ✅ 특정 숙소 ID 리스트에 대한 위시리스트 항목 개수를 조회합니다. (숙소 비교용)
     * @param homeIds 조회할 Home의 Integer 타입 ID 리스트
     * @return List<Object[]> -> [home_idx, count]
     */
    @Query("SELECT w.home.idx, COUNT(w) FROM Wishlist w WHERE w.home.idx IN :homeIds GROUP BY w.home.idx")
    List<Object[]> countWishlistsByHomeIds(@Param("homeIds") List<Integer> homeIds);
}