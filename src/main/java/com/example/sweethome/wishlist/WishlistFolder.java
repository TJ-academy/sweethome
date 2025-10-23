package com.example.sweethome.wishlist;

import java.time.LocalDateTime;
import java.util.List;

import com.example.sweethome.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx; // 폴더 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_email")
    private User user; // 폴더를 만든 유저

    private String folderName; // 폴더 이름 (예: "여행 예정", "가고 싶은 곳")
    private LocalDateTime createdAt; // 폴더 생성 시간

    @OneToMany(mappedBy = "folder", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wishlist> wishlistItems;
    
    public WishlistFolder(User user, String folderName, LocalDateTime createdAt) {
        this.user = user;
        this.folderName = folderName;
        this.createdAt = createdAt;
    }

    // getter, setter, toString 등 필요에 따라 추가
}
