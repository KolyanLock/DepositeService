package org.nikolait.assigment.userdeposit.security.userdetails;

import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Оптимизировано с учётом того что большинство пользователей входит по email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return userRepository.findByEmail(login)
                .or(() -> userRepository.findByPhone(login))
                .map(user -> new CustomUserDetails(user.getId(), login, user.getPassword()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
