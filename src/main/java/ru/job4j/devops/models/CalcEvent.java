package ru.job4j.devops.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "calc_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CalcEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    User user;

    @Column(name = "first")
    Integer first;

    @Column(name = "second")
    Integer second;

    @Column(name = "result")
    Integer result;

    @Column(name = "create_date")
    LocalDateTime createDate;

    @Enumerated(EnumType.STRING)
    TypeEnum type;

    @Override
    public boolean equals(Object o) {
        if (this == o){ return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        CalcEvent calcEvent = (CalcEvent) o;
        return Objects.equals(first, calcEvent.first) && Objects.equals(second, calcEvent.second) && result == calcEvent.result && Objects.equals(id, calcEvent.id) && Objects.equals(user, calcEvent.user) && Objects.equals(createDate, calcEvent.createDate) && Objects.equals(type, calcEvent.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, first, second, result, createDate, type);
    }
}