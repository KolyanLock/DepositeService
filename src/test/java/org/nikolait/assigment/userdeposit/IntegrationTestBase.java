package org.nikolait.assigment.userdeposit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.nikolait.assigment.userdeposit.entity.User;
import org.nikolait.assigment.userdeposit.helper.AuthHelper;
import org.nikolait.assigment.userdeposit.init.UserDataInitializer;
import org.nikolait.assigment.userdeposit.repository.UserEsRepository;
import org.nikolait.assigment.userdeposit.repository.UserRepository;
import org.nikolait.assigment.userdeposit.scheduler.DepositScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.nikolait.assigment.userdeposit.util.TestConstants.*;


@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTestBase {

    // Отключаем планировщик
    @MockitoBean
    private DepositScheduler depositScheduler;

    @Autowired
    private UserDataInitializer userDataInitializer;

    @Autowired
    private UserEsRepository userEsRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected AuthHelper authHelper;

    @Autowired
    protected UserRepository userRepository;

    protected Long user1Id;
    protected Long user2Id;
    protected Long user3Id;

    protected String user1AccessToken;
    protected String user2AccessToken;
    protected String user3AccessToken;

    @BeforeEach
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void setUp() {
        try {
            userDataInitializer.run(new DefaultApplicationArguments());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize user data before test", e);
        }
        List<User> users = userRepository.findAll();
        assertEquals(3, users.size());

        user1Id = userRepository.findByEmailFetchEmailData(USER1_EMAIL).get().getId();
        user2Id = userRepository.findByEmailFetchEmailData(USER2_EMAIL).get().getId();
        user3Id = userRepository.findByEmailFetchEmailData(USER3_EMAIL).get().getId();

        user1AccessToken = authHelper.getAccessToken(USER1_EMAIL, USER1_PASSWORD);
        user2AccessToken = authHelper.getAccessToken(USER2_EMAIL, USER2_PASSWORD);
        user3AccessToken = authHelper.getAccessToken(USER3_EMAIL, USER3_PASSWORD);
    }

    @AfterEach
    public void cleanUp() {
        userEsRepository.deleteAll();
        userRepository.deleteAll();
    }

}
