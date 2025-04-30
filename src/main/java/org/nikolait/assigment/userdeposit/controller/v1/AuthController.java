package org.nikolait.assigment.userdeposit.controller.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.dto.LoginRequest;
import org.nikolait.assigment.userdeposit.dto.TokenResponse;
import org.nikolait.assigment.userdeposit.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getLogin(), request.getPassword());
        return new TokenResponse(token);
    }
}
