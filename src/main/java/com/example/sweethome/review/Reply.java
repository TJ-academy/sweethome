package com.example.sweethome.review;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.example.sweethome.user.User; // User 엔티티 import

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "reply", 
uniqueConstraints = {
    @UniqueConstraint(columnNames = {"review_id"}, name = "UQ_REVIEW_REPLY")
})
public class Reply {

    // replyIdx (int, PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int replyIdx;

    @OneToOne
    @JoinColumn(name = "reviewId", referencedColumnName = "reviewIdx", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;

    // hostId (varchar(50), FK) -> User 엔티티와 ManyToOne 관계 (User.email 참조)
    @ManyToOne
    @JoinColumn(name = "hostId", referencedColumnName = "email", nullable = false)
    private User host;

    // content (varchar(500))
    @Column(length = 500, nullable = false)
    private String content;

    // createdAt (date, timestamp) -> 작성일자
    @Column(columnDefinition = "timestamp")
    private LocalDateTime createdAt;
    
    // updatedAt (date) -> 수정일자
    private LocalDateTime updatedAt;
}