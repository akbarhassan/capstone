package com.ga.capstone;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"jwt-secret=test-secret-key-for-jwt-must-be-long-enough-123456",
	"jwt-expiration-ms=86400000",
	"app.mail.from=test@test.com",
	"spring.mail.host=localhost",
	"spring.mail.port=3025",
	"app.upload.path=./test-uploads",
	"app.verification.token-expiry-hours=24",
	"app.verification.base-url=http://localhost:8080",
	"app.password-reset.token-expiry-hours=1",
	"app.password-reset.base-url=http://localhost:8080",
	"app.auth.default-role-id=2",
	"app.password-history.check-recent-count=10"
})
class CapstoneApplicationTests {

	@Test
	void contextLoads() {
	}

}
