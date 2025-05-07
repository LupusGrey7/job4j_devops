package ru.job4j.devops.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.devops.models.*;
import ru.job4j.devops.repository.CalcEventRepository;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CalcEventService {

    private final CalcEventRepository repository;

    public CalcEvent add(User user, int first, int second) {
        var createEvent = createCalcEvent(user, first, second);

        createEvent = repository.save(createEvent);
        log.debug("Create new CalcEvent with ID: {}", createEvent);

        return createEvent;
    }

    public void logSomeCalcEvent(CalcEvent event) {
        log.info("Log some CalcEvent info, ID: {}, created date: {}", event.getId(), event.getCreateDate());
    }

    private CalcEvent createCalcEvent(User user, int first, int second) {
        return CalcEvent.builder()
                .user(user)
                .first(first)
                .second(second)
                .createDate(LocalDateTime.now())
                .type(TypeEnum.APPROVED)
                .build();
    }
}
