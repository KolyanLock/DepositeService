package org.nikolait.assigment.userdeposit.service.impl;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.entity.EmailData;
import org.nikolait.assigment.userdeposit.entity.User;
import org.nikolait.assigment.userdeposit.event.EmailDataEvent;
import org.nikolait.assigment.userdeposit.exception.DeletionException;
import org.nikolait.assigment.userdeposit.repository.EmailDataRepository;
import org.nikolait.assigment.userdeposit.service.EmailDataService;
import org.nikolait.assigment.userdeposit.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailDataServiceImpl implements EmailDataService {

    private final ApplicationEventPublisher eventPublisher;
    private final EmailDataRepository emailDataRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void createEmail(String email) {
        if (emailDataRepository.existsByEmail(email)) {
            throw new EntityExistsException("Email %s is already in use".formatted(email));
        }

        User user = userService.getCurrentUserFetchEmailData();

        EmailData emailData = EmailData.builder()
                .email(email)
                .user(user)
                .build();

        user.getEmails().add(emailData);
        eventPublisher.publishEvent(new EmailDataEvent(user.getId(), user.getEmails()));
    }

    @Override
    @Transactional
    public void updateEmail(Long id, String email) {
        User user = userService.getCurrentUserFetchEmailData();

        user.getEmails().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Email with id %d not found for current User".formatted(id))
                )
                .setEmail(email);

        eventPublisher.publishEvent(new EmailDataEvent(user.getId(), user.getEmails()));
    }

    @Override
    @Transactional
    public void deleteEmail(Long id) {
        User user = userService.getCurrentUserFetchEmailData();
        if (user.getEmails().size() == 1) {
            throw new DeletionException("User must have at least 1 email");
        }
        if (!user.getEmails().removeIf(emailData -> emailData.getId().equals(id))) {
            throw new EntityNotFoundException("Email with id %d not found for current User".formatted(id));
        }
        eventPublisher.publishEvent(new EmailDataEvent(user.getId(), user.getEmails()));
    }

}
