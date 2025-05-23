package ru.job4j.devops.controllers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.job4j.devops.models.Result;
import ru.job4j.devops.models.TwoArgs;
import ru.job4j.devops.service.ResultService;
import ru.job4j.devops.service.impl.ResultServiceImpl;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("calc")
@AllArgsConstructor
public class CalcController {

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
        System.out.println("------> twoArgs: " + twoArgs);

        var result = new Result();
        result.setFirstArg(twoArgs.getFirst());
        result.setSecondArg(twoArgs.getSecond());
        result.setResult(twoArgs.getFirst() + twoArgs.getSecond());
        result.setOperation("+");
        result.setCreateDate(LocalDate.now());
        System.out.println("------> result: " + result);
        var res = resultService.save(result);

        System.out.println("------> res: " + res);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/times")
    public ResponseEntity<Result> times(@RequestBody TwoArgs twoArgs) {
        var result = twoArgs.getFirst() * twoArgs.getSecond();
        return ResponseEntity.ok(new Result(result));
    }
}
