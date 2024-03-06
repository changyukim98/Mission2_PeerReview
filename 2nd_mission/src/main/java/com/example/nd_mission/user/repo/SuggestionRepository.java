package com.example.nd_mission.user.repo;

import com.example.nd_mission.usedProducts.entity.UsedProductSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestionRepository extends JpaRepository<UsedProductSuggestion, Long> {
}
