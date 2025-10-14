package com.example.sweethome.home;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HomePhotoRepository extends JpaRepository<HomePhoto, Integer> {
	HomePhoto findByHome(Home home);  // 숙소와 관련된 사진을 찾는 메서드
}
