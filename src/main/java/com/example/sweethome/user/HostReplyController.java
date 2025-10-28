package com.example.sweethome.user;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sweethome.review.Reply;
import com.example.sweethome.review.ReplyRepository;
import com.example.sweethome.review.Review;
import com.example.sweethome.review.ReviewDirection;
import com.example.sweethome.review.ReviewRepository;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

// URL: /host/reply
@Controller
@RequestMapping("/host/reply")
@RequiredArgsConstructor
public class HostReplyController {

    private final ReviewRepository reviewRepository;
    private final ReplyRepository replyRepository;

    /**
     * 답글 작성을 위한 폼 객체 (ReplyForm)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReplyForm {
        private int reviewId; // 어떤 리뷰에 대한 답글인지 식별하기 위한 ID
        private String content; // 답글 내용
    }

    // --- 답글 작성 (Write) ---

    /**
     * 답글 작성 페이지 (GET)
     * URL: /host/reply/write?reviewId={reviewId}
     */
    @GetMapping("/write")
    public String showReplyWriteForm(@RequestParam("reviewId") int reviewId, HttpSession session, Model model, RedirectAttributes rttr) {
        User hostUser = (User) session.getAttribute("userProfile");
        if (hostUser == null) {
            return "redirect:/user/login"; 
        }
        
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            rttr.addFlashAttribute("errorMsg", "원래 리뷰를 찾을 수 없습니다.");
            return "redirect:/host/review/list";
        }
        Review review = reviewOpt.get();

        // 이미 답글이 있다면 수정 페이지로 리다이렉트
        Optional<Reply> existingReply = replyRepository.findByReview(review);
        if (existingReply.isPresent()) {
            rttr.addFlashAttribute("errorMsg", "이미 답글이 작성되어 있습니다. 수정 페이지로 이동합니다.");
            return "redirect:/host/reply/edit?replyId=" + existingReply.get().getReplyIdx();
        }

        ReplyForm replyForm = new ReplyForm();
        replyForm.setReviewId(reviewId);
        
        model.addAttribute("review", review);
        model.addAttribute("replyForm", replyForm);
        model.addAttribute("user", hostUser); // 사이드바를 위해 사용자 정보 전달

