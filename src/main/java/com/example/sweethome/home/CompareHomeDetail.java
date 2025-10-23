package com.example.sweethome.home; // 👈 DTO를 위한 별도 패키지 경로로 가정합니다.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 추가
public class CompareHomeDetail {
    private int idx;
    private String title;
    private String thumbnail;
    private int costBasic;
    private int costExpen;
    private int maxPeople;
    private int room;
    private Integer bath;
    private Integer bed;

    private Long likeCount; // 좋아요 개수

    // 옵션 그룹별 옵션 목록 (예: "욕실" -> ["헤어드라이어", "샴푸"])
    private Map<String, List<String>> groupedOptions; 
}