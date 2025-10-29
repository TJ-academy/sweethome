package com.example.sweethome.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationRepository;
import com.example.sweethome.review.Reply;
import com.example.sweethome.review.ReplyRepository;
import com.example.sweethome.review.Review;
import com.example.sweethome.review.ReviewDirection;
import com.example.sweethome.review.ReviewRepository;
import com.example.sweethome.user.noti.NotificationService;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Controller
@RequestMapping("/host/review")
@RequiredArgsConstructor
public class HostReviewController {

    private final ReviewRepository reviewRepository;
    private final ReplyRepository replyRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notiservice;

    /**
     * 호스트에게 작성된 리뷰 목록 (GUEST_TO_HOST) 조회
     */
    @GetMapping("/list")
    public String getHostReviews(HttpSession session, Model model) {
        User hostUser = (User) session.getAttribute("userProfile");
        
        // 1. 로그인 체크
        if (hostUser == null) {
            return "redirect:/user/login"; 
        }
        
        model.addAttribute("user", hostUser);
        
        // 2. 호스트의 숙소에 작성된 GUEST_TO_HOST 방향의 리뷰를 모두 조회
        // (ReviewRepository에 findByHomeHostEmailAndDirection 메서드가 존재해야 함)
        List<Review> reviewsAboutMe = reviewRepository
                .findByHomeHostEmailAndDirection(hostUser.getEmail(), ReviewDirection.GUEST_TO_HOST);

        // 3. 각 리뷰에 대해 답글(Reply) 존재 여부를 확인하여 DTO 형태로 변환
        List<HostReviewDto> hostReviewDtos = reviewsAboutMe.stream()
                .map(review -> {
                    Reply reply = replyRepository.findByReview(review).orElse(null);
                    return new HostReviewDto(
                            review.getReviewIdx(),
                            review.getHome().getTitle(),
                            review.getWriter().getNickname(),
                            review.getStar(),
                            review.getContent(),
                            review.getCreatedAt(),
                            review.getReservation().getReservationIdx(), 
                            reply != null, 
                            reply != null ? reply.getReplyIdx() : null 
                    );
                }).collect(Collectors.toList());

        model.addAttribute("reviewsAboutMe", hostReviewDtos);
        
        // 🚨 userType 관련 코드는 User 엔티티에 role이 없으므로 제거합니다.
        // model.addAttribute("userType", hostUser.getRole().name()); // 이 코드를 제거함

        return "host/hostReviewList";
    }
    
    /**
     * 호스트의 게스트 리뷰 작성 페이지 (HOST_TO_GUEST)
     */
    @GetMapping("/write")
    public String writeGuestReview(@RequestParam("reservationId") int reservationId, HttpSession session, Model model) {
        User hostUser = (User) session.getAttribute("userProfile");
        
        // 1. 로그인 체크 (호스트)
        if (hostUser == null) {
            return "redirect:/user/login"; 
        }
        
        model.addAttribute("user", hostUser);
        
        // 2. 예약 정보 확인
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reservation Id:" + reservationId));

        // 3. 권한 확인: 이 숙소의 호스트가 맞는지 확인
        if (!reservation.getReservedHome().getHost().getEmail().equals(hostUser.getEmail())) {
            // 권한 없음
            return "redirect:/host/reservations?error=unauthorized";
        }
        
        // 4. 이미 리뷰를 작성했는지 확인 (HOST_TO_GUEST 방향으로)
        boolean alreadyWrote = reviewRepository
                .findByReservationAndDirection(reservation, ReviewDirection.HOST_TO_GUEST)
                .isPresent();
        
        if (alreadyWrote) {
            // 이미 작성했으면 상세 페이지로 리다이렉트 (또는 알림)
            return "redirect:/host/reservation/detail/" + reservationId + "?error=already_reviewed";
        }

        model.addAttribute("reservation", reservation);
        model.addAttribute("guestNickname", reservation.getBooker().getNickname());
        
        return "host/hostReviewWrite"; // 💡 hostReviewWrite.html로 이동
    }
    
    /**
     * 호스트의 게스트 리뷰 작성 완료 처리 (HOST_TO_GUEST)
     */
    @PostMapping("/write")
    public String submitGuestReview(
            @RequestParam("reservationId") int reservationId,
            @RequestParam("star") double star,
            @RequestParam("content") String content,
            HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {

        User hostUser = (User) session.getAttribute("userProfile");
        
        // 1. 로그인 체크
        if (hostUser == null) {
            return "redirect:/user/login"; 
        }
        model.addAttribute("user", hostUser);

        // 2. 예약 정보 확인 및 리뷰 생성
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reservation Id:" + reservationId));
        
        // 3. 권한 확인 (2번과 동일)
        if (!reservation.getReservedHome().getHost().getEmail().equals(hostUser.getEmail())) {
            return "redirect:/host/reservations?error=unauthorized";
        }
        
     // ✅ 4. 최종 방어: POST 요청 시 중복 작성 재확인 로직 추가! (수정할 부분)
        // existsByReservationAndDirection을 사용하여 DB에 이미 있는지 확인합니다.
        boolean alreadyWrote = reviewRepository
                .existsByReservationAndDirection(reservation, ReviewDirection.HOST_TO_GUEST);
        
        if (alreadyWrote) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 해당 예약에 대한 리뷰를 작성했습니다.");
            // 이미 작성했으므로, 리뷰 저장 시도 없이 상세 페이지로 리다이렉트
            return "redirect:/host/reservation/detail/" + reservationId;
        }

     // 4. 리뷰 엔티티 생성 및 저장
        Review review = Review.builder()
                .reservation(reservation)
                // 💡 target 필드 제거: 예약자(게스트) 정보는 reservation.getBooker()를 통해 간접적으로 참조됨.
                .writer(hostUser) // 작성자: 호스트
                .home(reservation.getReservedHome())
                .star((int)star) // Review 엔티티의 star 필드는 int이므로 캐스팅
                .content(content)
                .direction(ReviewDirection.HOST_TO_GUEST) // 방향: HOST -> GUEST
                .createdAt(java.time.LocalDateTime.now()) // createdAt 필드 추가 (nullable이 아니라면)
                .build();
        
        Review savedReview = reviewRepository.save(review);
        
        User guest = reservation.getBooker();
        
        String message = savedReview.getContent().length() > 10 
        		? savedReview.getContent().substring(0, 10) + "..." 
				: savedReview.getContent();
        
    	notiservice.sendNotification(guest, 
        		"호스트가 내게 리뷰를 달았습니다.", 
        		message,
        		"GUESTREVIEW");
        
        redirectAttributes.addFlashAttribute("message", "게스트에 대한 리뷰 작성이 완료되었습니다.");
        
        // 리뷰 작성 후 해당 예약 상세 페이지로 리다이렉트
        return "redirect:/host/reservation/detail/" + reservationId;
    }
}

// ... (HostReviewDto 정의 유지) ...
@Getter
@Setter
class HostReviewDto {
    private int reviewId;
    private String homeTitle;
    private String writerNickname;
    private double rating;
    private String content;
    private java.time.LocalDateTime createdAt;
    private int reservationId;
    private boolean hasReply;
    private Integer replyId;

    public HostReviewDto(int reviewId, String homeTitle, String writerNickname, double rating, String content, java.time.LocalDateTime createdAt, int reservationId, boolean hasReply, Integer replyId) {
        this.reviewId = reviewId;
        this.homeTitle = homeTitle;
        this.writerNickname = writerNickname;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
        this.reservationId = reservationId;
        this.hasReply = hasReply;
        this.replyId = replyId;
    }
}