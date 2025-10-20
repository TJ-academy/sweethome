package com.example.sweethome.home;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationOptionRepository extends JpaRepository<AccommodationOption, AccommodationOptionId> {
	
	// 특정 Home 엔티티(숙소)에 연결된 모든 AccommodationOption 목록을 조회합니다.
    // AccommodationOption 엔티티의 'home' 필드를 기준으로 검색합니다.
    List<AccommodationOption> findByHome(Home home);
    
}
