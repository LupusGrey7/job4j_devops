package ru.job4j.devops.service.impl;

import ru.job4j.devops.models.Result;
import ru.job4j.devops.service.ResultService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultFakeService implements ResultService {
    private final Map<Long, Result> mem = new HashMap<>();
    private long genId = 0;

    @Override
    public Result save(Result result) {
        mem.put(genId++, result);
        return result;
    }

    @Override
    public List<Result> findAll() {
        return new ArrayList<>(mem.values());
    }
}