package org.nikolait.assigment.userdeposit.service.impl;

import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.security.jwt.JwtTokenProvider;
import org.nikolait.assigment.userdeposit.security.userdetails.CustomUserDetails;
import org.nikolait.assigment.userdeposit.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    public String login(String login, String password) {
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));
        return tokenProvider.generateToken(((CustomUserDetails) auth.getPrincipal()).getId());
    }

}
