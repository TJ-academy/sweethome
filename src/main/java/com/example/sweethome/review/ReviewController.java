package com.example.sweethome.review;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationRepository;
import com.example.sweethome.reservation.ReservationStatus;
import com.example.sweethome.user.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewKeywordRepository reviewKeywordRepository;
    private final ReviewKeywordMapRepository reviewKeywordMapRepository;
    // private final FileHandlerService fileHandlerService; // 파일저장 쓰면 주입

    /** 리뷰 작성 폼
     *  /review/write?reservationId=1&to=HOST  (게스트가 호스트 평가)
     *  /review/write?reservationId=1&to=GUEST (호스트가 게스트 평가)
     */
    @GetMapping("/write")
    public String write(@RequestParam("reservationId") int reservationId,
                        @RequestParam("to") String to, // HOST | GUEST
                        HttpSession session, Model model) {

        User me = (User) session.getAttribute("userProfile");
        if (me == null) return "redirect:/user/login";

        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        // 상태 확인(보통 COMPLETED 에서 작성)
        if (reservation.getReservationStatus() != ReservationStatus.COMPLETED) {
            model.addAttribute("blockReason", "이 예약은 아직 이용완료 상태가 아니라 리뷰를 작성할 수 없습니다.");
        }

        ReviewDirection direction;
        User targetUser;
        String targetLabel;

        if ("HOST".equalsIgnoreCase(to)) {
            // ✅ 게스트가 호스트에게 쓰는 경우
            if (!reservation.getBooker().getEmail().equals(me.getEmail())) {
                throw new IllegalStateException("게스트만 호스트에 대한 리뷰를 작성할 수 있습니다.");
            }
            direction = ReviewDirection.GUEST_TO_HOST;
            targetUser = reservation.getReservedHome().getHost();
            targetLabel = "호스트";
        } else if ("GUEST".equalsIgnoreCase(to)) {
            // ✅ 호스트가 게스트에게 쓰는 경우
            if (!reservation.getReservedHome().getHost().getEmail().equals(me.getEmail())) {
                throw new IllegalStateException("호스트만 게스트에 대한 리뷰를 작성할 수 있습니다.");
            }
            direction = ReviewDirection.HOST_TO_GUEST;
            targetUser = reservation.getBooker();
            targetLabel = "게스트";
        } else {
            throw new IllegalArgumentException("to 파라미터는 HOST 또는 GUEST 여야 합니다.");
        }

        // 예약 + 방향 중복 작성 방지
        if (reviewRepository.existsByReservationAndDirection(reservation, direction)) {
            throw new IllegalStateException("이미 해당 방향으로 리뷰를 작성하셨습니다.");
        }

        // 키워드 불러오기 (카테고리별)
        var allKeywords = reviewKeywordRepository.findAll();
        var keywordGroups = allKeywords.stream()
            .collect(Collectors.groupingBy(ReviewKeyword::getCategory, LinkedHashMap::new, Collectors.toList()));

        model.addAttribute("reservation", reservation);
        model.addAttribute("home", reservation.getReservedHome());
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("targetLabel", targetLabel); // 화면 문구용
        model.addAttribute("direction", direction.name());
        model.addAttribute("keywordGroups", keywordGroups);

        return "mypage/reviewWrite";
    }

    /** 리뷰 저장 */
    @PostMapping("/write")
    public String save(@RequestParam("reservationId") int reservationId,
                       @RequestParam("direction") String directionStr, // GUEST_TO_HOST | HOST_TO_GUEST
                       @RequestParam("star") int star,
                       @RequestParam("content") String content,
                       @RequestParam(value = "keywordIds", required = false) List<Integer> keywordIds,
                       @RequestParam(value = "reviewThumb", required = false) MultipartFile reviewThumb,
                       @RequestParam(value = "imgOne", required = false) MultipartFile imgOne,
                       @RequestParam(value = "imgTwo", required = false) MultipartFile imgTwo,
                       @RequestParam(value = "imgThree", required = false) MultipartFile imgThree,
                       HttpSession session) {

        User me = (User) session.getAttribute("userProfile");
        if (me == null) return "redirect:/user/login";

        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        ReviewDirection direction = ReviewDirection.valueOf(directionStr);

        // 권한 체크
        if (direction == ReviewDirection.GUEST_TO_HOST) {
            if (!reservation.getBooker().getEmail().equals(me.getEmail())) {
                throw new IllegalStateException("게스트만 호스트에 대한 리뷰를 작성할 수 있습니다.");
            }
        } else { // HOST_TO_GUEST
            if (!reservation.getReservedHome().getHost().getEmail().equals(me.getEmail())) {
                throw new IllegalStateException("호스트만 게스트에 대한 리뷰를 작성할 수 있습니다.");
            }
        }

        // 중복 작성 방지
        if (reviewRepository.existsByReservationAndDirection(reservation, direction)) {
            throw new IllegalStateException("이미 해당 방향으로 리뷰를 작성하셨습니다.");
        }

        // (선택) 파일 저장
        String thumbPath = null, imgOnePath = null, imgTwoPath = null, imgThreePath = null;
        // if (reviewThumb != null && !reviewThumb.isEmpty()) thumbPath = fileHandlerService.saveFile(reviewThumb, "review");
        // if (imgOne != null && !imgOne.isEmpty()) imgOnePath = fileHandlerService.saveFile(imgOne, "review");
        // if (imgTwo != null && !imgTwo.isEmpty()) imgTwoPath = fileHandlerService.saveFile(imgTwo, "review");
        // if (imgThree != null && !imgThree.isEmpty()) imgThreePath = fileHandlerService.saveFile(imgThree, "review");

        Review review = Review.builder()
            .reservation(reservation)
            .home(reservation.getReservedHome())
            .writer(me)
            .direction(direction)
            .content(content)
            .reviewThumb(thumbPath)
            .imgOne(imgOnePath)
            .imgTwo(imgTwoPath)
            .imgThree(imgThreePath)
            .star(star)
            .createdAt(LocalDateTime.now())
            .build();

        Review saved = reviewRepository.save(review);

        // 키워드 매핑
        if (keywordIds != null && !keywordIds.isEmpty()) {
            reviewKeywordRepository.findAllById(keywordIds).forEach(kw -> {
                reviewKeywordMapRepository.save(
                    ReviewKeywordMap.builder().review(saved).keyword(kw).build()
                );
            });
        }

        return "redirect:/review/detail/" + saved.getReviewIdx();
    }
}
