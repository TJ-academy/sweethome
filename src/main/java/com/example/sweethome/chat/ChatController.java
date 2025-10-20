package com.example.sweethome.chat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.user.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
	private final ChatService service;
	private final SimpMessagingTemplate messagingTemplate;
	
	//내 채팅방 리스트
	@GetMapping("/rooms")
	public String getChatRooms(Model model, 
			HttpSession session) {
		User user = (User) session.getAttribute("userProfile");
		String userNickname = user.getNickname();
		
		List<ChatRoomPreviewDTO> roomsList = service.getChatRoomsByNickname(userNickname);
		
		model.addAttribute("roomsList", roomsList);
		return "chat/chatList";
	}
	
	//숙소 상세에서 문의하기 버튼 눌렀을 때
	@PostMapping("/room/question")
    public String createQuestionChatRoom(Model model, 
    		HttpSession session,
    		@RequestParam("nickname") String hostNickname) {
		User guest = (User) session.getAttribute("userProfile");
		String guestNickname = guest.getNickname();
		
		//두사람의 채팅방
		ChatRoom room = service.getChatRoom(guestNickname, hostNickname, null);
		
		return "redirect:/chat/room/" + room.getId();
    }
	
	//예약 후 채팅방 생성 또는 조회 + 자동 메시지 전송
    public String createReservationChat(Reservation reservation) {
        String guest = reservation.getBooker().getNickname();
        String host = reservation.getReservedHome().getHost().getNickname();

        //두사람의 채팅방
        ChatRoom room = service.getChatRoom(guest, host, reservation);

        //자동 메시지 전송
        ChatMessageDto autoMsg = new ChatMessageDto();
        autoMsg.setRoomId(room.getId());
        autoMsg.setSender(host);
        autoMsg.setContent("예약이 완료되었습니다. 감사합니다!");

        ChatMessage saved = service.saveMessage(autoMsg);

        //실시간 메시지 전송
        messagingTemplate.convertAndSend("/topic/chat/" + room.getId(), saved);

        return "ok";
    }
	
	//한 채팅방에 들어가기
	@GetMapping("/room/{roomId}")
	public String getChat(@PathVariable("roomId") Integer roomId,
			Model model, 
			HttpSession session) {
		User user = (User) session.getAttribute("userProfile");
		String userNickname = user.getNickname();
		
		//채팅방 메시지 조회
		List<ChatMessage> messageList = service.getMessagesByChatRoom(roomId);
		//채팅방 내 정보 조회
		ChatUser me = service.findChatUser(roomId, userNickname);
		//채팅방 상대 정보 조회
		ChatUser other = service.findChatOtherUser(roomId, userNickname);
		
		model.addAttribute("myNickname", userNickname);
        model.addAttribute("roomId", roomId);
        model.addAttribute("lastRead", me.getLastRead());
        model.addAttribute("otherNickname", other.getNickname());
		model.addAttribute("messageList", messageList);
		
		return "chat/chatRoom";
	}
	
	//메시지 전송
	@MessageMapping("/message/send")
    public void handleChatMessage(ChatMessageDto dto) {
        ChatMessage savedMessage = service.saveMessage(dto);
        messagingTemplate.convertAndSend("/topic/chat/" + dto.getRoomId(), savedMessage);
    }
	
	//이미지 전송
	@PostMapping("/uploadImage")
	public Map<String, String> uploadImage(
			@RequestParam("image") MultipartFile file,
			@RequestParam("roomId") Integer roomId) 
			throws IOException {
		//이미지 저장 경로
		String uploadDir = "src/main/resources/static/img/chat/room_" + roomId;
	    Path uploadPath = Paths.get(uploadDir);
	    
	    //폴더 없나?
	    if (!Files.exists(uploadPath)) {
	        Files.createDirectories(uploadPath);
	    }
	    
	    //파일 이름 설정
	    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
	    Path filePath = uploadPath.resolve(fileName);
	    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

	    // 클라이언트가 접근할 수 있는 URL
	    String fileUrl = "/img/chat/room_" + roomId + "/" + fileName;

	    return Map.of("url", fileUrl);
	}
}