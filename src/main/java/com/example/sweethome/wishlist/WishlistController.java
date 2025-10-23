package com.example.sweethome.wishlist;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sweethome.home.Home;
import com.example.sweethome.home.HomeRepository;
import com.example.sweethome.user.User;
import com.example.sweethome.user.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private WishlistFolderRepository wishlistFolderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HomeRepository homeRepository;
    @Autowired
    private WishlistRepository wishlistRepository;
    
 // 헬퍼 메서드 (DetailController에서 복사하거나 공통으로 관리)
    private User getLoggedInUser(HttpSession session) {
        Object userProfile = session.getAttribute("userProfile");
        if (userProfile instanceof User) {
            return (User) userProfile;
        }
        throw new IllegalStateException("로그인이 필요합니다.");
    }

 // 좋아요 버튼 클릭 시 로그인 여부 확인
    @GetMapping("/home/like")
    public String addToWishlist(@RequestParam("homeId") int homeId, HttpSession session) {
        // 세션에서 userProfile 확인
        Object userProfile = session.getAttribute("userProfile");

        // 로그인하지 않았다면 로그인 페이지로 리다이렉트
        if (userProfile == null) {
        	// ⭐️ 핵심 수정: prevPage 세션에 상세 페이지 URL 저장
            // 좋아요 요청이 들어왔을 때, 실제 돌아가고 싶은 곳은 상세 페이지입니다.
            String detailPageUrl = "/home/detail/" + homeId;
            session.setAttribute("prevPage", detailPageUrl);
            
            return "redirect:/user/login"; // 로그인 페이지로 리다이렉트
        }

        // 로그인된 사용자가 좋아요를 눌렀을 경우 처리
        // 예: 위시리스트에 추가하는 코드 또는 폴더 선택 페이지로 리다이렉트
        return "redirect:/wishlist/folderSelect?homeId=" + homeId; // 폴더 선택 페이지로 이동
    }
    // 1. 폴더 생성 화면
    @GetMapping("/wishlist/folder")
    public String showCreateFolderPage() {
        return "wishlist/createFolder";
    }

    // 2. 폴더 생성 처리
    @PostMapping("/wishlist/folder")
    public String createFolder(@RequestParam("folderName") String folderName, HttpSession session, Model model) {
        User user = (User) session.getAttribute("userProfile");
        
        // 폴더 생성
        WishlistFolder folder = new WishlistFolder(user, folderName, LocalDateTime.now());
        wishlistFolderRepository.save(folder);

        model.addAttribute("message", "폴더가 생성되었습니다.");
        return "redirect:/wishlist/folder";  // 폴더 생성 후 다시 폴더 페이지로 리다이렉트
    }

    // 3. 위시리스트 추가 처리
    @PostMapping("/wishlist/add")
    public String addToWishlist(@RequestParam("homeIdx") int homeIdx,
                                @RequestParam("folderId") Long folderId,
                                @RequestParam("userEmail") String userEmail, HttpSession session) {

        // 1. 유저 정보 확인
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        // 2. 홈 정보 가져오기
        Home home = homeRepository.findById(homeIdx)
                .orElseThrow(() -> new IllegalArgumentException("숙소가 존재하지 않습니다."));

        // 3. 폴더 정보 가져오기
        WishlistFolder folder = wishlistFolderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더가 존재하지 않습니다."));

        // 4. 위시리스트에 추가
        wishlistService.addToWishlist(home, user, folder);

        return "redirect:/home/detail/" + homeIdx; // 상세 페이지로 돌아가거나, 홈으로 리다이렉트
    }
    
 // 4. 새 폴더 생성 및 위시리스트 추가 처리 (새로운 매핑)
    @PostMapping("/wishlist/createAndAdd")
    public String createAndAddToWishlist(@RequestParam("homeIdx") int homeIdx,
                                         @RequestParam("newFolderName") String newFolderName,
                                         @RequestParam("userEmail") String userEmail) {
        
        // 1. 유저 정보 확인
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        // 2. 홈 정보 가져오기
        Home home = homeRepository.findById(homeIdx)
                .orElseThrow(() -> new IllegalArgumentException("숙소가 존재하지 않습니다."));

        // 3. 새 폴더 생성
        // WishlistService에 폴더 생성 로직이 있으므로 재사용
        WishlistFolder newFolder = wishlistService.createFolder(user, newFolderName); 

        // 4. 위시리스트에 추가
        wishlistService.addToWishlist(home, user, newFolder);

        // 성공 후 리다이렉트
        return "redirect:/home/detail/" + homeIdx; // 상세 페이지로 돌아가거나, 홈으로 리다이렉트
    }
    
 // ⭐️ 좋아요 취소 (새로운 로직)
    @PostMapping("/wishlist/remove")
    public String removeWishlist(@RequestParam("homeIdx") int homeIdx,
                                 RedirectAttributes rttr, HttpSession session) {
        
        try {
            User user = getLoggedInUser(session);
            Home home = homeRepository.findById(homeIdx)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 숙소입니다."));
            
            // 삭제 처리
            long deletedCount = wishlistRepository.deleteByHomeAndUser(home, user);
            
            if (deletedCount > 0) {
                rttr.addFlashAttribute("message", "위시리스트에서 제거되었습니다.");
            } else {
                rttr.addFlashAttribute("error", "해당 위시리스트 항목을 찾을 수 없습니다.");
            }
            
        } catch (IllegalStateException e) {
            return "redirect:/user/login"; // 로그인 필요
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "좋아요 취소 중 오류가 발생했습니다.");
        }
        
        // 상세 페이지로 리다이렉트
        return "redirect:/home/detail/" + homeIdx;
    }
}
