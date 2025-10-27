package com.example.sweethome.mypage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationRepository;
import com.example.sweethome.reservation.ReservationStatus;
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
            return "redirect:/mypage/review/detail?reservationId=" + reservationId + "&direction=" + direction;
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

    // ★ 저장 (키워드 삭제 / 이미지만 선택)
    @PostMapping("/review/write")
    @Transactional
    public String submitReview(@RequestParam("reservationId") int reservationId,
                               @RequestParam("direction") ReviewDirection direction,
                               @RequestParam("star") int star,
                               @RequestParam("content") String content,
                               // 이미지 4종(선택). 파일 저장 로직은 프로젝트 공통 유틸에 맞게 채우세요.
                               @RequestParam(value = "reviewThumb", required = false) org.springframework.web.multipart.MultipartFile reviewThumb,
                               @RequestParam(value = "imgOne", required = false) org.springframework.web.multipart.MultipartFile imgOne,
                               @RequestParam(value = "imgTwo", required = false) org.springframework.web.multipart.MultipartFile imgTwo,
                               @RequestParam(value = "imgThree", required = false) org.springframework.web.multipart.MultipartFile imgThree,
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

        // (선택) 파일 저장 – 예시: 파일명을 리턴하는 헬퍼 사용 가정
        String thumb = saveIfPresent(reviewThumb);
        String one   = saveIfPresent(imgOne);
        String two   = saveIfPresent(imgTwo);
        String three = saveIfPresent(imgThree);

        Review review = Review.builder()
                .reservation(r)
                .home(r.getReservedHome())
                .writer(user)
                .direction(direction)
                .star(star)
                .content(content)
                .reviewThumb(thumb)
                .imgOne(one)
                .imgTwo(two)
                .imgThree(three)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        reviewRepository.save(review);

        return "redirect:/mypage/review/detail?reservationId=" + reservationId + "&direction=" + direction;
    }

    private String saveIfPresent(MultipartFile f) {
        if (f == null || f.isEmpty()) return null;

        // 파일 이름 중복 방지용
        String originalName = f.getOriginalFilename();
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String newName = UUID.randomUUID() + ext;

        // 실제 저장 경로
        Path uploadPath = Paths.get("src/main/resources/static/img/review");
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(newName);
            f.transferTo(filePath.toFile());
            System.out.println("리뷰 이미지 저장 완료: " + filePath);
            return newName; // DB에는 파일명만 저장
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

        User viewer = user; // 세션 유저
        User booker = review.getReservation().getBooker();
        User host   = review.getReservation().getReservedHome().getHost();
        boolean isWriter = review.getWriter().getEmail().equals(viewer.getEmail());

        // 방향에 따른 대상자 체크 (게스트→호스트면 대상은 host, 호스트→게스트면 대상은 booker)
        boolean isTarget =
                (review.getDirection() == ReviewDirection.GUEST_TO_HOST  && host.getEmail().equals(viewer.getEmail())) ||
                (review.getDirection() == ReviewDirection.HOST_TO_GUEST   && booker.getEmail().equals(viewer.getEmail()));

        if (!(isWriter || isTarget)) {
            throw new IllegalArgumentException("리뷰 열람 권한이 없습니다.");
        }

        model.addAttribute("user", user);
        model.addAttribute("reservation", r);
        model.addAttribute("review", review);
        model.addAttribute("direction", direction);
        return "mypage/myReviewDetail";
    }
    
    /*
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

        // 모델 바인딩
        model.addAttribute("user", viewer);
        model.addAttribute("reservation", r);
        model.addAttribute("home", r.getReservedHome());
        model.addAttribute("review", review);
        model.addAttribute("direction", direction);
        model.addAttribute("writer", review.getWriter());
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("targetLabel", targetLabel);
        model.addAttribute("isWriter", isWriter); // 수정 버튼 노출용

        return "mypage/myReviewDetail";
    }
    */
}
