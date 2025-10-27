package com.example.sweethome.home;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface HashtagRepository extends JpaRepository<Hashtag, Integer> {
    Optional<Hashtag> findByHome(Home home);
    List<Hashtag> findByHome_Idx(int homeIdx);
    
    @Modifying
    void deleteByHome_Idx(int homeIdx);
}
