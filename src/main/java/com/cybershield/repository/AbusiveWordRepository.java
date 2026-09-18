package com.cybershield.repository;

import com.cybershield.model.AbusiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbusiveWordRepository extends JpaRepository<AbusiveWord, Long> {
    List<AbusiveWord> findByActiveTrue();
}
