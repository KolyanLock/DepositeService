package org.nikolait.assigment.userdeposit.dto;

import java.math.BigDecimal;

public record AccountResponse(
        BigDecimal deposit,
        BigDecimal capitalization,
        BigDecimal balance
) {
}
