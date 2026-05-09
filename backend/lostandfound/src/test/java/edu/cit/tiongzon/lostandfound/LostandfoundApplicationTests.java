package edu.cit.tiongzon.lostandfound;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// Skips database auto-configuration for CI/test runs; the full context
// is exercised by the running server during integration/regression testing.
@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LostandfoundApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring context starts up without errors.
    }

}
