package com.example.sweethome.home;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sweethome.user.User;

public interface HomeRepository extends JpaRepository<Home, Integer> {
	List<Home> findByLocationContainingIgnoreCase(String keyword);
	
	// 호스트(User 엔티티)를 기준으로 숙소 목록을 조회하는 메서드
    List<Home> findByHost(User host);
    
    //검색
    List<Home> findByLocationContainingIgnoreCaseAndMaxPeopleGreaterThanEqual(String locationKeyword, int maxPeople);
}
