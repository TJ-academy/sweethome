package com.example.sweethome.wishlist;

import com.example.sweethome.home.Home;
import com.example.sweethome.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private WishlistFolderRepository wishlistFolderRepository;

    // 위시리스트에 숙소 추가
    public void addToWishlist(Home home, User user, WishlistFolder folder) {
    	
    	// ⭐️ 중복 체크 로직 추가
        if (wishlistRepository.existsByHomeAndUser(home, user)) {
            // 이미 좋아요가 눌려있으면 예외를 발생시키거나 (권장) 로그를 남기고 리턴할 수 있습니다.
            throw new IllegalStateException("이미 위시리스트에 추가된 숙소입니다."); 
        }
        
        // 위시리스트에 추가
        Wishlist wishlist = new Wishlist(home, user, folder, LocalDateTime.now());
        wishlistRepository.save(wishlist);
    }

    // 폴더 생성
    public WishlistFolder createFolder(User user, String folderName) {
        WishlistFolder folder = new WishlistFolder(user, folderName, LocalDateTime.now());
        return wishlistFolderRepository.save(folder);
    }
}
