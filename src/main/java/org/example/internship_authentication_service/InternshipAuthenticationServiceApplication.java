package org.example.internship_authentication_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class InternshipAuthenticationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternshipAuthenticationServiceApplication.class, args);
    }

}
