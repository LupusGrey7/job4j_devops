package ru.job4j.devops.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import ru.job4j.devops.models.Result;
import ru.job4j.devops.models.TwoArgs;

import static org.assertj.core.api.Assertions.assertThat;

class CalcControllerTest {

    @Test
    void whenOnePlusOneThenTwo() {
        var input = new TwoArgs(1, 1);
        var expected = new Result(2);
        var output = new CalcController().summarise(input);
        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody()).isEqualTo(expected);
    }

    @Test
    void whenOnePlusTwoThenThree() {
        var input = new TwoArgs(1, 2);
        var expected = new Result(3);
        var output = new CalcController().summarise(input);
        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody()).isEqualTo(expected);
    }

    @Test
    void whenNegativeNumber() {
        var input = new TwoArgs(-1, -1);
        var expected = new Result(-2);
        var output = new CalcController().summarise(input);
        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody()).isEqualTo(expected);
    }

    @Test
    void whenZeroPlusZero() {
        var input = new TwoArgs(0, 3);
        var expected = new Result(3);
        var output = new CalcController().summarise(input);
        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody()).isEqualTo(expected);
    }

    @Test
    void whenTwoTimesTwoThenFour() {
        var input = new TwoArgs(2, 2);
        var expected = new Result(4);
        var output = new CalcController().times(input);
        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody()).isEqualTo(expected);
    }

    @Test
    void whenZeroTimesZero() {
        var input = new TwoArgs(0, 0);
        var expected = new Result(0);
        var output = new CalcController().times(input);
        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody()).isEqualTo(expected);
    }

    @Test
    void whenTimesNegatives() {
        var input = new TwoArgs(-3, -3);
        var expected = new Result(9);
        var output = new CalcController().times(input);
        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody()).isEqualTo(expected);
    }

    @Test
    void whenTimesNegativesAndNullThenMinusNull() {
        var input = new TwoArgs(-3, 0);
        var expected = new Result(-0.0);
        var output = new CalcController().times(input);
        assertThat(output.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        assertThat(output.getBody()).isEqualTo(expected);
    }
}
