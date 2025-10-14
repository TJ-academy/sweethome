package com.example.sweethome.home;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "site_option")
public class Option {

    // optionId (int, PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int optionId;

    // optionGroup (varchar(50)) - 옵션 그룹 (예: "욕실", "침실 및 세탁", "냉난방")
    @Column(length = 50, nullable = false)
    private String optionGroup;

    // optionName (varchar(100)) - 옵션 이름 (예: "헤어드라이어", "고데기", "샴푸")
    @Column(length = 100, nullable = false) 
    private String optionName;
}