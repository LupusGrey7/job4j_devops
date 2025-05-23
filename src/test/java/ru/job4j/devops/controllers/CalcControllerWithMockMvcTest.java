package ru.job4j.devops.controllers;

import org.junit.jupiter.api.Test;

import org.springframework.test.context.ActiveProfiles;
import ru.job4j.devops.models.TwoArgs;
import ru.job4j.devops.repository.ResultRepository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class CalcControllerWithMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        resultRepository.deleteAll();
    }

    @AfterEach
    void cleanup() {
        resultRepository.deleteAll();
    }

    @Test
    void whenOnePlusOneThenTwo() throws Exception {
        TwoArgs input = new TwoArgs(1, 1);

        mockMvc.perform(post("/calc/summarise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(2.0));

        assertThat(resultRepository.findAll())
                .hasSize(1)
                .allSatisfy(r -> assertThat(r.getResult()).isEqualTo(2.0));
    }

    @Test
    void whenNegativeNumber() throws Exception {
        TwoArgs input = new TwoArgs(-1, -1);

        mockMvc.perform(post("/calc/summarise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(-2.0));
    }

    @Test
    void whenZeroPlusThree() throws Exception {
        TwoArgs input = new TwoArgs(0, 3);

        mockMvc.perform(post("/calc/summarise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(3.0));
    }

    @Test
    void whenTwoTimesTwoThenFour() throws Exception {
        TwoArgs input = new TwoArgs(2, 2);

        mockMvc.perform(post("/calc/summarise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(4.0));
    }

    @Test
    void whenZeroTimesZero() throws Exception {
        TwoArgs input = new TwoArgs(0, 0);

        mockMvc.perform(post("/calc/summarise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(0.0));
    }

    @Test
    void whenTimesNegatives() throws Exception {
        TwoArgs input = new TwoArgs(-3, -3);

        mockMvc.perform(post("/calc/summarise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(-6.0));
    }
}
