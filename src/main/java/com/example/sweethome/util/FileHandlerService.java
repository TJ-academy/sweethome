package com.example.sweethome.util;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 처리를 위한 인터페이스를 정의합니다.
 * 이를 통해 HomeService는 실제 구현체(LocalFileHandlerService 등)에 의존하지 않고
 * 파일 처리 기능을 주입받아 사용할 수 있습니다.
 */
public interface FileHandlerService {
    /**
     * MultipartFile을 저장하고, DB에 저장할 상대 경로를 반환합니다.
     * @param file 저장할 파일
     * @return 저장된 파일의 DB에 저장할 경로 (예: /img/home/파일명)
     */
    String saveFile(MultipartFile file);
}
