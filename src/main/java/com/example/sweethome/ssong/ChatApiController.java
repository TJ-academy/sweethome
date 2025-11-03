package com.example.sweethome.ssong;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.sweethome.chat.ChatMessage;
import com.example.sweethome.chat.ChatMessageDto;
import com.example.sweethome.chat.ChatRoomPreviewDTO;
import com.example.sweethome.chat.ChatService;
import com.example.sweethome.chat.ChatUser;
import com.example.sweethome.user.User;
import com.example.sweethome.user.UserRepository;
import com.example.sweethome.user.noti.NotificationService;
import com.example.sweethome.util.FileHandlerService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatApiController {
	private final ChatService service;
	private final UserRepository userrepo;
	private final SimpMessagingTemplate messagingTemplate;
	private final FileHandlerService fileHandlerService;
	private final NotificationService notiservice;
	private final JwtUtil jwtUtil;
	
	//쿠키에서 JWT 토큰 추출
	private Optional<String> getTokenFromCookie(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if(cookies == null)
			return Optional.empty();
		// 쿠키 배열을 순회하면서 access_token을 읽음(반복문 대체)
		return Arrays.stream(cookies).filter(c -> "ACCESS_TOKEN"
		.equals(c.getName()))
				.map(Cookie::getValue).findFirst();
	}
	
	//JWT로부터 이메일 추출
	private String extractEmail(HttpServletRequest req) {
		String authHeader = req.getHeader("Authorization");
	    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	        throw new RuntimeException("JWT 토큰이 존재하지 않습니다.");
	    }
	    String token = authHeader.substring(7); // "Bearer " 제거
        try {
            Claims claims = jwtUtil.validateTokenAndGetEmail(token)
            		.getBody();
            return claims.getSubject(); // 이메일 반환
        } catch (Exception e) {
            throw new RuntimeException("JWT 토큰이 유효하지 않습니다.");
        }
    }
	
	//채팅방 리스트
	@GetMapping("/rooms")
	public List<ChatRoomPreviewDTO> getChatRooms(HttpServletRequest req) {
		String email = extractEmail(req);
        User user = userrepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
	    return service.getChatRoomsByUser(user);
	}

	//한 채팅방에 들어가기
	@GetMapping("/rooms/{roomId}")
	public Map<String, Object> getChat(@PathVariable("roomId") Integer roomId,
			HttpServletRequest req) {
		String email = extractEmail(req);
		User user = userrepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
		
		//채팅방 메시지 조회
		List<ChatMessageDto> messageList = service.getMessagesByChatRoom(roomId);
		//채팅방 내 정보 조회
		ChatUser me = service.findChatUser(roomId, user);
		//채팅방 상대 정보 조회
		ChatUser other = service.findChatOtherUser(roomId, user);
		
		Map<String, Object> map = new HashMap<>();
		map.put("myEmail", user.getEmail());
		map.put("myNickname", user.getNickname());
		map.put("roomId", roomId);
		map.put("lastRead", me.getLastRead());
		map.put("otherEmail", other.getUser().getEmail());
		map.put("otherNickname", other.getUser().getNickname());
		map.put("otherProfileImg", other.getUser().getProfileImg());
		map.put("messages", messageList);
		
		return map;
	}
	
	//메시지 전송
	@MessageMapping("/api/message/send")
    public void handleChatMessage(ChatMessageDto dto) {
        ChatMessage savedMessage = service.saveMessage(dto);
        
        dto.setMsgId(savedMessage.getIdx());
        dto.setSendedAt(savedMessage.getSendedAt());
        dto.setSenderNickname(savedMessage.getSender().getNickname());
        
        messagingTemplate.convertAndSend("/topic/chat/" + dto.getRoomId(), dto);
        
        User receiver = userrepo.findByEmail(dto.getReceiverEmail()).get();
        String message = "";
        if (dto.getContent() != null && 
    			!dto.getContent().isEmpty()) {
            String content = dto.getContent();
            message = content.length() > 10 
            		? content.substring(0, 10) + "..." 
            		: content;
        } else {
        	message = "이미지";
        }
        notiservice.sendNotification(receiver, 
        		dto.getSenderNickname() + "님에게 메시지가 왔어요!", 
        		message,
        		"MESSAGE");
    }
	
	//이미지 전송
	@PostMapping("/uploadImage")
	public Map<String, Object> uploadImage(
			@RequestParam("image") MultipartFile file,
			@RequestParam("roomId") Integer roomId) 
			throws IOException {
		String savedUrl = fileHandlerService.saveFile(file, "chat/room_" + roomId);
		
		// 브라우저 캐시 방지용 쿼리 스트링 추가
	    String cacheBustedUrl = savedUrl + "?t=" + System.currentTimeMillis();
		
	    Map<String, Object> map = new HashMap<>();
	    map.put("imgUrl", cacheBustedUrl);
	    map.put("success", "이미지가 저장되었습니다.");

	    return map;
	}
	
	//마지막으로 읽은 메시지 업데이트
	@PostMapping("/updateLastRead")
	public ResponseEntity<?> updateLastRead(
			@RequestParam("roomId") Integer roomId,
			@RequestParam("msgId") Integer messageIdx,
			HttpServletRequest req) {
		String email = extractEmail(req);
		User user = userrepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
	    service.updateLastRead(roomId, user, messageIdx);
	    return ResponseEntity.ok(Map.of("ok", true));
	}
	
	//웹소켓 연결
}