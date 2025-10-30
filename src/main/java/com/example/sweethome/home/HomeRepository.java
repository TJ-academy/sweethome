package com.example.sweethome.home;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}

