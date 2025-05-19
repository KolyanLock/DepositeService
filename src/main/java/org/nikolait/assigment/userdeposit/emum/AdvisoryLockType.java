package org.nikolait.assigment.userdeposit.emum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdvisoryLockType {
    TRIGGER_ACCRUAL(1),
    INIT_USER_DATA(2);
    private final int key;
}
