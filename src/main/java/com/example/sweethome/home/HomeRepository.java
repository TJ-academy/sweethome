package com.example.sweethome.home;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeRepository extends JpaRepository<Home, Integer> {
	List<Home> findByLocationContainingIgnoreCase(String keyword);
}
