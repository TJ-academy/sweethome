package com.example.sweethome.home;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationOptionRepository extends JpaRepository<AccommodationOption, AccommodationOptionId> {
}
