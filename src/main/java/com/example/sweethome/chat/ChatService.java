package com.example.sweethome.chat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.sweethome.reservation.Reservation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {
	private final ChatMessageRepository mrepo;
	private final ChatRoomRepository rrepo;
	private final ChatUserRepository urepo;
	
	//두 사람이 포함된 채팅방이 있는가
	public Integer isExistTwoUserRoom(String nicknameOne,
			String nicknameTwo) {
		return urepo.findRoomIdWithBothUsers(nicknameOne, nicknameTwo)
	            .orElse(0);
	}
	
	//새로운 채팅방 만들기
	public ChatRoom createNewChatRoom(String nicknameOne, 
			String nicknameTwo, 
			Reservation reservation) {
		var room = new ChatRoom();
		if(reservation == null) {
			rrepo.save(room);
		} else {
			room.setReservation(reservation);
			rrepo.save(room);
		}
		
	    urepo.save(new ChatUser(room.getId(), nicknameOne, null));
	    urepo.save(new ChatUser(room.getId(), nicknameTwo, null));
	    
		return room;
	}
	
	//내 채팅방 목록 조회
	public List<ChatRoomPreviewDTO> getChatRoomsByNickname(String nickname) {
		//사용자의 모든 채팅방 조회
		List<ChatUser> userRooms = urepo.findByNickname(nickname);
		
		//채팅방이 없으면 빈 리스트 반환
		if (userRooms.isEmpty()) {
			return List.of();
		}
		
		//채팅방별 마지막 메시지 조회
		List<ChatRoomPreviewDTO> previews = new ArrayList<>();
		
		for (ChatUser userRoom : userRooms) {
			//채팅방 번호
			Integer roomId = userRoom.getRoomId();
	        
	        //채팅 상대 닉네임
	        String roomName = findChatOtherUser(roomId, nickname).getNickname();

	        //마지막 메시지 가져오기
	        ChatMessage lastMessage = mrepo.findFirstByChatRoom_IdOrderBySendedAtDesc(roomId)
	            .orElse(null);

	        String lastMsgPreview = "-";
	        //마지막 메시지가 있는 채팅방만 보여주기
	        if (lastMessage != null) {
	        	if (lastMessage.getContent() != null && 
	        			!lastMessage.getContent().isEmpty()) {
	                String content = lastMessage.getContent();
	                lastMsgPreview = content.length() > 10 
	                		? content.substring(0, 10) + "..." 
	                		: content;
	            } else {
	                lastMsgPreview = "이미지";
	            }
	        	
	        	int unreadCount = countUnreadMessages(roomId, userRoom.getLastRead());
	        	
	        	ChatRoomPreviewDTO preview = new ChatRoomPreviewDTO(roomId, roomName, lastMsgPreview, lastMessage.getSendedAt(), unreadCount);
		        previews.add(preview);
	        }
		}
		
		//최근 메시지 시간 순으로 내림차순 정렬 (null은 가장 뒤로)
	    previews.sort((a, b) -> {
	        if (a.getLastMessageTime() == null) return 1;
	        if (b.getLastMessageTime() == null) return -1;
	        return b.getLastMessageTime().compareTo(a.getLastMessageTime());
	    });

	    return previews;
    }
	
	//안읽은 메시지 수 계산하기
	public int countUnreadMessages(Integer roomId, Integer lastRead) {
		if (lastRead == null) {
            lastRead = 0; // 초기값 설정 (읽은 메시지 없음)
        }
        return mrepo.countByChatRoom_IdAndIdxGreaterThan(roomId, lastRead);
	}
	
	//한 채팅방의 메시지 조회
	public List<ChatMessage> getMessagesByChatRoom(Integer chatRoomId) {
        return mrepo.findByChatRoom_IdOrderBySendedAtAsc(chatRoomId);
    }
	
	//한 채팅방의 나 조회
	public ChatUser findChatUser(Integer roomId, 
			String myNickname) {
		return urepo.findByRoomIdAndNickname(roomId, myNickname).get();
	}
	
	//한 채팅방의 채팅 상대 조회
	public ChatUser findChatOtherUser(Integer roomId, 
			String myNickname) {
		return urepo.findByRoomIdAndNicknameNot(roomId, myNickname).get();
	}
	
	//한 채팅방 조회
	public ChatRoom findChatRoom(Integer roomId) {
		return rrepo.findById(roomId).get();
	}
	
	//두사람이 있는 채팅방이 있으면 그걸 반환, 없으면 새로 생성
	public ChatRoom getChatRoom(String nicknameOne,
			String nicknameTwo, 
			Reservation reservation) {
		//두사람의 채팅방이 있니?
		Integer roomId = isExistTwoUserRoom(nicknameOne, nicknameTwo);
		ChatRoom room;

		//채팅방이 있으면
        if (roomId != 0) {
            room = findChatRoom(roomId);
        } else { //채팅방이 없으면
            room = createNewChatRoom(nicknameOne, nicknameTwo, reservation);
        }
        
        return room;
	}
	
	//메시지 저장
	public ChatMessage saveMessage(ChatMessageDto dto) {
		ChatRoom chatRoom = findChatRoom(dto.getRoomId());
		
		ChatMessage message = ChatMessage.builder()
				.chatRoom(chatRoom)
				.sender(dto.getSender())
				.content(dto.getContent())
				.img(dto.getImg() != null ? dto.getImg() : "-")
				.sendedAt(Instant.now())
				.build();
		
		return mrepo.save(message);
	}
	
	//마지막으로 읽은 메시지 업데이트
	public void updateLastRead(Integer roomId, String nickname, Integer messageIdx) {
	    ChatUser user = urepo.findByRoomIdAndNickname(roomId, nickname).get();
	    if (user != null) {
	        user.setLastRead(messageIdx);
	        urepo.save(user);
	    }
	}
}