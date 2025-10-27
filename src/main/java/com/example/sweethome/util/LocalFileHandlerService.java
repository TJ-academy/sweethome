package com.example.sweethome.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LocalFileHandlerService implements FileHandlerService {

    // [최종 경로] src/main/resources/static/img/home/ 경로를 현재 작업 디렉터리 기준으로 지정합니다.
    //private static final String RELATIVE_UPLOAD_DIR = "src/main/resources/static/img/home";
    
    // 웹 접근 경로는 static 폴더 아래의 경로를 사용하므로 "/img/home/"이 됩니다.
    //private static final String WEB_ACCESS_PREFIX = "/img/home/";
    
    private static final String STATIC_ROOT = "src/main/resources/static/img";

    @Override
    public String saveFile(MultipartFile file, 
    		String subDir) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 1. 파일 이름 생성 (UUID를 사용하여 파일명 충돌 방지)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedFilename = UUID.randomUUID().toString() + extension;

            // 2. 저장 디렉토리의 절대 경로 계산 및 생성
            // 상대 경로를 현재 시스템의 절대 경로로 변환하여 사용합니다.
            Path absoluteUploadPath = Paths.get(STATIC_ROOT, subDir).toAbsolutePath().normalize();
            File targetDir = absoluteUploadPath.toFile();

            log.info("파일 저장 시도 절대 경로 (Target Dir): {}", targetDir.getAbsolutePath());

            // 디렉토리가 없으면 생성 (src/main/resources/static/img/"subDir"이 없으면 만들어 줍니다.)
            if (!targetDir.exists()) {
                boolean created = targetDir.mkdirs();
                if (created) {
                    log.info("파일 저장 디렉토리 생성 성공: {}", targetDir.getAbsolutePath());
                } else {
                    log.error("파일 저장 디렉토리 생성 실패: {}", targetDir.getAbsolutePath());
                    throw new IOException("디렉토리 생성에 실패했습니다. 경로/권한을 확인하세요.");
                }
            }

            // 3. 최종 파일 경로 (Path 객체)
            Path targetFilePath = Paths.get(targetDir.getAbsolutePath(), savedFilename);
            
            // 4. 파일 복사 (Files.copy() 사용)
            // MultipartFile의 입력 스트림을 우리가 지정한 절대 경로로 직접 복사합니다.
            // (이것이 Tomcat의 transferTo 오류를 해결하는 핵심 방식입니다.)
            Files.copy(file.getInputStream(), targetFilePath);
            
            log.info("파일 저장 성공: {}", targetFilePath.toAbsolutePath());
            
            // 5. 웹 접근 경로 반환 (DB에 저장)
            return "/img/" + subDir + "/" + savedFilename;

        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생:", e);
            // 사용자에게 정확한 오류 메시지를 전달하기 위해 RuntimeException으로 변환하여 던집니다.
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String saveFile(MultipartFile file) {
        return saveFile(file, "home");
    }
    
    @Override
    public String saveChatImage(MultipartFile file, String subDir) {
    	return saveFile(file, "chat/" + subDir);
    }
}