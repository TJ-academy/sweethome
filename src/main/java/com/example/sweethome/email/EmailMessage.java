package com.example.sweethome.email;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmailMessage {
	private String toEmail;
	private String title;
	private String message;
}