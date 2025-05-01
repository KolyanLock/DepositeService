package org.nikolait.assigment.userdeposit;

import org.springframework.boot.SpringApplication;

public class TestUserDepositeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(UserDepositeServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
