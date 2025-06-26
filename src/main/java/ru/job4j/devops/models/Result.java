package ru.job4j.devops.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

import lombok.*;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "results")
@Table(name = "results")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "first_arg")
    private Double firstArg;

    @Column(name = "second_arg")
    private Double secondArg;

    @Column(name = "result")
    private Double result;

    @Column(name = "create_date")
    private LocalDate createDate;

    private String operation;

    public Result(double result) {

        this.result = result;
    }
}

