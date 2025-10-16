package com.example.sweethome.home;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionRepository extends JpaRepository<Option, Integer> {
    List<Option> findAll(); // 그룹핑해서 보여줄 때 사용
    List<Option> findByOptionGroup(String optionGroup);
}
