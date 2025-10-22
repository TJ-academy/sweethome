package com.example.sweethome.wishlist;

import java.time.LocalDateTime;

import com.example.sweethome.home.Home;
import com.example.sweethome.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Wishlist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx; // 기본 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_idx")
    private Home home; // 좋아요한 숙소

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_email")
    private User user; // 좋아요를 누른 유저

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_idx")
    private WishlistFolder folder; // 좋아요가 저장된 폴더

    private LocalDateTime likedAt; // 좋아요 누른 시간

    public Wishlist(Home home, User user, WishlistFolder folder, LocalDateTime likedAt) {
        this.home = home;
        this.user = user;
        this.folder = folder;
        this.likedAt = likedAt;
    }

    // getter, setter, toString 등 필요에 따라 추가
}
