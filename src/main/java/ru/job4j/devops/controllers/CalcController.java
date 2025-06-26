package ru.job4j.devops.controllers;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.job4j.devops.models.Result;
import ru.job4j.devops.models.TwoArgs;
import ru.job4j.devops.service.ResultService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("calc")
@AllArgsConstructor
public class CalcController {
    private final Logger logger = LoggerFactory.getLogger(CalcController.class);
    private final ResultService resultService;

    @GetMapping("/")
    public ResponseEntity<List<Result>> logs() {
        return ResponseEntity.ok(resultService.findAll());
    }

    @GetMapping("/info")
    public String info() {
        return "Hello from CalcController";
    }

    @PostMapping("/summarise")
    public ResponseEntity<Result> summarise(@RequestBody TwoArgs twoArgs) {
        logger.debug("------> Arg for /summarise IS  TwoArgs: {}", twoArgs);

        var result = new Result();
        result.setFirstArg(twoArgs.getFirst());
        result.setSecondArg(twoArgs.getSecond());
        result.setResult(twoArgs.getFirst() + twoArgs.getSecond());
        result.setOperation("+");
        result.setCreateDate(LocalDate.now());
        logger.info("------> result: {}", result);
        var savedResult = resultService.save(result);

        logger.info("------> savedResult: {}", savedResult);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/times")
    public ResponseEntity<Result> times(@RequestBody TwoArgs twoArgs) {
        var result = twoArgs.getFirst() * twoArgs.getSecond();
        var op = "*";
        var createdResult = new Result(result);
        createdResult.setId(1L);
        createdResult.setFirstArg(twoArgs.getFirst());
        createdResult.setSecondArg(twoArgs.getSecond());
        createdResult.setOperation(op);
        createdResult.setCreateDate(LocalDate.now());
        logger.debug("------> RESULT IS : {}", createdResult);

        return ResponseEntity.ok(createdResult);
    }
}