        return "host/hostReplyWrite";
    }

    /**
     * 답글 저장 처리 (POST)
     */
    @Transactional
    @PostMapping("/write")
    public String processReplyWrite(@ModelAttribute ReplyForm replyForm, HttpSession session, RedirectAttributes rttr) {
        User hostUser = (User) session.getAttribute("userProfile");
        if (hostUser == null) {
            return "redirect:/user/login"; 
        }

        Review review = reviewRepository.findById(replyForm.getReviewId()).orElse(null);
        if (review == null) {
            rttr.addFlashAttribute("errorMsg", "원래 리뷰를 찾을 수 없습니다.");
            return "redirect:/host/review/list";
        }
        
        // Reply 객체 생성 및 저장 (Reply 엔티티의 필드명에 맞춰서 작성)
        Reply newReply = Reply.builder()
                .content(replyForm.getContent())
                .host(hostUser)
                .review(review)
                .createdAt(LocalDateTime.now()) // 가정: 엔티티에 LocalDateTime 필드가 있음
                .updatedAt(LocalDateTime.now()) 
                .build();
        
        replyRepository.save(newReply);
        
        rttr.addFlashAttribute("successMsg", "답글이 성공적으로 작성되었습니다.");
        
        return "redirect:/host/review/list"; 
    }

    // --- 답글 수정 (Edit) ---

    // TODO: 답글 수정 (GET) 및 수정 처리 (POST) 로직을 여기에 추가해야 합니다.
    
    // --- 리뷰 상세 보기 (Detail) ---

    /**
     * 리뷰 상세 보기 페이지 (GET)
     * URL: /host/reply/detail?reservationId={reservationId}
     * 참고: reviewList.html에서 사용하는 링크에 맞춰 /mypage/review/detail?reservationId={r.reservationId}&direction=GUEST_TO_HOST 를 
     * /host/review/detail 로 변경하여 사용한다고 가정합니다.
     */
     @GetMapping("/detail")
     public String showReviewDetail(@RequestParam("reservationId") int reservationId, 
                                     @RequestParam("direction") ReviewDirection direction, 
                                     HttpSession session, Model model, RedirectAttributes rttr) {
        User hostUser = (User) session.getAttribute("userProfile");
        if (hostUser == null) {
            return "redirect:/user/login"; 
        }

        // 1. 해당 예약 ID와 방향으로 리뷰 조회
        // (ReviewRepository에 findByReservationIdAndDirection 같은 메서드가 있다고 가정)
        Optional<Review> reviewOpt = reviewRepository.findByReservationReservationIdxAndDirection(reservationId, direction);
        if (reviewOpt.isEmpty()) {
            rttr.addFlashAttribute("errorMsg", "리뷰를 찾을 수 없습니다.");
            return "redirect:/host/review/list";
        }
        Review review = reviewOpt.get();

        // 2. 답글 정보 조회
        Optional<Reply> replyOpt = replyRepository.findByReview(review);
        
        model.addAttribute("review", review);
        model.addAttribute("reply", replyOpt.orElse(null));
        model.addAttribute("user", hostUser);

        return "host/hostReviewDetail";
     }
     
  // ----------------------------------------
     // --- 답글 수정 (Edit) ---
     // ----------------------------------------

     /**
      * 답글 수정 페이지 (GET)
      * URL: /host/reply/edit?replyId={replyId}
      */
     @GetMapping("/edit")
     public String showReplyEditForm(@RequestParam("replyId") int replyId, HttpSession session, Model model, RedirectAttributes rttr) {
         User hostUser = (User) session.getAttribute("userProfile");
         if (hostUser == null) {
             return "redirect:/user/login"; 
         }

         // 1. 기존 답글 조회
         Optional<Reply> replyOpt = replyRepository.findById(replyId);
         if (replyOpt.isEmpty()) {
             rttr.addFlashAttribute("errorMsg", "수정할 답글을 찾을 수 없습니다.");
             return "redirect:/host/review/list";
         }
         Reply reply = replyOpt.get();

         // 2. 현재 로그인된 호스트가 작성한 답글인지 확인
         // (User 엔티티에 getEmail()이 있다고 가정)
         if (!reply.getHost().getEmail().equals(hostUser.getEmail())) {
             rttr.addFlashAttribute("errorMsg", "답글 수정 권한이 없습니다.");
             return "redirect:/host/review/list";
         }

         // 3. 폼 객체에 기존 데이터 설정
         HostReplyController.ReplyForm replyForm = new HostReplyController.ReplyForm();
         replyForm.setReviewId(reply.getReview().getReviewIdx()); // 원본 리뷰 ID
         replyForm.setContent(reply.getContent()); // 기존 답글 내용

         model.addAttribute("replyId", replyId); // 수정 대상 답글 ID
         model.addAttribute("review", reply.getReview()); // 원본 리뷰 정보
         model.addAttribute("replyForm", replyForm);
         model.addAttribute("user", hostUser); // 사이드바용 사용자 정보

         return "host/hostReplyEdit";
     }

     /**
      * 답글 수정 처리 (POST)
      * URL: /host/reply/edit?replyId={replyId}
      */
     @PostMapping("/edit")
     public String processReplyEdit(@RequestParam("replyId") int replyId, 
                                    @ModelAttribute HostReplyController.ReplyForm replyForm, 
                                    HttpSession session, 
                                    RedirectAttributes rttr) {
         
         User hostUser = (User) session.getAttribute("userProfile");
         if (hostUser == null) {
             return "redirect:/user/login"; 
         }

         Optional<Reply> replyOpt = replyRepository.findById(replyId);
         if (replyOpt.isEmpty()) {
             rttr.addFlashAttribute("errorMsg", "수정 대상 답글을 찾을 수 없습니다.");
             return "redirect:/host/review/list";
         }
         Reply reply = replyOpt.get();

         // 1. 권한 확인 (다시 한번)
         if (!reply.getHost().getEmail().equals(hostUser.getEmail())) {
             rttr.addFlashAttribute("errorMsg", "답글 수정 권한이 없습니다.");
             return "redirect:/host/review/list";
         }

         // 2. 답글 내용 업데이트
         reply.setContent(replyForm.getContent());
         // createdAt은 그대로 두고 updatedAt만 업데이트
         reply.setUpdatedAt(java.time.LocalDateTime.now()); 
         
         replyRepository.save(reply);
         
         rttr.addFlashAttribute("successMsg", "답글이 성공적으로 수정되었습니다.");
         
         // 수정 후 리뷰 목록으로 리다이렉트
         return "redirect:/host/review/list"; 
     }
}