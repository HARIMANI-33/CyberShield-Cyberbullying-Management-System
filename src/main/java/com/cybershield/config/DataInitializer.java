package com.cybershield.config;

import com.cybershield.model.AbusiveWord;
import com.cybershield.model.User;
import com.cybershield.repository.AbusiveWordRepository;
import com.cybershield.repository.UserRepository;
import com.cybershield.service.AbusiveWordsExcelImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AbusiveWordRepository abusiveWordRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AbusiveWordsExcelImporter abusiveWordsExcelImporter;

    public DataInitializer(AbusiveWordRepository abusiveWordRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AbusiveWordsExcelImporter abusiveWordsExcelImporter) {
        this.abusiveWordRepository = abusiveWordRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.abusiveWordsExcelImporter = abusiveWordsExcelImporter;
    }

    @Override
    public void run(String... args) {
        try {
            // STEP 2: Import and synchronize Excel knowledge dataset into database
            log.info("Synchronizing abusive words dataset from Excel...");
            abusiveWordsExcelImporter.importDatasetFromExcel();

            // Preserve additional existing baseline words if not already present (no duplicates)
            List<AbusiveWord> seedWords = List.of(
                    new AbusiveWord("kill yourself", "English", "Severe Harm", "CRITICAL", 9, "Immediate Attention", true),
                    new AbusiveWord("threat", "English", "Threat", "HIGH", 7, "Immediate Attention", true),
                    new AbusiveWord("die", "English", "Harassment", "HIGH", 6, "Immediate Attention", true),
                    new AbusiveWord("harass", "English", "Harassment", "MEDIUM", 4, "Normal Review", true),
                    new AbusiveWord("stfu", "English", "Insult", "MEDIUM", 3, "Normal Review", true),
                    new AbusiveWord("idiot", "English", "Insult", "LOW", 2, "Normal Review", true),
                    new AbusiveWord("ugly", "English", "Body Shaming", "LOW", 2, "Normal Review", true),
                    new AbusiveWord("கொன்றுவிடுவேன்", "Tamil", "Severe Harm", "CRITICAL", 9, "Immediate Attention", true),
                    new AbusiveWord("மிரட்டல்", "Tamil", "Threat", "HIGH", 6, "Immediate Attention", true),
                    new AbusiveWord("அசிங்கம்", "Tamil", "Insult", "LOW", 2, "Normal Review", true)
            );

            for (AbusiveWord w : seedWords) {
                if (!abusiveWordRepository.existsByKeywordIgnoreCase(w.getKeyword())) {
                    abusiveWordRepository.save(w);
                }
            }

            // Seed demo student user with BCrypt hashed password
            if (!userRepository.existsByEmail("student@cybershield.edu")) {
                User demoUser = new User();
                demoUser.setName("Aravind Kumar");
                demoUser.setEmail("student@cybershield.edu");
                demoUser.setPhone("9840123456");
                demoUser.setPassword(passwordEncoder.encode("password123"));
                demoUser.setRole("USER");
                userRepository.save(demoUser);
            }

            // Seed demo admin / officer user with BCrypt hashed password
            if (!userRepository.existsByEmail("admin@cybershield.gov")) {
                User adminUser = new User();
                adminUser.setName("Inspector K. Raman");
                adminUser.setEmail("admin@cybershield.gov");
                adminUser.setPhone("9840999888");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setRole("ADMIN");
                userRepository.save(adminUser);
            }

            log.info("CyberShield initialization complete. Active abusive words in DB: {}",
                    abusiveWordRepository.findByActiveTrue().size());

        } catch (Exception e) {
            log.error("Database initialization error: {}", e.getMessage(), e);
        }
    }
}
