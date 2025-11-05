package com.example.sweethome.chat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.user.User;
import com.example.sweethome.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {
	private final ChatMessageRepository mrepo;
	private final ChatRoomRepository rrepo;
	private final ChatUserRepository urepo;
	private final UserRepository userrepo;
	
	//두 사람이 포함된 채팅방이 있는가
	public Integer isExistTwoUserRoom(User userOne,
			User userTwo) {
		return urepo.findRoomIdWithBothUsers(userOne, userTwo)
	            .orElse(0);
	}
	
	//새로운 채팅방 만들기
	public ChatRoom createNewChatRoom(User userOne, 
			User userTwo, 
			Reservation reservation) {
		var room = new ChatRoom();
		if(reservation == null) {
			rrepo.save(room);
		} else {
			room.setReservation(reservation);
			rrepo.save(room);
		}
		
	    urepo.save(new ChatUser(room.getId(), userOne, 0));
	    urepo.save(new ChatUser(room.getId(), userTwo, 0));
	    System.out.println("채팅방이 새로 만들어졌습니다.\n" + room);
		return room;
	}
	
	//내 채팅방 목록 조회
	public List<ChatRoomPreviewDTO> getChatRoomsByUser(User user) {
		//사용자의 모든 채팅방 조회
		List<ChatUser> userRooms = urepo.findByUser(user);
		
		//채팅방이 없으면 빈 리스트 반환
		if (userRooms.isEmpty()) {
			return List.of();
		}
		
		//채팅방별 마지막 메시지 조회
		List<ChatRoomPreviewDTO> previews = new ArrayList<>();
		
		for (ChatUser userRoom : userRooms) {
			//채팅방 번호
			Integer roomId = userRoom.getRoomId();
	        
	        //채팅 상대
			ChatUser otherUser = findChatOtherUser(roomId, user);
	        String roomName = "-";
	        if (otherUser != null) {
	            roomName = otherUser.getUser().getNickname();
	        }

	        //마지막 메시지 가져오기
	        ChatMessage lastMessage = mrepo.findFirstByChatRoom_IdOrderBySendedAtDesc(roomId)
	            .orElse(null);

	        String lastMsgPreview = "-";
	        
	        //채팅 상대 프사
	        String profileImg = otherUser.getUser().getProfileImg();
	        
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
	        	
	        	int unreadCount = countUnreadMessages(roomId, userRoom.getLastRead(), user);

	        	ChatRoomPreviewDTO preview = new ChatRoomPreviewDTO(
	        			roomId, roomName, 
	        			lastMsgPreview, 
	        			lastMessage.getSendedAt(), 
	        			unreadCount,
	        			profileImg);
		        previews.add(preview);
	        } else if (lastMessage == null) {
        		lastMsgPreview = "";
        		ChatRoomPreviewDTO preview = new ChatRoomPreviewDTO(
	        			roomId, roomName, 
	        			lastMsgPreview, 
	        			null, 
	        			0,
	        			profileImg);
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
	public int countUnreadMessages(Integer roomId, Integer lastRead, User currentUser) {
	    if (lastRead == null) lastRead = 0;
	    return mrepo.countByChatRoom_IdAndIdxGreaterThanAndSender_EmailNot(
	            roomId, lastRead, currentUser.getEmail());	}
	
	//한 채팅방의 메시지 조회
	public List<ChatMessageDto> getMessagesByChatRoom(Integer chatRoomId) {
		List<ChatMessage> cmlist =  mrepo.findByChatRoom_IdOrderBySendedAtAsc(chatRoomId);
		
		List<ChatMessageDto> dtolist = new ArrayList<ChatMessageDto>();
		for (ChatMessage cm : cmlist) {
			ChatMessageDto dto = new ChatMessageDto();
			dto.setMsgId(cm.getIdx());
			dto.setRoomId(chatRoomId);
			dto.setSenderEmail(cm.getSender().getEmail());
			dto.setSenderNickname(cm.getSender().getNickname());
			dto.setContent(cm.getContent());
			dto.setImg(cm.getImg() != null ? cm.getImg() : "-");
			dto.setSendedAt(cm.getSendedAt());
			
			dtolist.add(dto);
		}
		
		return dtolist;
    }
	
	//한 채팅방의 나 조회
	public ChatUser findChatUser(Integer roomId, 
			User user) {
		return urepo.findByRoomIdAndUser(roomId, user).get();
	}
	
	//한 채팅방의 채팅 상대 조회
	public ChatUser findChatOtherUser(Integer roomId, 
			User user) {
		return urepo.findByRoomIdAndUserNot(roomId, user).orElse(null);
	}
	
	//한 채팅방 조회
	public ChatRoom findChatRoom(Integer roomId) {
		return rrepo.findById(roomId).get();
	}
	
	//두사람이 있는 채팅방이 있으면 그걸 반환, 없으면 새로 생성
	public ChatRoom getChatRoom(User userOne,
			User userTwo, 
			Reservation reservation) {
		//두사람의 채팅방이 있니?
		Integer roomId = isExistTwoUserRoom(userOne, userTwo);
		System.out.println("두 사람의 채팅방이 있니? : " + roomId);
		ChatRoom room;

		//채팅방이 있으면
        if (roomId != 0) {
            room = findChatRoom(roomId);
        } else { //채팅방이 없으면
            room = createNewChatRoom(userOne, userTwo, reservation);
        }
        
        return room;
	}
	
	//메시지 저장
//	public ChatMessage saveMessage(ChatMessageDto dto) {
//		ChatRoom chatRoom = findChatRoom(dto.getRoomId());
//		User sender = userrepo.findByEmail(dto.getSenderEmail()).get();
//		
//		ChatMessage message = ChatMessage.builder()
//				.chatRoom(chatRoom)
//				.sender(sender)
//				.content(dto.getContent())
//				.img(dto.getImg() != null ? dto.getImg() : "-")
//				.sendedAt(Instant.now())
//				.build();
//		
//		return mrepo.save(message);
//	}
	// 메시지 저장
	public ChatMessage saveMessage(ChatMessageDto dto) {
	    ChatRoom chatRoom = findChatRoom(dto.getRoomId());

	    // ⚙️ 따옴표 제거 + 공백 제거
	    String senderEmail = dto.getSenderEmail().replace("\"", "").trim();

	    // ✅ sender null-safe 처리
	    User sender = userrepo.findByEmail(senderEmail)
	            .orElseThrow(() -> new IllegalArgumentException("❌ Sender not found: " + senderEmail));

	    ChatMessage message = ChatMessage.builder()
	            .chatRoom(chatRoom)
	            .sender(sender)
	            .content(dto.getContent())
	            .img(dto.getImg() != null ? dto.getImg() : "-")
	            .sendedAt(Instant.now())
	            .build();

	    return mrepo.save(message);
	}


	
	//마지막으로 읽은 메시지 업데이트
	public void updateLastRead(Integer roomId, User user, Integer messageIdx) {
	    ChatUser userChat = urepo.findByRoomIdAndUser(roomId, user).get();
	    if (userChat != null) {
	        userChat.setLastRead(messageIdx);
	        urepo.save(userChat);
	    }
	}
}