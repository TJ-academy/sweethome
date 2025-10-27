package com.example.sweethome.chat;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.user.User;
import com.example.sweethome.user.UserRepository;
import com.example.sweethome.util.FileHandlerService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
	private final ChatService service;
	private final UserRepository userrepo;
	private final SimpMessagingTemplate messagingTemplate;
	private final FileHandlerService fileHandlerService;
	
	//내 채팅방 리스트
	@GetMapping("/rooms")
	public String getChatRooms(Model model, 
			HttpSession session) {
		User user = (User) session.getAttribute("userProfile");
		
		List<ChatRoomPreviewDTO> roomsList = service.getChatRoomsByUser(user);
		
		model.addAttribute("roomsList", roomsList);
		return "chat/chatList";
	}
	
	//숙소 상세에서 문의하기 버튼 눌렀을 때
	@PostMapping("/room/question")
    public String createQuestionChatRoom(Model model, 
    		HttpSession session,
    		@RequestParam("host") String hostEmail) {
		User guest = (User) session.getAttribute("userProfile");
		User host = userrepo.findByEmail(hostEmail).get();
		
		//두사람의 채팅방
		ChatRoom room = service.getChatRoom(guest, host, null);
		
		return "redirect:/chat/room/" + room.getId();
    }
	
	//예약 후 채팅방 생성 또는 조회 + 자동 메시지 전송
    public String createReservationChat(Reservation reservation) {
    	User guest = reservation.getBooker();
    	User host = reservation.getReservedHome().getHost();

        //두사람의 채팅방
        ChatRoom room = service.getChatRoom(guest, host, reservation);

        //자동 메시지 전송
        ChatMessageDto dto = new ChatMessageDto();
        dto.setRoomId(room.getId());
        dto.setSenderEmail(host.getEmail());
        dto.setSenderNickname(host.getEmail());
        dto.setReceiverEmail(guest.getEmail());
        dto.setContent("예약이 완료되었습니다. 감사합니다!");
        
        ChatMessage saved = service.saveMessage(dto);
        
        dto.setMsgId(saved.getIdx());
        dto.setSendedAt(saved.getSendedAt());
        
        //실시간 메시지 전송
        messagingTemplate.convertAndSend("/topic/chat/" + room.getId(), dto);

        return "ok";
    }
	
	//한 채팅방에 들어가기
	@GetMapping("/room/{roomId}")
	public String getChat(@PathVariable("roomId") Integer roomId,
			Model model, 
			HttpSession session) {
		User user = (User) session.getAttribute("userProfile");
		
		//채팅방 메시지 조회
		List<ChatMessageDto> messageList = service.getMessagesByChatRoom(roomId);
		//채팅방 내 정보 조회
		ChatUser me = service.findChatUser(roomId, user);
		//채팅방 상대 정보 조회
		ChatUser other = service.findChatOtherUser(roomId, user);
		
		model.addAttribute("myEmail", user.getEmail());
		model.addAttribute("myNickname", user.getNickname());
        model.addAttribute("roomId", roomId);
        model.addAttribute("lastRead", me.getLastRead());
        model.addAttribute("otherEmail", other.getUser().getEmail());
        model.addAttribute("otherNickname", other.getUser().getNickname());
		model.addAttribute("messageList", messageList);
		
		return "chat/chatRoom";
	}
	
	//메시지 전송
	@MessageMapping("/message/send")
    public void handleChatMessage(ChatMessageDto dto) {
        ChatMessage savedMessage = service.saveMessage(dto);
        
        dto.setMsgId(savedMessage.getIdx());
        dto.setSendedAt(savedMessage.getSendedAt());
        dto.setSenderNickname(savedMessage.getSender().getNickname());
        
        messagingTemplate.convertAndSend("/topic/chat/" + dto.getRoomId(), dto);
    }
	
	//이미지 전송
	@PostMapping("/uploadImage")
	@ResponseBody
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
	@ResponseBody
	@PostMapping("/updateLastRead")
	public String updateLastRead(@RequestParam("roomId") Integer roomId,
	                             @RequestParam("msgId") Integer messageIdx,
	                             HttpSession session) {
	    User user = (User) session.getAttribute("userProfile");

	    service.updateLastRead(roomId, user, messageIdx);
	    return "ok";
	}
}