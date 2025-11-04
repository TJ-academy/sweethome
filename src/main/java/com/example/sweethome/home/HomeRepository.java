package com.example.sweethome.home;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sweethome.user.User;

public interface HomeRepository extends JpaRepository<Home, Integer> {
	
	//성능개선 추가
	@Query("SELECT h, COUNT(w) FROM Home h LEFT JOIN Wishlist w ON h.idx = w.home.idx GROUP BY h.idx, h")
    List<Object[]> findAllHomesWithLikeCounts(); 
	
	List<Home> findByLocationContainingIgnoreCase(String keyword);
	
	// 호스트(User 엔티티)를 기준으로 숙소 목록을 조회하는 메서드
    List<Home> findByHost(User host);
    
    //검색
    List<Home> findByLocationContainingIgnoreCaseAndMaxPeopleGreaterThanEqual(String locationKeyword, int maxPeople);
    
    //단체숙소 조회
    List<Home> findByMaxPeopleGreaterThanEqual(int maxPeople);
    
 // Fetch Join을 사용하여 Home과 Host를 한 번에 가져옵니다.
    @Query("SELECT h FROM Home h JOIN FETCH h.host")
    List<Home> findAllHomesWithHost();

    @Query("SELECT h FROM Home h JOIN FETCH h.host WHERE h.location LIKE CONCAT('%', :location, '%')")
    List<Home> findByLocationContainingIgnoreCaseWithHost(@Param("location") String location);
    
    // HomeRepository.java (인터페이스)
    @Query("SELECT h FROM Home h "
         + "JOIN FETCH h.host "              // Host 정보 Eager 로딩
         + "LEFT JOIN FETCH h.homePhoto "    // HomePhoto 정보 Eager 로딩
         + "LEFT JOIN FETCH h.hashtag "      // Hashtag 정보 Eager 로딩 (Hashtag 엔티티가 Home과 OneToOne/ManyToOne 관계라고 가정)
         + "WHERE h.idx = :idx")
    Optional<Home> findByIdWithAll(@Param("idx") int idx);
    
    //해시태그 조인검색
    @Query("""
        SELECT h 
        FROM Home h 
        JOIN h.hashtag tag
        WHERE LOWER(h.location) LIKE LOWER(CONCAT('%', :keyword, '%'))
        AND h.maxPeople >= :maxPeople
        AND (:wifi IS NULL OR tag.wifi = true)
        AND (:tv IS NULL OR tag.tv = true)
        AND (:kitchen IS NULL OR tag.kitchen = true)
        AND (:freePark IS NULL OR tag.freePark = true)
        AND (:selfCheckin IS NULL OR tag.selfCheckin = true)
        AND (:coldWarm IS NULL OR tag.coldWarm = true)
        AND (:petFriendly IS NULL OR tag.petFriendly = true)
        AND (:barrierFree IS NULL OR tag.barrierFree = true)
        AND (:elevator IS NULL OR tag.elevator = true)
        """)
    List<Home> searchHomesByHashtagFilters(
            @Param("keyword") String keyword,
            @Param("maxPeople") int maxPeople,
            @Param("wifi") Boolean wifi,
            @Param("tv") Boolean tv,
            @Param("kitchen") Boolean kitchen,
            @Param("freePark") Boolean freePark,
            @Param("selfCheckin") Boolean selfCheckin,
            @Param("coldWarm") Boolean coldWarm,
            @Param("petFriendly") Boolean petFriendly,
            @Param("barrierFree") Boolean barrierFree,
            @Param("elevator") Boolean elevator
    );

}

