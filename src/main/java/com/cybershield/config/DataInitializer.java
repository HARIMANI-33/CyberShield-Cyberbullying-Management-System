package com.cybershield.config;

import com.cybershield.model.AbusiveWord;
import com.cybershield.repository.AbusiveWordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AbusiveWordRepository abusiveWordRepository;

    public DataInitializer(AbusiveWordRepository abusiveWordRepository) {
        this.abusiveWordRepository = abusiveWordRepository;
    }

    @Override
    public void run(String... args) {
        try {
            if (abusiveWordRepository.count() == 0) {
                List<AbusiveWord> seedWords = List.of(
                        new AbusiveWord("kill yourself", "English", "Severe Harm", "CRITICAL", 9, true),
                        new AbusiveWord("threat", "English", "Threat", "HIGH", 7, true),
                        new AbusiveWord("die", "English", "Harassment", "HIGH", 6, true),
                        new AbusiveWord("harass", "English", "Harassment", "MEDIUM", 4, true),
                        new AbusiveWord("stfu", "English", "Insult", "MEDIUM", 3, true),
                        new AbusiveWord("idiot", "English", "Insult", "LOW", 2, true),
                        new AbusiveWord("ugly", "English", "Body Shaming", "LOW", 2, true),
                        new AbusiveWord("கொன்றுவிடுவேன்", "Tamil", "Severe Harm", "CRITICAL", 9, true),
                        new AbusiveWord("மிரட்டல்", "Tamil", "Threat", "HIGH", 6, true),
                        new AbusiveWord("அசிங்கம்", "Tamil", "Insult", "LOW", 2, true)
                );
                abusiveWordRepository.saveAll(seedWords);
            }
        } catch (Exception e) {
            // Log and allow graceful start if database is not available during offline test phase
            System.err.println("Database seeding skipped or deferred: " + e.getMessage());
        }
    }
}
