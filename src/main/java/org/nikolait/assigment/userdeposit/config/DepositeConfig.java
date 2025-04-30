package org.nikolait.assigment.userdeposit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Data
@ConfigurationProperties(prefix = "deposit")
public class DepositeConfig {
    private BigDecimal interestRate;
    private BigDecimal maxRate;
}
