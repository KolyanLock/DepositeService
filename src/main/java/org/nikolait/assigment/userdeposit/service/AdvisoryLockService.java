package org.nikolait.assigment.userdeposit.service;

import org.nikolait.assigment.userdeposit.emum.AdvisoryLockType;

public interface AdvisoryLockService {
    boolean tryAcquirePermanentLock(AdvisoryLockType lockType);
}
