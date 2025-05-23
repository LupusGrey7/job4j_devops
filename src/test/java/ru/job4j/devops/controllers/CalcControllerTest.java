package ru.job4j.devops.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import ru.job4j.devops.models.Result;
import ru.job4j.devops.models.TwoArgs;
import ru.job4j.devops.service.impl.ResultFakeService;

import static org.assertj.core.api.Assertions.assertThat;

public class CalcControllerTest {

    @Test
    public void whenOnePlusOneThenTwo() {
        var input = new TwoArgs(1, 1);
        var expected = new Result();
        expected.setResult(2D);

        var output = new CalcController(
                new ResultFakeService()
        ).summarise(input);

        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody().getResult()).isEqualTo(expected.getResult());
    }

    @Test
    public void whenNegativeNumber() {
        var input = new TwoArgs(-1, -1);
        var expected = new Result();
        expected.setResult(-2D);

        var output = new CalcController(
                new ResultFakeService()
        ).summarise(input);

        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody().getResult()).isEqualTo(expected.getResult());
    }

    @Test
    public void whenZeroPlusZero() {
        var input = new TwoArgs(0, 3);
        var expected = new Result();
        expected.setResult(3D);

        var output = new CalcController(
                new ResultFakeService()
        ).summarise(input);

        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody().getResult()).isEqualTo(expected.getResult());
    }

    @Test
    public void whenTwoTimesTwoThenFour() {
        var input = new TwoArgs(2, 2);
        var expected = new Result();
        expected.setResult(4D);

        var output = new CalcController(
                new ResultFakeService()
        ).summarise(input);

        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody().getResult()).isEqualTo(expected.getResult());
    }

    @Test
    public void whenZeroTimesZero() {
        var input = new TwoArgs(0, 0);
        var expected = new Result();
        expected.setResult(0D);

        var output = new CalcController(
                new ResultFakeService()
        ).summarise(input);

        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody().getResult()).isEqualTo(expected.getResult());
    }

    @Test
    public void whenTimesNegatives() {
        var input = new TwoArgs(-3, -3);
        var expected = new Result();
        expected.setResult(-6D);

        var output = new CalcController(
                new ResultFakeService()
        ).summarise(input);

        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody().getResult()).isEqualTo(expected.getResult());
    }
}
