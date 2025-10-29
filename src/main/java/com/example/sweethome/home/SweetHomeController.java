package com.example.sweethome.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
public class SweetHomeController {

    private final HomeService homeService;

    // ================= Write 단계 =================

    @GetMapping("/writeOne")
    public String goWriteOne(HttpSession session) {
    	User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }
        
        if (session.getAttribute("homeWriteDTO") == null) {
            session.setAttribute("homeWriteDTO", new HomeWriteDTO());
            log.info("새 HomeWriteDTO 세션 초기화 (writeOne)");
        }
        return "home/writeOne";
    }

    @GetMapping("/writeTwo") public String goWriteTwo() { return "home/writeTwo"; }
    @GetMapping("/writeThree") public String goWriteThree() { return "home/writeThree"; }

    @GetMapping("/writeFour")
    public String goWriteFour(HttpSession session, Model model) {
    	User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }
        
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";

        model.addAttribute("selectedHomeType", dto.getHomeType());
        model.addAttribute("address", dto.getAddress());
        model.addAttribute("detailAddress", dto.getDetailAddress());
        return "home/writeFour";
    }

    @PostMapping("/writeFour")
    public String handleWriteFour(@RequestParam("homeType") String homeType,
                                  @RequestParam("address") String address,
                                  @RequestParam("detailAddress") String detailAddress,
                                  HttpSession session) {
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";

        dto.setHomeType(homeType);
        dto.setAddress(address);
        dto.setDetailAddress(detailAddress);
        session.setAttribute("homeWriteDTO", dto);
        
        return "redirect:/home/writeFive";
    }

    @GetMapping("/writeFive")
    public String showWriteFive(HttpSession session, Model model, @RequestParam("homeType") String homeType,
            @RequestParam("address") String address,
            @RequestParam("detailAddress") String detailAddress) {
    	
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        //쿼리스트링값 여기 저장
        dto.setHomeType(homeType);
        dto.setAddress(address);
        dto.setDetailAddress(detailAddress);
        
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }

        dto.setHostId(userProfile.getEmail());       

        if (dto == null) return "redirect:/home/writeOne";

        model.addAttribute("homeWriteDTO", dto);
        
        return "home/writeFive";
    }

    @PostMapping("/writeFive")
    public String handleWriteFive(@ModelAttribute HomeWriteDTO updatedDto,
                                  HttpSession session) {
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";
        
        dto.setMaxPeople(updatedDto.getMaxPeople());
        dto.setRoom(updatedDto.getRoom());
        dto.setBed(updatedDto.getBed());
        dto.setBath(updatedDto.getBath());
        session.setAttribute("homeWriteDTO", dto);
        
        return "redirect:/home/writeSix";
    }

    @GetMapping("/writeSix")
    public String showWriteSix(HttpSession session, Model model) {
    	User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }
        
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";
        model.addAttribute("homeWriteDTO", dto);
        
        return "home/writeSix";
    }

    @PostMapping("/writeSix")
    public String handleWriteSix(HttpSession session) { return "redirect:/home/writeSeven"; }

    @GetMapping("/writeSeven")
    public String showWriteSeven(HttpSession session, Model model) {
    	User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }
        
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";
        model.addAttribute("homeWriteDTO", dto);
        model.addAttribute("groupedOptions", homeService.getGroupedOptions());
        
        return "home/writeSeven";
    }

    @PostMapping("/writeSeven")
    public String handleWriteSeven(@ModelAttribute HomeWriteDTO updatedDto,
                                   HttpSession session) {
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";

        dto.setOptionIds(updatedDto.getOptionIds());
        session.setAttribute("homeWriteDTO", dto);
        
        return "redirect:/home/writeEight";
    }

    @GetMapping("/writeEight")
    public String showWriteEight(HttpSession session, Model model) {
    	User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }
        
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";
        model.addAttribute("homeWriteDTO", dto);
        
        return "home/writeEight";
    }

    @PostMapping("/writeEight")
    public String handleWriteEight(@RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";

        if (thumbnail == null || thumbnail.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "대표 사진을 등록해주세요.");
            return "redirect:/home/writeEight";
        }

        dto.setThumbnail(thumbnail);
        session.setAttribute("homeWriteDTO", dto);
        
        return "redirect:/home/writeNine";
    }

    @GetMapping("/writeNine")
    public String showWriteNine(HttpSession session, Model model) {
    	User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }
        
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";
        model.addAttribute("homeWriteDTO", dto);
        
        return "home/writeNine";
    }

    @PostMapping("/writeNine")
    public String handleWriteNine(@RequestParam(value = "homeImages", required = false) List<MultipartFile> homeImages,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";

        if (homeImages == null || homeImages.isEmpty() || (homeImages.size() == 1 && homeImages.get(0).isEmpty())) {
            redirectAttributes.addFlashAttribute("error", "숙소 사진을 최소 1장 이상 등록해야 합니다.");
            return "redirect:/home/writeNine";
        }

        dto.setHomeImages(homeImages);
        session.setAttribute("homeWriteDTO", dto);
        
        return "redirect:/home/writeTen";
    }

    @GetMapping("/writeTen")
    public String showWriteTen(HttpSession session, Model model) {
    	User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }
        
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";
        model.addAttribute("homeWriteDTO", dto);
        
        return "home/writeTen";
    }

    @PostMapping("/writeTen")
    public String handleWriteTen(@ModelAttribute HomeWriteDTO updatedDto,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";

        String title = updatedDto.getTitle();
        if (title == null || title.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "숙소 이름을 입력해주세요.");
            return "redirect:/home/writeTen";
        }
        if (title.length() > 200) title = title.substring(0, 200);
        dto.setTitle(title);
        session.setAttribute("homeWriteDTO", dto);
        return "redirect:/home/writeEleven";
    }

    @GetMapping("/writeEleven")
    public String showWriteEleven(HttpSession session, Model model) {
    	User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }
        
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";
        model.addAttribute("homeWriteDTO", dto);
        
        return "home/writeEleven";
    }

    @PostMapping("/writeEleven")
    public String handleWriteEleven(@ModelAttribute HomeWriteDTO updatedDto,
                                    HttpSession session) {
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";

        String description = updatedDto.getDescription();
        if (description != null && description.length() > 2000) description = description.substring(0, 2000);
        dto.setDescription(description);
        session.setAttribute("homeWriteDTO", dto);
        return "redirect:/home/writeTwelve";
    }

    @GetMapping("/writeTwelve")
    public String showWriteTwelve(HttpSession session, Model model) {
    	User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }
        
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";
        model.addAttribute("homeWriteDTO", dto);
        
        return "home/writeTwelve";
    }

    @PostMapping("/writeTwelve")
    public String handleWriteTwelve(@ModelAttribute HomeWriteDTO updatedDto,
                                    HttpSession session) {
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";

        dto.setLocation(updatedDto.getLocation());
        dto.setCostBasic(updatedDto.getCostBasic());
        dto.setCostExpen(updatedDto.getCostExpen());
        dto.setCheckIn(updatedDto.getCheckIn());
        dto.setCheckOut(updatedDto.getCheckOut());
        session.setAttribute("homeWriteDTO", dto);

        return "redirect:/home/writeThirteen";
    }


    @GetMapping("/writeThirteen")
    public String showWriteThirteen(HttpSession session, Model model) {
    	User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }
        
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";
        
        System.out.println("현재 DTO 상태:");
        System.out.println(dto);

        model.addAttribute("homeWriteDTO", dto);
        return "home/writeThirteen";
    }

    @PostMapping("/writeThirteen")
    public String handleWriteThirteen(@ModelAttribute HomeWriteDTO updatedDto,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        HomeWriteDTO dto = (HomeWriteDTO) session.getAttribute("homeWriteDTO");
        if (dto == null) return "redirect:/home/writeOne";

        // 해시태그 체크박스 정보 반영
        dto.setWifi(updatedDto.isWifi());
        dto.setTv(updatedDto.isTv());
        dto.setKitchen(updatedDto.isKitchen());
        dto.setFreePark(updatedDto.isFreePark());
        dto.setSelfCheckin(updatedDto.isSelfCheckin());
        dto.setColdWarm(updatedDto.isColdWarm());
        dto.setPetFriendly(updatedDto.isPetFriendly());
        dto.setBarrierFree(updatedDto.isBarrierFree());
        dto.setElevator(updatedDto.isElevator());

        // 최종 DB 저장
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }
        dto.setHostId(userProfile.getEmail());

        try {
            int newHomeIdx = homeService.registerHome(dto);
            session.removeAttribute("homeWriteDTO");
            redirectAttributes.addFlashAttribute("message", "숙소 등록 성공!");
            return "redirect:/home/writeComplete?homeIdx=" + newHomeIdx;
        } catch (Exception e) {
            log.error("숙소 등록 실패", e);
            redirectAttributes.addFlashAttribute("error", "숙소 등록 실패: " + e.getMessage());
            return "redirect:/home/writeThirteen";
        }
    }

    @GetMapping("/writeComplete")
    public String showWriteComplete(@RequestParam("homeIdx") int homeIdx, Model model) {
        model.addAttribute("homeIdx", homeIdx);
        return "home/writeComplete";
    }
    
    /*
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
    */

}
