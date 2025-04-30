package org.nikolait.assigment.userdeposit.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikolait.assigment.userdeposit.elastic.UserEs;
import org.nikolait.assigment.userdeposit.entity.User;
import org.nikolait.assigment.userdeposit.event.EmailDataEvent;
import org.nikolait.assigment.userdeposit.event.PhoneDataEvent;
import org.nikolait.assigment.userdeposit.mapper.EmailMapper;
import org.nikolait.assigment.userdeposit.mapper.PhoneMapper;
import org.nikolait.assigment.userdeposit.mapper.UserMapper;
import org.nikolait.assigment.userdeposit.repository.UserEsRepository;
import org.nikolait.assigment.userdeposit.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final UserEsRepository userEsRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailMapper emailMapper;
    private final PhoneMapper phoneMapper;

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailDataEvent(EmailDataEvent event) {
        UserEs userEs = userEsRepository.findById(event.userId()).orElse(null);

        if (userEs == null) {
            log.warn("User {} not found in ES during EmailDataEvent, trying to sync from DB", event.userId());
            synchronizeUser(event.userId());
            return;
        }

        userEs.setEmails(emailMapper.toEmailDataEsList(event.emails()));
        userEsRepository.save(userEs);
    }

    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmailDataEvent(PhoneDataEvent event) {
        UserEs userEs = userEsRepository.findById(event.userId()).orElse(null);

        if (userEs == null) {
            log.warn("User {} not found in ES during PhoneDataEvent, trying to sync from DB", event.userId());
            synchronizeUser(event.userId());
            return;
        }

        userEs.setPhones(phoneMapper.toPhoneDataEsList(event.phones()));
        userEsRepository.save(userEs);
    }

    private void synchronizeUser(Long userid) {
        User user = userRepository.findByIdFetchEmailsAndPhones(userid).orElse(null);
        if (user == null) {
            userEsRepository.deleteById(userid);
            log.error("User {} not found in DB, removed from ES", userid);
            return;
        }
        userEsRepository.save(userMapper.toUserEs(user));
    }
}
