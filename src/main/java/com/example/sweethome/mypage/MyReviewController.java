package com.example.sweethome.mypage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationRepository;
import com.example.sweethome.reservation.ReservationStatus;
import com.example.sweethome.review.Review;
import com.example.sweethome.review.ReviewDirection;
import com.example.sweethome.review.ReviewKeyword;
import com.example.sweethome.review.ReviewKeywordMap;
import com.example.sweethome.review.ReviewKeywordMapRepository;
import com.example.sweethome.review.ReviewKeywordRepository;
import com.example.sweethome.review.ReviewRepository;
import com.example.sweethome.user.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyReviewController {

	private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewKeywordRepository reviewKeywordRepository;
    private final ReviewKeywordMapRepository reviewKeywordMapRepository;
	
	@GetMapping("/review")
	public String showReview(HttpSession session, Model model) {
		User user = (User) session.getAttribute("userProfile");
        if (user == null) return "redirect:/user/login";
        model.addAttribute("user", user);
		return "mypage/myReviewMain";
	}
	
	@GetMapping("/review/write")
    public String writeReviewForm(@RequestParam("reservationId") int reservationId,
                                  @RequestParam("direction") ReviewDirection direction,
                                  HttpSession session,
                                  Model model) {
        User user = (User) session.getAttribute("userProfile");
        if (user == null) return "redirect:/user/login";

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 권한 체크: 방향에 따라 작성자 달라짐
        if (direction == ReviewDirection.GUEST_TO_HOST) {
            if (!r.getBooker().getEmail().equals(user.getEmail()))
                throw new IllegalArgumentException("해당 예약에 대한 게스트 리뷰 권한이 없습니다.");
        } else { // HOST_TO_GUEST
            if (!r.getReservedHome().getHost().getEmail().equals(user.getEmail()))
                throw new IllegalArgumentException("해당 예약에 대한 호스트 리뷰 권한이 없습니다.");
        }

        // 상태 체크: 완료된 예약만 작성 가능
        if (r.getReservationStatus() != ReservationStatus.COMPLETED) {
            throw new IllegalStateException("이용 완료된 예약만 리뷰 작성이 가능합니다.");
        }
        
        // 상대방(targetUser) 계산
        User targetUser = (direction == ReviewDirection.GUEST_TO_HOST)
                ? r.getReservedHome().getHost()   // 호스트 객체
                : r.getBooker();                  // 게스트 객체


        // 중복 작성 방지
        boolean exists = reviewRepository.existsByReservationAndDirection(r, direction);
        if (exists) {
            return "redirect:/mypage/review/detail?reservationId=" + reservationId + "&direction=" + direction;
        }

        // 키워드(카테고리별 묶음) 조회
        List<ReviewKeyword> allKeywords = reviewKeywordRepository.findAll();
        Map<String, List<ReviewKeyword>> keywordsByCategory = allKeywords.stream()
                .collect(Collectors.groupingBy(ReviewKeyword::getCategory));

        model.addAttribute("user", user);
        model.addAttribute("reservation", r);
        model.addAttribute("direction", direction);
        model.addAttribute("home", r.getReservedHome());
        model.addAttribute("keywordsByCategory", keywordsByCategory);

        model.addAttribute("targetUser", targetUser);   // ★ 여기!
        model.addAttribute("keywordGroups", reviewKeywordRepository.findAll()); // 선택
        
        return "mypage/myReviewWrite"; // 작성 폼 페이지
    }

    /**
     * 리뷰 저장
     */
    @PostMapping("/review/write")
    @Transactional
    public String submitReview(@RequestParam("reservationId") int reservationId,
                               @RequestParam("direction") ReviewDirection direction,
                               @RequestParam("star") int star,
                               @RequestParam("content") String content,
                               @RequestParam(value = "keywordIds", required = false) List<Integer> keywordIds,
                               HttpSession session) {

        User user = (User) session.getAttribute("userProfile");
        if (user == null) return "redirect:/user/login";

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 권한 체크
        if (direction == ReviewDirection.GUEST_TO_HOST) {
            if (!r.getBooker().getEmail().equals(user.getEmail()))
                throw new IllegalArgumentException("해당 예약에 대한 게스트 리뷰 권한이 없습니다.");
        } else {
            if (!r.getReservedHome().getHost().getEmail().equals(user.getEmail()))
                throw new IllegalArgumentException("해당 예약에 대한 호스트 리뷰 권한이 없습니다.");
        }

        // 상태/중복 체크
        if (r.getReservationStatus() != ReservationStatus.COMPLETED)
            throw new IllegalStateException("이용 완료된 예약만 리뷰 작성이 가능합니다.");

        if (reviewRepository.existsByReservationAndDirection(r, direction))
            throw new IllegalStateException("이미 리뷰를 작성하였습니다.");

        // 리뷰 저장
        Review review = Review.builder()
                .reservation(r)
                .home(r.getReservedHome())
                .writer(user)
                .direction(direction)          // ★ Review 엔티티에 direction 필드가 있어야 합니다
                .star(star)
                .content(content)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        reviewRepository.save(review);

        // 키워드 매핑
        if (keywordIds != null && !keywordIds.isEmpty()) {
            List<ReviewKeyword> selected = reviewKeywordRepository.findAllById(keywordIds);
            for (ReviewKeyword k : selected) {
                ReviewKeywordMap map = ReviewKeywordMap.builder()
                        .review(review)
                        .keyword(k)
                        .build();
                reviewKeywordMapRepository.save(map);
            }
        }

        return "redirect:/mypage/review/detail?reservationId=" + reservationId + "&direction=" + direction;
    }

    /**
     * 리뷰 상세 보기 (내가 쓴 리뷰)
     */
    @GetMapping("/review/detail")
    public String reviewDetail(@RequestParam("reservationId") int reservationId,
                               @RequestParam("direction") ReviewDirection direction,
                               HttpSession session,
                               Model model) {
        User user = (User) session.getAttribute("userProfile");
        if (user == null) return "redirect:/user/login";

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        Review review = reviewRepository.findByReservationAndDirection(r, direction)
                .orElseThrow(() -> new IllegalArgumentException("작성된 리뷰가 없습니다."));

        // 본인 리뷰만 열람 가능(게스트/호스트 방향에 따라)
        if (!review.getWriter().getEmail().equals(user.getEmail()))
            throw new IllegalArgumentException("리뷰 열람 권한이 없습니다.");

        model.addAttribute("user", user);
        model.addAttribute("reservation", r);
        model.addAttribute("review", review);
        model.addAttribute("direction", direction);
        return "mypage/myReviewDetail";
    }
}
