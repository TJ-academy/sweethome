package com.example.sweethome.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.sweethome.review.Reply;
import com.example.sweethome.review.ReplyRepository;
import com.example.sweethome.review.Review;
import com.example.sweethome.review.ReviewDirection;
import com.example.sweethome.review.ReviewRepository;

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