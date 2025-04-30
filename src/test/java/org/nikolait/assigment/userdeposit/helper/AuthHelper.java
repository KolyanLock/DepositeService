package org.nikolait.assigment.userdeposit.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.dto.LoginRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public String getAccessToken(String email, String password) {
        try {
            MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new LoginRequest(email, password)))
            ).andReturn();
            String responseContent = result.getResponse().getContentAsString();
            return objectMapper.readTree(responseContent).get("token").asText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
