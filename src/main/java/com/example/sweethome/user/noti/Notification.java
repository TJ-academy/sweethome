package com.example.sweethome.user.noti;

import java.time.LocalDateTime;

import com.example.sweethome.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    // id (int, PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // receiver (varchar(50), FK) -> User 엔티티와 ManyToOne 관계 (User.email 참조)
    @ManyToOne 
    @JoinColumn(name = "receiver", referencedColumnName = "email", nullable = false)
    @ToString.Exclude // 💡 toString 호출 시 receiver 필드를 제외
    @JsonIgnore       // 💡 JSON 직렬화 시 이 필드를 제외
    private User receiver;

    // title (varchar(200))
    @Column(length = 200)
    private String title;

    // message (varchar(1000))
    @Column(length = 1000)
    private String message;

    // alertLevel (varchar(20), default INFO) -> Enum으로 매핑
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 20, columnDefinition = "varchar(20) default 'INFO'")
    private AlertLevel alertLevel = AlertLevel.INFO;

    // status (varchar(20), default 'NEW') -> Enum으로 매핑
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 20, columnDefinition = "varchar(20) default 'NEW'")
    private NotificationStatus status = NotificationStatus.NEW;

    // sendAt (date, timestamp)
    @Column(columnDefinition = "timestamp")
    private LocalDateTime sendAt;
}

// 알림 레벨 Enum 정의 (INFO, WARN, CRITICAL)
enum AlertLevel {
    INFO,       // 정보 (기본값)
    WARN,       // 경고
    CRITICAL    // 심각
}

// 알림 상태 Enum 정의 (NEW, READ)
enum NotificationStatus {
    NEW,        // 신규 (기본값)
    READ        // 읽음
}