package com.example.sweethome.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class LocalFileHandlerService implements FileHandlerService {

    // [중요 수정] 파일을 저장할 물리적인 절대 경로를 지정합니다. 
    // 이 폴더가 존재하지 않아도, 아래 mkdirs() 로직이 자동으로 생성합니다.
    // 윈도우 환경을 가정하여 C: 드라이브 내에 안전한 폴더를 지정합니다.
    private static final String ABSOLUTE_UPLOAD_PATH = "C:/sweethome_upload/img/home/";
    
    // DB에 저장하고 웹에서 접근할 URL 경로의 접두사
    private static final String WEB_ACCESS_PREFIX = "/img/home/";

    @Override
    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedFilename = UUID.randomUUID().toString() + extension;

            // 파일이 저장될 최종 물리적 경로 (ABSOLUTE_UPLOAD_PATH + savedFilename)
            Path absolutePath = Paths.get(ABSOLUTE_UPLOAD_PATH, savedFilename);
            File targetFile = absolutePath.toFile();

            log.info("파일 저장 시도 경로: {}", targetFile.getAbsolutePath());

            // 저장 경로의 부모 디렉토리가 존재하는지 확인하고, 없으면 생성합니다. (C:/sweethome_upload/img/home/)
            File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (created) {
                    log.info("파일 저장 디렉토리 생성 성공: {}", parentDir.getAbsolutePath());
                } else {
                    log.error("파일 저장 디렉토리 생성 실패: {}", parentDir.getAbsolutePath());
                    throw new IOException("디렉토리 생성에 실패했습니다. 권한 및 경로를 확인하세요.");
                }
            }

            // 파일 저장 (이전의 FileNotFoundException을 해결)
            file.transferTo(targetFile);
            
            // DB에 저장할 상대 경로 (웹 접근 경로) 반환: /img/home/파일명
            return WEB_ACCESS_PREFIX + savedFilename;

        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생", e);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
