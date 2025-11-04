package com.example.sweethome.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentInfo {
    private String imp_uid; //아임포트 결제 고유번호
    private String merchant_uid; //서버에서 발급한 주문&예약 번호
    private int amount; //실제 결제금액 
    private String status; //아임포트 서버 기록 결제 상태     
}