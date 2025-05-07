package ru.job4j.devops.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.devops.models.*;
import ru.job4j.devops.repository.CalcEventRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CalcEventService {

    private final CalcEventRepository repository;

    public CalcEvent add(User user, int first, int second) {

        var createEvent = createCalcEvent(user, first, second);

        createEvent = repository.save(createEvent);
        log.debug("Create new CalcEvent with ID: {}", createEvent);

        return createEvent;
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
