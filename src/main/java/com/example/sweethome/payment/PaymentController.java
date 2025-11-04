package com.example.sweethome.payment;

import com.example.sweethome.payment.IamportResponse;
import com.example.sweethome.payment.PaymentInfo;
import com.example.sweethome.payment.PaymentVerificationDto;
import com.example.sweethome.payment.IamportService;
import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationStatus;
import com.example.sweethome.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// 이 컨트롤러는 REST API로 동작하며, 클라이언트의 fetch/ajax 요청을 처리합니다.
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    
    // 주입 (DI)
    private final IamportService iamportService;
    private final ReservationRepository reservationRepository; 

    // 클라이언트로부터 전달받을 DTO (imp_uid, merchant_uid)
    // 별도 파일로 분리하지 않고 내부 클래스로 정의하는 이전 프로젝트 스타일을 유지합니다.
    public static class PaymentVerificationDto {
        public String imp_uid;
        public String merchant_uid;
    }
    
    /**
     * 최종 결제 검증 및 DB 업데이트 엔드포인트.
     * 클라이언트(프론트엔드)에서 결제 성공 후 호출됩니다.
     * * @param verifyDto 결제 고유번호(imp_uid)와 예약 번호(merchant_uid)
     * @return 검증 결과 (success/error)
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyPayment(@RequestBody PaymentVerificationDto verifyDto) {
        // 응답 데이터는 Map 형태로 구성하여 JSON으로 반환합니다.
        Map<String, String> response = new HashMap<>();
        
        try {
            // 1. Access Token 발급 (아임포트 API 접근 권한 획득)
            String accessToken = iamportService.getAccessToken();
            if (accessToken == null) {
                 response.put("status", "error");
                 response.put("message", "아임포트 토큰 발급 실패");
                 return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 2. 아임포트 서버에서 imp_uid로 실제 결제 정보 조회
            IamportResponse<PaymentInfo> paymentInfoResponse = iamportService.getPaymentInfo(accessToken, verifyDto.imp_uid);
            
            if (paymentInfoResponse == null || paymentInfoResponse.getCode() != 0 || paymentInfoResponse.getResponse() == null) {
                 response.put("status", "error");
                 response.put("message", "아임포트 결제 정보 조회 실패");
                 return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            PaymentInfo paymentInfo = paymentInfoResponse.getResponse();
            // 아임포트 서버에 기록된 실제 결제 금액 및 상태
            int actualAmount = paymentInfo.getAmount();
            String status = paymentInfo.getStatus(); // "paid"여야 정상 결제

            // 3. DB에서 Reservation 정보 조회 (Merchant UID 기반)
            Optional<Reservation> reservationOptional = reservationRepository.findByMerchantUid(verifyDto.merchant_uid);

            if (reservationOptional.isEmpty()) {
                 response.put("status", "error");
                 response.put("message", "DB에 존재하지 않는 예약 번호입니다.");
                 // 중요: 이 경우 실제 결제가 되었더라도 DB에 주문이 없으므로,
                 // 관리자 확인 및 환불 처리가 필요할 수 있습니다.
                 return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
            Reservation savedReservation = reservationOptional.get();
            int savedAmount = savedReservation.getTotalMoney(); // DB에 저장된 총 예약 금액

            // 4. 결제 위변조 검증 (Validation)
            // (1) 금액 일치 여부 확인 AND (2) 아임포트 상태가 "paid"(결제 완료)인지 확인
            if (savedAmount == actualAmount && "paid".equals(status)) {
                
                // 검증 성공: DB 업데이트
                savedReservation.setImpUid(verifyDto.imp_uid); // 결제 고유 번호 저장
                savedReservation.setReservationStatus(ReservationStatus.CONFIRMED); // 상태를 '확정'으로 변경
                reservationRepository.save(savedReservation);

                response.put("status", "success");
                response.put("message", "결제 성공 및 예약 확정 완료");
                return new ResponseEntity<>(response, HttpStatus.OK);
                
            } else {
                // 금액 불일치(위변조) 또는 결제 실패 상태인 경우
                response.put("status", "error");
                response.put("message", "결제 위변조가 의심되거나 결제 상태가 'paid'가 아닙니다.");
                // 중요: 이 경우 위변조로 간주하며, 아임포트 취소 API를 호출하여 해당 결제를 즉시 취소하는 로직이 추가되어야 합니다.
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "결제 검증 중 서버 오류가 발생했습니다: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}