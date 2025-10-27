package com.example.sweethome.user;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sweethome.home.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/host")
@RequiredArgsConstructor
public class HostController {

    private final HashtagRepository hashtagRepository;

    private final AccommodationOptionRepository accommodationOptionRepository;

    private final OptionRepository optionRepository;

	private final HomeRepository homeRepository; // 🌟 HomeRepository 주입
    private final HomeService homeService;

    /*
    HostController(OptionRepository optionRepository, AccommodationOptionRepository accommodationOptionRepository, HashtagRepository hashtagRepository) {
        this.optionRepository = optionRepository;
        this.accommodationOptionRepository = accommodationOptionRepository;
        this.hashtagRepository = hashtagRepository;
    } // 🌟 HomeService 추가 주입
    */

    @GetMapping("/list")
    public String list(HttpSession session, Model model) {

        User user = (User) session.getAttribute("userProfile");
        if (user == null)
            return "redirect:/user/login";

        // 🌟 로그인된 호스트의 숙소 목록 조회
        List<Home> myHomes = homeRepository.findByHost(user);

        model.addAttribute("user", user);
        model.addAttribute("myHomes", myHomes); // 🌟 모델에 숙소 리스트 추가

        return "host/myHomeList";
    }

    /**
     * 숙소 상세 페이지
     */
    @GetMapping("/detail/{homeIdx}")
    public String detail(@PathVariable("homeIdx") int homeIdx, HttpSession session, Model model) {

        User user = (User) session.getAttribute("userProfile");
        if (user == null)
            return "redirect:/user/login";

        // 1. 숙소 정보 조회
        Optional<Home> homeOpt = homeRepository.findById(homeIdx);

        if (homeOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "숙소 정보를 찾을 수 없습니다.");
        }

        Home home = homeOpt.get();

        // 2. 호스트가 현재 로그인된 사용자인지 확인 (보안 체크)
        if (!home.getHost().getEmail().equals(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }

        // 3. 모델에 추가
        model.addAttribute("user", user);
        model.addAttribute("home", home); // 🌟 숙소 상세 정보 추가

        return "host/myHomeDetail"; // myHomeDetail.html 템플릿으로 이동
    }

	@GetMapping("/calendar")
	public String calendar(HttpSession session, Model model) {

		User user = (User) session.getAttribute("userProfile");
		if (user == null)
			return "redirect:/user/login";

		model.addAttribute("user", user);
		return "host/calendar";
	}

	@GetMapping("/today")
	public String today(HttpSession session, Model model) {

		User user = (User) session.getAttribute("userProfile");
		if (user == null)
			return "redirect:/user/login";

		model.addAttribute("user", user);

		return "host/today";
	}
	
	@GetMapping("/edit/{homeIdx}")
    public String myHomeUpdate(@PathVariable("homeIdx") int homeIdx, 
                               Model model, 
                               HttpSession session) {
        
        // 1. 호스트 로그인 상태 및 권한 확인
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            // 로그인되어 있지 않으면 로그인 페이지로 리다이렉트
            return "redirect:/user/login"; 
        }

        // 2. Home 엔티티 조회
        Home home = homeService.getHomeById(homeIdx);
        if (home == null) {
            // 숙소가 존재하지 않으면 에러 페이지 또는 목록 페이지로 리다이렉트
            return "redirect:/home/list"; // 예시
        }

        // 3. 호스트 권한 확인 (세션 유저의 이메일과 숙소 호스트의 이메일 비교)
        if (!userProfile.getEmail().equals(home.getHost().getEmail())) {
            // 권한이 없으면 접근 거부 처리 (예: 이전 페이지 리다이렉트 또는 에러 메시지)
            model.addAttribute("error", "해당 숙소의 수정 권한이 없습니다.");
            return "redirect:/home/detail/" + homeIdx; 
        }

        // 4. Home 엔티티를 HomeWriteDTO로 변환 (기존 데이터 채우기)
        // 이 로직은 HomeService에 이미 구현되어 있습니다.
        HomeWriteDTO homeWriteDTO = homeService.getHomeWriteDTOForUpdate(home);
        
        // 5. 모든 옵션 목록을 그룹별로 조회 (폼에 체크박스를 생성하기 위해 필요)
        Map<String, List<Option>> groupedOptions = homeService.getGroupedOptions();
        
        // 6. Model에 데이터 바인딩
        // DTO에 있는 컬럼들이 전부 HTML 파일로 보내지게끔 바인딩
        model.addAttribute("homeWriteDTO", homeWriteDTO);
        model.addAttribute("groupedOptions", groupedOptions);

        return "host/myHomeUpdate";
    }

	//숙소 정보 수정 처리
	@PostMapping("/edit/{homeIdx}")
    public String updateHome(@PathVariable("homeIdx") int homeIdx,
                             @ModelAttribute HomeWriteDTO homeWriteDTO,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {

        // 1. 호스트 로그인 상태 및 권한 확인
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }

        // DTO에 숙소 idx와 호스트 ID 설정 (HomeService에서 권한 체크에 사용될 수 있음)
        homeWriteDTO.setIdx(homeIdx);
        homeWriteDTO.setHostId(userProfile.getEmail());

        try {
            // 2. 서비스 로직 호출하여 숙소 정보 업데이트
            homeService.updateHome(homeIdx, homeWriteDTO, userProfile);

            // 3. 성공 메시지 담기 및 상세 페이지로 리다이렉트
            redirectAttributes.addFlashAttribute("message", "숙소 정보가 성공적으로 수정되었습니다.");
            return "redirect:/home/detail/" + homeIdx;

        } catch (IllegalArgumentException e) {
            // 4. 권한 에러나 존재하지 않는 숙소 에러 처리
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/host/edit/" + homeIdx;
        } catch (RuntimeException e) {
            // 5. 기타 DB 또는 파일 처리 오류
            redirectAttributes.addFlashAttribute("error", "숙소 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/host/edit/" + homeIdx;
        }
    }
	
	//숙소 삭제
	
}
