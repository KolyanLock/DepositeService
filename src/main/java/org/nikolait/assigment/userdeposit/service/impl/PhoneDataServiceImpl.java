package org.nikolait.assigment.userdeposit.service.impl;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.entity.PhoneData;
import org.nikolait.assigment.userdeposit.entity.User;
import org.nikolait.assigment.userdeposit.event.PhoneDataEvent;
import org.nikolait.assigment.userdeposit.exception.DeletionException;
import org.nikolait.assigment.userdeposit.repository.PhoneDataRepository;
import org.nikolait.assigment.userdeposit.service.PhoneDataService;
import org.nikolait.assigment.userdeposit.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhoneDataServiceImpl implements PhoneDataService {

    private final ApplicationEventPublisher eventPublisher;
    private final PhoneDataRepository phoneDataRepository;
    private final UserService userService;


    @Override
    @Transactional
    public void createPhone(String phone) {
        if (phoneDataRepository.existsByPhone(phone)) {
            throw new EntityExistsException("Phone %s is already in use".formatted(phone));
        }

        User user = userService.getCurrentUserFetchPhoneData();

        PhoneData phoneData = PhoneData.builder()
                .phone(phone)
                .user(user)
                .build();

        user.getPhones().add(phoneData);
        eventPublisher.publishEvent(new PhoneDataEvent(user.getId(), user.getPhones()));
    }

    @Override
    @Transactional
    public void updatePhone(Long id, String phone) {
        User user = userService.getCurrentUserFetchPhoneData();

        user.getPhones().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Phone with id %d not found for current User".formatted(id))
                )
                .setPhone(phone);

        eventPublisher.publishEvent(new PhoneDataEvent(user.getId(), user.getPhones()));
    }

    @Override
    @Transactional
    public void deletePhone(Long id) {
        User user = userService.getCurrentUserFetchPhoneData();
        if (user.getPhones().size() == 1) {
            throw new DeletionException("User must have at least 1 phone");
        }
        if (!user.getPhones().removeIf(p -> p.getId().equals(id))) {
            throw new EntityNotFoundException("Phone with id %d not found for current User".formatted(id));
        }
        eventPublisher.publishEvent(new PhoneDataEvent(user.getId(), user.getPhones()));
    }
}
