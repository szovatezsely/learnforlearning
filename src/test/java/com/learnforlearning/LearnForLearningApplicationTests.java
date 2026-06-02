package com.learnforlearning;

import static org.assertj.core.api.Assertions.assertThat;

import com.learnforlearning.domain.User;
import com.learnforlearning.recommendation.RecommendationResult;
import com.learnforlearning.recommendation.RecommendationService;
import com.learnforlearning.recommendation.Semester;
import com.learnforlearning.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Boots the whole application context against an in-memory database. This exercises
 * every JPA mapping and custom JPQL query (validated at EntityManagerFactory startup),
 * the security configuration, and the data seeder — then runs one real recommendation.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lfltest;DB_CLOSE_DELAY=-1",
        "app.seed-on-startup=true"
})
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class LearnForLearningApplicationTests {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
        // Seeder populated the in-memory DB on startup.
        assertThat(userRepository.count()).isPositive();
    }

    @Test
    void recommendsAnElectiveForASeededStudent() {
        User admin = userRepository.findByEmail("admin@lfl.hu").orElseThrow();

        RecommendationResult result = recommendationService.recommend(admin.getId(), Semester.SPRING);

        assertThat(result.successful()).isTrue();
        assertThat(result.subject()).isNotNull();
        assertThat(result.subject().isOptionalOn(admin.getSpecialization())).isTrue();
    }
}
