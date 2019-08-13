package co.bancolombia.com.oauth.oauth;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OauthApplication {

	public static void main(String[] args) {
		SpringApplication.run(OauthApplication.class, args);
	}

	/*public static void main(String[] args) {
		SpringApplication app = new SpringApplication(OauthApplication.class);
		app.setDefaultProperties(Collections.singletonMap("server.port", "8000"));
		app.run(args);
	}*/
}
