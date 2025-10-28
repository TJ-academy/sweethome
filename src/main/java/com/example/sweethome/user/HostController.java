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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sweethome.home.AccommodationOptionRepository;
import com.example.sweethome.home.HashtagRepository;
import com.example.sweethome.home.Home;
import com.example.sweethome.home.HomeRepository;
import com.example.sweethome.home.HomeService;
import com.example.sweethome.home.HomeWriteDTO;
import com.example.sweethome.home.Option;
import com.example.sweethome.home.OptionRepository;
import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationRepository;
import com.example.sweethome.reservation.ReservationStatus;
import com.example.sweethome.review.ReviewDirection;
import com.example.sweethome.review.ReviewRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/host")
@RequiredArgsConstructor
public class HostController {

	private final HomeRepository homeRepository; // 🌟 HomeRepository 주입
	private final ReservationRepository reservationRepository;
	private final HashtagRepository hashtagRepository;
	private final AccommodationOptionRepository accommodationOptionRepository;
	private final OptionRepository optionRepository;
	private final HomeService homeService;
	private final ReviewRepository reviewRepository;


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

		// 1. 호스트의 모든 숙소 목록 조회 (숙소 필터링용)
		// 🌟 추가된 부분
        List<Home> myHomes = homeRepository.findByHost(user);
        
        // 2. 오늘 날짜 예약 내역 조회 (오늘 체크인, 체크아웃, 숙박 중인 예약)
        // 이 부분은 서비스 레이어에서 처리하는 것이 좋으나, 임시로 리포지토리 메서드를 가정합니다.
        // 현재 코드로만 판단할 때, '오늘의 예약 리스트'를 가져오는 로직이 필요합니다.
        // 임시로 모든 예약을 가져오는 것으로 대체하고, 실제 구현 시 날짜 기반 쿼리로 변경해야 합니다.
        // List<Reservation> todayReservations = reservationRepository.findTodayBookingsForHost(user.getEmail()); 
        // findTodayBookingsForHost 메서드가 없다고 가정하고, 일단 모든 예약을 가져와서 템플릿에서 테스트할 수 있도록 합니다.
        // List<Reservation> todayReservations = reservationRepository.findByReservedHome_Host(user);
        
        // 실제 운영 환경에서는 오늘 날짜에 해당하는 예약만 필터링해야 합니다.
        // 임시로 호스트의 모든 확정된 예약을 가져와 템플릿에 전달합니다. (정확한 '오늘'의 예약 구현은 서비스 레이어에서 별도의 날짜 쿼리 필요)
        List<Reservation> todayReservations = reservationRepository.findByReservedHome_Host(user);

        model.addAttribute("user", user);
        // 🌟 숙소 필터링용 목록 추가
        model.addAttribute("myHomes", myHomes); 
        // 🌟 오늘의 예약 목록 추가
        model.addAttribute("todayReservations", todayReservations); 
        
        // 오늘 날짜 (템플릿에 전달)
        model.addAttribute("todayDate", java.time.LocalDate.now());

		return "host/today";
	}
	
	@org.springframework.web.bind.annotation.RequestMapping(
	        value = "/reservation/detail/{reservationId}",
	        method = {org.springframework.web.bind.annotation.RequestMethod.GET,
	                  org.springframework.web.bind.annotation.RequestMethod.POST}
	)
	public String reservationDetail(
	        @PathVariable("reservationId") int reservationId,
	        @org.springframework.web.bind.annotation.RequestParam(value = "action", required = false) String action,
	        jakarta.servlet.http.HttpSession session,
	        org.springframework.ui.Model model) {

	    User host = (User) session.getAttribute("userProfile");
	    if (host == null) return "redirect:/user/login";

	    Reservation r = reservationRepository.findById(reservationId)
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."));

	    // 소유권 확인(로그인한 사용자가 해당 숙소의 호스트인지)
	    if (!r.getReservedHome().getHost().getEmail().equals(host.getEmail())) {
	        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
	    }

	    // ---- POST 액션 처리 ----
	    if (action != null) {
	        ReservationStatus curr = r.getReservationStatus();

	        switch (action) {
	            case "confirm": // 예약 확인
	                if (curr == ReservationStatus.REQUESTED) {
	                    r.setReservationStatus(ReservationStatus.CONFIRMED);
	                }
	                break;

	            case "reject": // 예약 거절
	                if (curr == ReservationStatus.REQUESTED) {
	                    r.setReservationStatus(ReservationStatus.REJECTED);
	                }
	                break;

	            case "cancel_accept": // 취소 수락
	                if (curr == ReservationStatus.CANCEL_REQUESTED) {
	                    r.setReservationStatus(ReservationStatus.CANCELLED);
	                }
	                break;

	            case "cancel_reject": // 취소 거절 → 다시 예약 확정됨으로
	                if (curr == ReservationStatus.CANCEL_REQUESTED) {
	                    r.setReservationStatus(ReservationStatus.CONFIRMED);
	                }
	                break;

	            case "start_use": // 이용중으로 전환
	                if (curr == ReservationStatus.CONFIRMED) {
	                    r.setReservationStatus(ReservationStatus.IN_USE);
	                }
	                break;

	            case "complete": // 이용완료로 전환
	                if (curr == ReservationStatus.IN_USE) {
	                    r.setReservationStatus(ReservationStatus.COMPLETED);
	                }
	                break;
	        }

	        reservationRepository.save(r);
	        return "redirect:/host/reservation/detail/" + reservationId;
	    }

	    // ---- GET: 상세 화면 렌더 ----
	    
	 // ✅ 리뷰 작성 여부 확인 로직 추가
	    boolean hasHostReviewedGuest = reviewRepository
	            .existsByReservationAndDirection(r, ReviewDirection.HOST_TO_GUEST);
	    
	    model.addAttribute("user", host);
	    model.addAttribute("reservation", r);
	    model.addAttribute("home", r.getReservedHome());
	    model.addAttribute("hasHostReviewedGuest", hasHostReviewedGuest);
	    return "host/reservationDetail";
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
	@PostMapping("/delete/{homeIdx}")
    public String deleteHome(@PathVariable("homeIdx") int homeIdx,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        // 1. 호스트 로그인 상태 확인
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }

        try {
            // 2. Service 호출하여 숙소 및 연관 데이터 삭제 로직 실행
            // HomeService에서 권한 체크 및 트랜잭션 처리가 모두 이루어집니다.
            homeService.deleteHome(homeIdx, userProfile);

            // 3. 성공 메시지 담고 호스트의 숙소 목록 페이지로 리다이렉트
            redirectAttributes.addFlashAttribute("message", "숙소가 성공적으로 삭제되었습니다.");
            return "redirect:/host/list";

        } catch (IllegalArgumentException e) {
            // 숙소가 존재하지 않는 경우 (HomeService에서 발생)
            redirectAttributes.addFlashAttribute("error", "삭제하려는 숙소를 찾을 수 없습니다.");
            return "redirect:/host/list";
        } catch (IllegalStateException e) {
            // 권한 오류 (HostController의 detail과 같이 HomeService에서 호스트 불일치 시 발생)
            redirectAttributes.addFlashAttribute("error", "해당 숙소의 삭제 권한이 없습니다.");
            return "redirect:/host/detail/" + homeIdx;
        } catch (RuntimeException e) {
            // 기타 DB 처리 오류
            redirectAttributes.addFlashAttribute("error", "숙소 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/host/detail/" + homeIdx;
        }
    }
	
}