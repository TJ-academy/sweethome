/*
package com.example.sweethome.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sweethome.user.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/home/*")
@RequiredArgsConstructor
public class SweetHomeController2 {

    private final HomeService homeService;

    @GetMapping("/writeOne")
    public String goWriteOne(HttpSession session) {
         if (session.getAttribute("homeWriteDTO") == null) {
             session.setAttribute("homeWriteDTO", new HomeWriteDTO());
             log.info("새 HomeWriteDTO를 세션에 초기화했습니다. (writeOne)");
         }
         return "home/writeOne";
    }
    
    @GetMapping("/writeTwo")
    public String goWriteTwo() {
         return "home/writeTwo";
    }
    
    @GetMapping("/writeThree")
    public String goWriteThree() {
    	return "home/writeThree";
    }
    
    @GetMapping("/writeFour")
    public String goWriteFour(@RequestParam(name = "homeType", required = false) String homeType, Model model) {
        model.addAttribute("selectedHomeType", homeType); 
    	return "home/writeFour";        
    }
    @PostMapping("/writeFour")
    public String handleWriteFourData(@RequestParam("homeType") String homeType,
                                    @RequestParam("address") String address,
                                    @RequestParam("detailAddress") String detailAddress,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        HomeWriteDTO homeWriteDTO = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        
        if (homeWriteDTO == null) {
            log.warn("/writeFour 접근 시 세션 DTO 없음. write 페이지로 리다이렉션");
            return "redirect:/home/writeOne"; 
        }

        try {
            homeWriteDTO.setHomeType(homeType);
            homeWriteDTO.setAddress(address); 
            homeWriteDTO.setDetailAddress(detailAddress); 
            
            log.info("WriteFour 데이터 세션 저장 완료: homeType={}, address={}", homeWriteDTO.getHomeType(), homeWriteDTO.getAddress());

        } catch (Exception e) {
            log.error("주소 데이터 처리 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "주소 데이터 처리 중 오류가 발생했습니다.");
            return "redirect:/home/writeFour"; 
        }
        session.setAttribute("homeWriteDTO", homeWriteDTO);
        
        return "redirect:/home/writeFive";
    }
    
    @GetMapping("/writeFive")
    public String showWriteFiveForm(HttpSession session, Model model) {
        HomeWriteDTO homeWriteDTO = (HomeWriteDTO) session.getAttribute("homeWriteDTO");

        if (homeWriteDTO == null) {
            log.warn("writeFive 접근 시 세션 DTO 없음. write 페이지로 리다이렉션");
            return "redirect:/home/writeOne"; // 초기 단계로 리다이렉션
        }
        model.addAttribute("homeWriteDTO", homeWriteDTO);
        return "home/writeFive";
    }
    
    @PostMapping("/writeFive")
    public String handleWriteFiveData(@ModelAttribute HomeWriteDTO updatedDto,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        
        HomeWriteDTO homeWriteDTO = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        
        if (homeWriteDTO == null) {
            log.warn("/writeFive POST 접근 시 세션 DTO 없음. writeOne 페이지로 리다이렉션");
            return "redirect:/home/writeOne"; 
        }

        try {
            // updatedDto에 담긴 값(maxPeople, room, bed, bath)을 세션 DTO에 갱신
            homeWriteDTO.setMaxPeople(updatedDto.getMaxPeople());
            homeWriteDTO.setRoom(updatedDto.getRoom());
            homeWriteDTO.setBed(updatedDto.getBed());
            homeWriteDTO.setBath(updatedDto.getBath());
            
            log.info("WriteFive 데이터 세션 저장 완료: maxPeople={}, room={}, bed={}, bath={}", 
                     homeWriteDTO.getMaxPeople(), homeWriteDTO.getRoom(), homeWriteDTO.getBed(), homeWriteDTO.getBath());

        } catch (Exception e) {
            log.error("WriteFive 데이터 처리 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "데이터 처리 중 오류가 발생했습니다.");
            return "redirect:/home/writeFive"; 
        }
        
        session.setAttribute("homeWriteDTO", homeWriteDTO);
        
        return "redirect:/home/writeSix";
    }
    
    @GetMapping("/writeSix")
    public String showWriteSixForm(HttpSession session, Model model) {
        HomeWriteDTO homeWriteDTO = (HomeWriteDTO) session.getAttribute("homeWriteDTO");

        if (homeWriteDTO == null) {
            log.warn("writeSix GET 접근 시 세션 DTO 없음. writeOne 페이지로 리다이렉션");
            return "redirect:/home/writeOne"; 
        }
        
        model.addAttribute("homeWriteDTO", homeWriteDTO);
        log.info("writeSix 단계 진입: DTO 상태 확인 (MaxPeople: {})", homeWriteDTO.getMaxPeople());
        
        return "home/writeSix"; 
    }
    
    @PostMapping("/writeSix")
    public String handleWriteSixData(HttpSession session, RedirectAttributes redirectAttributes) {
        HomeWriteDTO homeWriteDTO = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        
        if (homeWriteDTO == null) {
            log.warn("/writeSix POST 접근 시 세션 DTO 없음. writeOne 페이지로 리다이렉션");
            return "redirect:/home/writeOne"; 
        }
        
        log.info("WriteSix 단계 통과, writeSeven으로 리다이렉션");
        return "redirect:/home/writeSeven";
    }

    @GetMapping("/writeSeven")
    public String showWriteSevenForm(HttpSession session, Model model) {
        HomeWriteDTO homeWriteDTO = (HomeWriteDTO) session.getAttribute("homeWriteDTO");

        if (homeWriteDTO == null) {
            log.warn("writeSeven GET 접근 시 세션 DTO 없음. writeOne 페이지로 리다이렉션");
            return "redirect:/home/writeOne"; 
        }
        
        model.addAttribute("homeWriteDTO", homeWriteDTO);
        model.addAttribute("groupedOptions", homeService.getGroupedOptions());
        log.info("writeSeven 단계 진입: 옵션 데이터 로드 완료");
        return "home/writeSeven"; 
    }
    
    @PostMapping("/writeSeven")
    public String handleWriteSevenData(@ModelAttribute HomeWriteDTO updatedDto,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        HomeWriteDTO homeWriteDTO = (HomeWriteDTO) session.getAttribute("homeWriteDTO");

        if (homeWriteDTO == null) {
            log.warn("/writeSeven POST 접근 시 세션 DTO 없음. writeOne 페이지로 리다이렉션");
            return "redirect:/home/writeOne";
        }

        try {
            // updatedDto에 담긴 optionIds를 세션 DTO에 갱신
            homeWriteDTO.setOptionIds(updatedDto.getOptionIds());

            log.info("WriteSeven 데이터 세션 저장 완료: optionIds 수={}", 
                     homeWriteDTO.getOptionIds() != null ? homeWriteDTO.getOptionIds().size() : 0);

        } catch (Exception e) {
            log.error("WriteSeven 데이터 처리 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "편의시설 데이터 처리 중 오류가 발생했습니다.");
            return "redirect:/home/writeSeven";
        }

        session.setAttribute("homeWriteDTO", homeWriteDTO);

        return "redirect:/home/writeEight";
    }

    // 8단계 폼 화면으로 이동
    @GetMapping("/writeEight")
    public String showWriteEightForm(HttpSession session, Model model) {
        HomeWriteDTO homeWriteDTO = (HomeWriteDTO) session.getAttribute("homeWriteDTO");

        if (homeWriteDTO == null) {
            log.warn("writeEight GET 접근 시 세션 DTO 없음. writeOne 페이지로 리다이렉션");
            return "redirect:/home/writeOne"; 
        }

        model.addAttribute("homeWriteDTO", homeWriteDTO);
        log.info("writeEight 단계 진입: DTO 상태 확인 (선택된 옵션 수: {})", 
                 homeWriteDTO.getOptionIds() != null ? homeWriteDTO.getOptionIds().size() : 0);
        return "home/writeEight"; 
    }
    
    // 8단계 데이터 처리 및 9단계로 이동
    @PostMapping("/writeEight")
    public String handleWriteEightData(@RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
                                       @ModelAttribute HomeWriteDTO updatedDto,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        HomeWriteDTO homeWriteDTO = (HomeWriteDTO) session.getAttribute("homeWriteDTO");

        if (homeWriteDTO == null) {
            log.warn("/writeEight POST 접근 시 세션 DTO 없음. writeOne 페이지로 리다이렉션");
            return "redirect:/home/writeOne";
        }

        try {
            // 썸네일 파일이 선택되었고 비어있지 않은 경우
            if (thumbnail != null && !thumbnail.isEmpty()) {
                homeWriteDTO.setThumbnail(thumbnail);
                log.info("WriteEight 데이터 세션 저장 완료: 썸네일 파일명={}", thumbnail.getOriginalFilename());
            } else {
                log.warn("WriteEight POST 요청에 썸네일 파일이 누락되었습니다.");
                redirectAttributes.addFlashAttribute("error", "대표 사진(썸네일)을 등록해주세요.");
                return "redirect:/home/writeEight";
            }

        } catch (Exception e) {
            log.error("WriteEight 데이터 처리 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "썸네일 업로드 중 오류가 발생했습니다.");
            return "redirect:/home/writeEight";
        }

        session.setAttribute("homeWriteDTO", homeWriteDTO);

        // 9단계로 리다이렉션
        return "redirect:/home/writeNine";
    }

    // 9단계 폼 화면으로 이동
    @GetMapping("/writeNine")
    public String showWriteNineForm(HttpSession session, Model model) {
        HomeWriteDTO homeWriteDTO = (HomeWriteDTO) session.getAttribute("homeWriteDTO");

        if (homeWriteDTO == null) {
            log.warn("writeNine GET 접근 시 세션 DTO 없음. writeOne 페이지로 리다이렉션");
            return "redirect:/home/writeOne"; 
        }

        model.addAttribute("homeWriteDTO", homeWriteDTO);
        log.info("writeNine 단계 진입: 숙소 사진 등록");
        return "home/writeNine"; 
    }

    // ⭐ 9단계 데이터 처리 및 10단계로 이동 (다중 파일 처리)
    @PostMapping("/writeNine")
    public String handleWriteNineData(@RequestParam(value = "homeImages", required = false) List<MultipartFile> homeImages,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        
        HomeWriteDTO homeWriteDTO = (HomeWriteDTO) session.getAttribute("homeWriteDTO");

        if (homeWriteDTO == null) {
            log.warn("/writeNine POST 접근 시 세션 DTO 없음. writeOne 페이지로 리다이렉션");
            return "redirect:/home/writeOne";
        }
        
        // 유효성 검사: 최소 1장의 사진이 필요함 (첫 파일의 크기가 0보다 커야 유효한 업로드로 간주)
        if (homeImages == null || homeImages.isEmpty() || (homeImages.size() == 1 && homeImages.get(0).isEmpty())) {
            log.warn("WriteNine POST 요청에 숙소 사진이 누락되었습니다.");
            redirectAttributes.addFlashAttribute("error", "숙소 사진을 최소 1장 이상 등록해야 합니다.");
            return "redirect:/home/writeNine";
        }

        try {
            // DTO에 숙소 사진 리스트 저장 (HomeWriteDTO에 List<MultipartFile> homeImages 필드가 있어야 함)
            homeWriteDTO.setHomeImages(homeImages);
            log.info("WriteNine 데이터 세션 저장 완료: 숙소 사진 {}장", homeImages.size());

        } catch (Exception e) {
            log.error("WriteNine 데이터 처리 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("error", "숙소 사진 처리 중 오류가 발생했습니다.");
            return "redirect:/home/writeNine";
        }

        session.setAttribute("homeWriteDTO", homeWriteDTO);

        // 10단계로 리다이렉션
        return "redirect:/home/writeTen";
    }

    // 10단계 폼 화면으로 이동
    @GetMapping("/writeTen")
    public String showWriteTenForm(HttpSession session, Model model) {
        HomeWriteDTO homeWriteDTO = (HomeWriteDTO) session.getAttribute("homeWriteDTO");

        if (homeWriteDTO == null) {
            log.warn("writeTen GET 접근 시 세션 DTO 없음. writeOne 페이지로 리다이렉션");
            return "redirect:/home/writeOne"; 
        }

        model.addAttribute("homeWriteDTO", homeWriteDTO);
        log.info("writeTen 단계 진입: 숙소 이름 등록");
        return "home/writeTen"; 
    }
    
    @GetMapping("/write")
    public String showWriteForm(HttpSession session, Model model) {
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("hostId", userProfile.getEmail());

        if (!model.containsAttribute("homeWriteDTO")) {
            model.addAttribute("homeWriteDTO", new HomeWriteDTO());
        }

        model.addAttribute("groupedOptions", homeService.getGroupedOptions());
        return "home/write";
    }

    @PostMapping("/write")
    public String registerHome(@ModelAttribute HomeWriteDTO homeWriteDTO,
                               RedirectAttributes redirectAttributes,
                               HttpSession session,BindingResult br) {
    	System.out.println("111111111111111111111");
    	System.out.println("binding:"+br.getAllErrors());
    	
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }
        
        homeWriteDTO.setHostId(userProfile.getEmail());

        try {
            int newHomeIdx = homeService.registerHome(homeWriteDTO);
            redirectAttributes.addFlashAttribute("message", "숙소 등록 성공!");
            return "redirect:/home/detail/" + newHomeIdx;
        } catch (MultipartException e) {
            log.error("파일 업로드 오류", e);
            redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류가 발생했습니다. 파일 크기나 형식을 확인하세요.");
        } catch (RuntimeException e) {
            log.error("숙소 등록 오류", e);
            redirectAttributes.addFlashAttribute("error", "숙소 등록 실패: " + e.getMessage());
        }

        redirectAttributes.addFlashAttribute("homeWriteDTO", homeWriteDTO);
        return "redirect:/home/write";
    }
    
   
}
*/