package com.example.sweethome.home;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationOptionRepository extends JpaRepository<AccommodationOption, AccommodationOptionId> {
	
	// 특정 Home 엔티티(숙소)에 연결된 모든 AccommodationOption 목록을 조회합니다.
    // AccommodationOption 엔티티의 'home' 필드를 기준으로 검색합니다.
    List<AccommodationOption> findByHome(Home home);
    
    /**
     * ✅ 숙소 ID 리스트를 받아 해당 숙소들의 옵션 정보를 한번에 조회합니다.
     * FETCH JOIN을 사용하여 Home과 Option 엔티티를 즉시 로딩합니다.
     * * @param homeIds 조회할 Home의 Integer 타입 ID 리스트
     * @return 조회된 AccommodationOption 리스트
     */
    @Query("SELECT ao FROM AccommodationOption ao JOIN FETCH ao.home h JOIN FETCH ao.option o WHERE h.idx IN :homeIds")
    List<AccommodationOption> findOptionsByHomeIds(@Param("homeIds") List<Integer> homeIds);
}