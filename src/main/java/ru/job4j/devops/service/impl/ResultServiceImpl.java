package ru.job4j.devops.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.devops.models.Result;
import ru.job4j.devops.repository.ResultRepository;
import ru.job4j.devops.service.ResultService;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ResultServiceImpl implements ResultService {
    private final ResultRepository resultRepository;

    @Override
    public Result save(Result result) {
       var res = resultRepository.save(result);
        log.debug("Create new Result with ID: {}", result);
        return res;
    }

    @Override
    public List<Result> findAll() {
        return resultRepository.findAll();
    }
}
