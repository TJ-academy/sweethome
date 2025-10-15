package com.example.sweethome.home;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 숙소 옵션(SiteOption 테이블) 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 모든 옵션 목록을 조회하는 기능을 제공
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OptionService {

    // Option 엔티티를 관리하는 Repository 주입
    // 이 Option 엔티티는 site_option 테이블에 매핑
    private final OptionRepository optionRepository; 

    /**
     * DB에 저장된 모든 숙소 옵션 목록을 조회
     * @return List<Option> 모든 옵션 엔티티 목록
     */
    public List<Option> getAllOptions() {
        // JpaRepository의 findAll() 메서드를 사용하여 모든 옵션을 가져옴
        return optionRepository.findAll();
    }
}
