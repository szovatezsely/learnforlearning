package com.learnforlearning.repository;

import com.learnforlearning.domain.Calculation;
import com.learnforlearning.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CalculationRepository extends JpaRepository<Calculation, Long> {

    List<Calculation> findByUserOrderByCreatedAtDesc(User user);

    @Transactional
    void deleteByUser(User user);
}
