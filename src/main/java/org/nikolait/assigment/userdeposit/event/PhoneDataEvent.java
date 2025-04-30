package org.nikolait.assigment.userdeposit.event;

import org.nikolait.assigment.userdeposit.entity.PhoneData;

import java.util.Set;

public record PhoneDataEvent(Long userId, Set<PhoneData> phones) {
}
