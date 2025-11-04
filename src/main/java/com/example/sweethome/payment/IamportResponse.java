package com.example.sweethome.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IamportResponse<T> {
	// 제네릭 <T>를 사용하여, response 필드에 IamportAccessToken이나 PaymentInfo 등 
	// 다양한 타입을 담도록 함.
    private int code; //응답 코드 (0이면 성공, -1 등은 오류 코드)
    private String message; //응답 메시지 (오류 발생 시 오류 메시지)
    private T response; //실제 응답 데이터 (T 타입, 예: PaymentInfo, IamportAccessToken)
}