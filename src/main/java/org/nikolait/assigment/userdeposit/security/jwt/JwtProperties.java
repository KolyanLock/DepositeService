package org.nikolait.assigment.userdeposit.security.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    @JsonIgnore
    @ToString.Exclude
    private final String secret;
    private final String claim;
    private final int minutes;
}
