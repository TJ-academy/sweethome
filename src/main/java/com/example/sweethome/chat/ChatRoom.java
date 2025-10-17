package com.example.sweethome.chat;

import com.example.sweethome.reservation.Reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reservationIdx")
    private Reservation reservation;
}