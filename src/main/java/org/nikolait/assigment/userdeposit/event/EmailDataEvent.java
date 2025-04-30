package org.nikolait.assigment.userdeposit.event;

import org.nikolait.assigment.userdeposit.entity.EmailData;

import java.util.Set;

public record EmailDataEvent(Long userId, Set<EmailData> emails) {
}
