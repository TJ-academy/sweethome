package com.example.sweethome.chat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sweethome.user.User;

public interface ChatUserRepository extends JpaRepository<ChatUser, ChatUserId> {
	//특정 채팅방의 내가 아닌 사용자 조회
    Optional<ChatUser> findByRoomIdAndUserNot(Integer roomId, User user);
    
    //특정 채팅방의 나 조회
    Optional<ChatUser> findByRoomIdAndUser(Integer roomId, User user);
    
    //특정 채팅방의 나 이메일로 조회
    //Optional<ChatUser> findByRoomIdAndUser(Integer roomId, User user);

    //사용자의 모든 채팅방 조회
    List<ChatUser> findByUser(User user);
    
    //두 사람이 같이 있는 채팅방이 있나요?
    @Query("""
            SELECT cu.roomId
            FROM ChatUser cu
            WHERE cu.user IN (:guest, :host)
            GROUP BY cu.roomId
            HAVING COUNT(DISTINCT cu.user) = 2
    """)
    Optional<Integer> findRoomIdWithBothUsers(
    		@Param("guest") User guest, 
    		@Param("host") User host);
}