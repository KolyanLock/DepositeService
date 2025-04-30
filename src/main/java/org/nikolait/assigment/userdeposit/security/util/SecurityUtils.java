package org.nikolait.assigment.userdeposit.security.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class SecurityUtils {

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
        }
        return ((Long) auth.getPrincipal());
    }
}
