package org.nikolait.assigment.userdeposit.init;

import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.mapper.UserMapper;
import org.nikolait.assigment.userdeposit.repository.UserEsRepository;
import org.nikolait.assigment.userdeposit.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
public class UserDataInitializer implements ApplicationRunner {

    private final DataSource dataSource;
    private final UserRepository userRepository;
    private final UserEsRepository userEsRepository;
    private final UserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() == 0) {
            new ResourceDatabasePopulator(new ClassPathResource("sql/init_user_data.sql")).execute(dataSource);
        }
        if (userEsRepository.count() == 0) {
            var userEsList = userMapper.toUserEsList(userRepository.findAllFetchEmailsAndPhones());
            userEsRepository.saveAll(userEsList);
        }
    }

}
