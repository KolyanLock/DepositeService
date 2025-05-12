package org.nikolait.assigment.userdeposit.emum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdvisoryLockType {
    TRIGGER_ACCRUAL(1);
    private final int key;
}
