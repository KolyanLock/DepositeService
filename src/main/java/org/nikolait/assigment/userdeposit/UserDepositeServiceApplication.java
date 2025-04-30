package org.nikolait.assigment.userdeposit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan
public class UserDepositeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserDepositeServiceApplication.class, args);
    }

}
