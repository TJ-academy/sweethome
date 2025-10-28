package com.example.sweethome.mypage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import com.example.sweethome.review.Reply;
import com.example.sweethome.review.ReplyRepository;
import com.example.sweethome.review.Review;
import com.example.sweethome.review.ReviewDirection;
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
    private final ReplyRepository replyRepository;

    // ★ 목록 페이지
    @GetMapping("/review")
    public String showReview(HttpSession session, Model model) {
        User user = (User) session.getAttribute("userProfile");
        if (user == null) return "redirect:/user/login";
        model.addAttribute("user", user);

        // 내가 쓴 리뷰
        List<Review> mine = reviewRepository.findByWriterOrderByCreatedAtDesc(user);

        // 나에 대한 리뷰(둘 다 합침)
        List<Review> aboutMe = new ArrayList<>();
        aboutMe.addAll(
            reviewRepository.findByDirectionAndReservation_ReservedHome_HostOrderByCreatedAtDesc(
                ReviewDirection.GUEST_TO_HOST, user)
        );
        aboutMe.addAll(
            reviewRepository.findByDirectionAndReservation_BookerOrderByCreatedAtDesc(
                ReviewDirection.HOST_TO_GUEST, user)
        );
        aboutMe.sort(Comparator.comparing(Review::getCreatedAt).reversed());

        // 엔티티 → 카드 VM 변환
        List<ReviewCardVM> myCards = mine.stream().map(this::toVM).toList();
        List<ReviewCardVM> aboutMeCards = aboutMe.stream().map(this::toVM).toList();

        model.addAttribute("myReviews", myCards);
        model.addAttribute("reviewsAboutMe", aboutMeCards);

        return "mypage/myReviewMain";
    }

    private ReviewCardVM toVM(Review r) {
        return ReviewCardVM.builder()
                .id(r.getReviewIdx())
                .reservationId(r.getReservation().getReservationIdx())
                .homeTitle(r.getHome().getTitle())
                .rating((double) r.getStar())
                .content(r.getContent())
                .createdAt(r.getCreatedAt())
                .reviewerNickname(r.getWriter().getNickname())
                .direction(r.getDirection())
                .build();
    }

    // ★ 작성 폼
    @GetMapping("/review/write")
    public String writeReviewForm(@RequestParam("reservationId") int reservationId,
                                  @RequestParam("direction") ReviewDirection direction,
                                  HttpSession session,
                                  Model model) {
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

        if (r.getReservationStatus() != ReservationStatus.COMPLETED)
            throw new IllegalStateException("이용 완료된 예약만 리뷰 작성이 가능합니다.");

        if (reviewRepository.existsByReservationAndDirection(r, direction)) {
            return "redirect:/mypage/review/detail?reservationId=" + reservationId + "&direction=" + direction.name(); // .name() 추가
        }

        // 템플릿 바인딩
        model.addAttribute("user", user);
        model.addAttribute("reservation", r);
        model.addAttribute("direction", direction);
        model.addAttribute("home", r.getReservedHome());

        // 대상 사용자(표시용)
        User targetUser = (direction == ReviewDirection.GUEST_TO_HOST)
                ? r.getReservedHome().getHost()
                : r.getBooker();
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("targetLabel", direction == ReviewDirection.GUEST_TO_HOST ? "호스트" : "게스트");

        return "mypage/myReviewWrite";
    }

    // ★ 저장
    @PostMapping("/review/write")
    @Transactional
    public String submitReview(@RequestParam("reservationId") int reservationId,
                               @RequestParam("direction") ReviewDirection direction,
                               @RequestParam("star") int star,
                               @RequestParam("content") String content,
                               HttpSession session) {

        User user = (User) session.getAttribute("userProfile");
        if (user == null) return "redirect:/user/login";

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 권한 & 상태 & 중복 체크
        if (direction == ReviewDirection.GUEST_TO_HOST) {
            if (!r.getBooker().getEmail().equals(user.getEmail()))
                throw new IllegalArgumentException("해당 예약에 대한 게스트 리뷰 권한이 없습니다.");
        } else {
            if (!r.getReservedHome().getHost().getEmail().equals(user.getEmail()))
                throw new IllegalArgumentException("해당 예약에 대한 호스트 리뷰 권한이 없습니다.");
        }
        if (r.getReservationStatus() != ReservationStatus.COMPLETED)
            throw new IllegalStateException("이용 완료된 예약만 리뷰 작성이 가능합니다.");

        if (reviewRepository.existsByReservationAndDirection(r, direction))
            throw new IllegalStateException("이미 리뷰를 작성하였습니다.");

        Review review = Review.builder()
                .reservation(r)
                .home(r.getReservedHome())
                .writer(user)
                .direction(direction)
                .star(star)
                .content(content)
                // 이미지 필드는 null로 설정 (필요 시 Review 엔티티에서 해당 필드 제거 고려)
                .reviewThumb(null)
                .imgOne(null)
                .imgTwo(null)
                .imgThree(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        reviewRepository.save(review);

        return "redirect:/mypage/review/detail?reservationId=" + reservationId + "&direction=" + direction.name();
    }
    
    // ★ 상세
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

        // 열람 권한: 작성자 또는 예약 참여자(게스트/호스트)
        User viewer = user;
        User booker = r.getBooker();
        User host   = r.getReservedHome().getHost();

        boolean isWriter = review.getWriter().getEmail().equals(viewer.getEmail());
        boolean isParticipant = viewer.getEmail().equals(booker.getEmail()) || viewer.getEmail().equals(host.getEmail());

        if (!(isWriter || isParticipant)) {
            throw new IllegalArgumentException("리뷰 열람 권한이 없습니다.");
        }
        
     // 대상 사용자 (표시용)
        User targetUser = (direction == ReviewDirection.GUEST_TO_HOST) ? host : booker;
        String targetLabel = (direction == ReviewDirection.GUEST_TO_HOST) ? "호스트" : "게스트";
        User writer = review.getWriter();
        
        // 템플릿 에러 방지를 위해 방향 라벨을 미리 계산하여 추가 (HTML에서 Enum 비교 오류 방지)
        String directionLabel = (direction == ReviewDirection.GUEST_TO_HOST) ? "게스트 → 호스트" : "호스트 → 게스트";
        
     // 4. 답글 조회 (답글이 존재할 경우)
        Reply reply = replyRepository.findByReview(review).orElse(null);

        // 모델 바인딩
        model.addAttribute("user", viewer);
        model.addAttribute("reservation", r);
        model.addAttribute("home", r.getReservedHome());
        model.addAttribute("review", review);
        model.addAttribute("directionLabel", directionLabel); // 이 변수를 HTML에서 사용
        model.addAttribute("writer", writer); 
        model.addAttribute("targetUser", targetUser); 
        model.addAttribute("targetLabel", targetLabel); 
        model.addAttribute("isWriter", isWriter); 
        model.addAttribute("direction", direction); // URL 생성 시 .name()을 위해 유지
        
        model.addAttribute("isHost", viewer.getEmail().equals(host.getEmail())); // 호스트 여부 추가
        model.addAttribute("reply", reply); // 답글 객체 추가
        
        return "mypage/myReviewDetail";
    }
    
    // ★ 수정 폼
    @GetMapping("/review/edit")
    public String editReviewForm(@RequestParam("reservationId") int reservationId,
                                 @RequestParam("direction") ReviewDirection direction,
                                 HttpSession session,
                                 Model model) {
        User user = (User) session.getAttribute("userProfile");
        if (user == null) return "redirect:/user/login";

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        Review review = reviewRepository.findByReservationAndDirection(r, direction)
                .orElseThrow(() -> new IllegalArgumentException("작성된 리뷰가 없습니다."));

        // 권한 체크: 리뷰 작성자만 수정 가능
        if (!review.getWriter().getEmail().equals(user.getEmail()))
            throw new IllegalArgumentException("리뷰 수정 권한이 없습니다.");

        // 템플릿 바인딩 (작성 폼과 거의 동일)
        model.addAttribute("user", user);
        model.addAttribute("reservation", r);
        model.addAttribute("direction", direction);
        model.addAttribute("home", r.getReservedHome());
        model.addAttribute("review", review);

        // 대상 사용자(표시용)
        User targetUser = (direction == ReviewDirection.GUEST_TO_HOST)
                ? r.getReservedHome().getHost()
                : r.getBooker();
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("targetLabel", direction == ReviewDirection.GUEST_TO_HOST ? "호스트" : "게스트");

        return "mypage/myReviewEdit";
    }

    // ★ 수정 저장 (이미지 기능 완전 삭제)
    @PostMapping("/review/edit")
    @Transactional
    public String updateReview(@RequestParam("reviewId") int reviewId,
                               @RequestParam("reservationId") int reservationId,
                               @RequestParam("direction") ReviewDirection direction,
                               @RequestParam("star") int star,
                               @RequestParam("content") String content,
                               HttpSession session) {

        User user = (User) session.getAttribute("userProfile");
        if (user == null) return "redirect:/user/login";

        // 기존 리뷰 로드
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        // 권한 체크
        if (!review.getWriter().getEmail().equals(user.getEmail()))
            throw new IllegalArgumentException("리뷰 수정 권한이 없습니다.");

        // 1. 내용 및 별점 업데이트
        review.setStar(star);
        review.setContent(content);
        review.setUpdatedAt(LocalDateTime.now());
        
        // 2. 이미지 필드는 그대로 유지 (또는 Review 엔티티에서 제거)
        // 여기서는 코드가 단순화되어 이미지 관련 코드가 모두 제거되었습니다.
        
        reviewRepository.save(review); // Transactional로 인해 자동 더티체킹됨

        // ⚠️ 문제 해결: 리다이렉트 시 direction.name() 사용
        return "redirect:/mypage/review/detail?reservationId=" + reservationId + "&direction=" + direction.name();
    }
    
    // 이미지 저장/삭제 관련 헬퍼 함수는 모두 제거되었습니다.
}