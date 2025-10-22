package com.example.sweethome.mypage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.sweethome.user.User;
import com.example.sweethome.wishlist.Wishlist;
import com.example.sweethome.wishlist.WishlistFolder;
import com.example.sweethome.wishlist.WishlistFolderRepository;
import com.example.sweethome.wishlist.WishlistRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/mypage")
public class MyWishListController {

	@Autowired
	private WishlistFolderRepository wishlistFolderRepository;
	@Autowired
	private WishlistRepository wishlistRepository;

	// 현재 로그인된 유저의 정보를 세션에서 가져오는 헬퍼 메서드 (가정)
	// 실제 구현에서는 UserDetails 등 스프링 시큐리티를 사용하는 것이 일반적이지만,
	// 주어진 코드 구조에 맞춰 HttpSession을 사용합니다.
	private User getLoggedInUser(HttpSession session) {
		// 세션에서 userProfile (User 객체)을 가져와 형 변환
		Object userProfile = session.getAttribute("userProfile");
		if (userProfile instanceof User) {
			return (User) userProfile;
		}
		// 로그인 정보가 없으면 예외 처리 또는 로그인 페이지 리다이렉션
		throw new IllegalStateException("로그인이 필요합니다.");
	}

	/**
	 * 유저별 위시리스트 폴더 목록을 보여줍니다. (mypage/myWishListFolder.html) 폴더가 없으면 "아직 좋아요 한 숙소가
	 * 없습니다" 메시지를 띄웁니다.
	 */
	@GetMapping("/wishListFolder")
	public String showWishlistFolders(HttpSession session, Model model) {
		try {
			User user = getLoggedInUser(session);

			// ⭐ 이 부분을 추가해야 합니다. 사이드바 등의 Fragment에서 user 객체를 사용하기 때문입니다.
			model.addAttribute("user", user);

			// 1. 유저의 모든 폴더 조회
			List<WishlistFolder> folders = wishlistFolderRepository.findByUser(user);

			// 2. 각 폴더별 좋아요 항목의 개수 조회 (한 번의 쿼리로 처리)
	        List<Object[]> counts = wishlistRepository.countWishlistsByFolderByUser(user);

	        // Map<folderIdx, count> 형태로 변환
	        Map<Long, Long> folderCounts = counts.stream()
	            .collect(Collectors.toMap(
	                arr -> (Long) arr[0], // 폴더 ID (idx)
	                arr -> (Long) arr[1]  // 개수 (count)
	            ));

			// 2. 각 폴더별 좋아요(Wishlist) 항목의 개수 조회 (N+1 문제 발생 가능성 고려)
			// 실제 서비스에서는 DTO나 별도의 최적화된 Repository 메서드를 사용하는 것이 좋습니다.
			// 여기서는 단순함을 위해 WishlistRepository의 findByFolder 메서드가 있다고 가정하고,
			// 별도의 쿼리가 없다면 findAll() 후 스트림으로 그룹핑하여 처리합니다.

			// 현재 WishlistRepository에는 폴더별 조회 메서드가 없으므로,
			// 모든 위시리스트 항목을 가져와 폴더별로 그룹핑하여 개수를 계산합니다. (대량 데이터 시 비효율적)
			// **실제로는 WishlistRepository에 List<Wishlist> findByFolder(WishlistFolder
			// folder); 를 추가해야 합니다.**

			// 간단하게, 폴더가 존재하는지만 확인합니다.
			if (folders.isEmpty()) {
				model.addAttribute("hasFolders", false);
			} else {
				model.addAttribute("hasFolders", true);
				model.addAttribute("folders", folders);
				model.addAttribute("folderCounts", folderCounts);
			}

			// 폴더 목록 페이지 반환
			return "mypage/MyWishListFolder";

		} catch (IllegalStateException e) {
			// 로그인 정보가 없을 경우 로그인 페이지로 리다이렉트
			return "redirect:/user/login";
		}
	}

	/**
	 * 특정 폴더의 좋아요(Wishlist) 숙소 목록을 보여줍니다. (mypage/myWishList.html)
	 * 
	 * @param folderIdx 조회할 폴더의 ID
	 */
	@GetMapping("/wishList")
	public String showWishlistItems(@RequestParam("folderIdx") Long folderIdx, HttpSession session, Model model) {
		try {
			User user = getLoggedInUser(session);

			// ⭐ 이 부분을 추가해야 합니다. 사이드바 등의 Fragment에서 user 객체를 사용하기 때문입니다.
			model.addAttribute("user", user);

			// 1. 폴더 정보 조회
			WishlistFolder folder = wishlistFolderRepository.findById(folderIdx)
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));

			// 2. 해당 폴더의 실제 소유자 이메일 가져오기
			String folderOwnerEmail = folder.getUser().getEmail(); // Lazy Loading 유발 및 이메일 가져오기

			System.out.println("로그인 유저 Email: " + user.getEmail());
			System.out.println("폴더 소유자 Email: " + folder.getUser().getEmail());

			// 3. 권한 체크
			if (!folderOwnerEmail.equals(user.getEmail())) { // ⭐ 세션 User와 DB에서 로드된 소유자 이메일을 비교
				// 권한이 없는 접근
				return "redirect:/mypage/wishListFolder";
			}

			// 3. 해당 폴더에 저장된 위시리스트 항목들 조회
			// 이 로직을 위해 WishlistRepository에 findByFolder 메서드가 필요합니다.
			// **가정:** WishlistRepository에 List<Wishlist> findByFolder(WishlistFolder
			// folder); 가 존재함
			List<Wishlist> wishlists = wishlistRepository.findByFolderWithHome(folder);

			model.addAttribute("folder", folder);
			model.addAttribute("wishlists", wishlists);

			// 위시리스트 항목 페이지 반환
			return "mypage/myWishList";

		} catch (IllegalStateException e) {
			// 로그인 정보가 없을 경우
			return "redirect:/user/login";
		} catch (IllegalArgumentException e) {
			// 폴더가 없거나 잘못된 접근일 경우
			return "redirect:/mypage/wishListFolder";
		}
	}
}