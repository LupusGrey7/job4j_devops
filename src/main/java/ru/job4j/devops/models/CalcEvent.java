package ru.job4j.devops.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Кастомизация билдера - Обновленный билдер для защиты от сохранения ссылки на мутабельный объект()
 * Почему это происходит?
 * Класс User является мутабельным (имеет сеттеры, аннотированные @Setter).
 * Поле CalcEvent.user хранит прямую ссылку на объект User, переданный через конструктор или билдер.
 * SpotBugs считает это небезопасным, так как внешний код может изменить объект User после его передачи в CalcEvent, что нарушит целостность данных.
 */
@Entity
@Table(name = "calc_events")
@Getter
@Setter
@NoArgsConstructor
@Builder
@ToString
public class CalcEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "first")
    private Integer first;

    @Column(name = "second")
    private Integer second;

    @Column(name = "result")
    private Integer result;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Enumerated(EnumType.STRING)
    private TypeEnum type;

    /**
     * Ручной конструктор
     * this.user = user != null ? copyUser(user) : null; - Создаём защитную копию
     * @param id
     * @param user
     * @param first
     * @param second
     * @param result
     * @param createDate
     * @param type
     */
    public CalcEvent(
            Long id,
            User user,
            Integer first,
            Integer second,
            Integer result,
            LocalDateTime createDate,
            TypeEnum type
    ) {
        this.id = id;
        this.user = user != null ? copyUser(user) : null;
        this.first = first;
        this.second = second;
        this.result = result;
        this.createDate = createDate;
        this.type = type;
    }

    /**
     * Защищенный геттер от мутационных операций
     * @return User
     */
    public User getUser() {
        return user != null ? copyUser(user) : null;
    }

    /**
     * Защищенный сеттер от мутационных операций
     * @param user
     */
    public void setUser(User user) {
        this.user = user != null ? copyUser(user) : null;
    }

    /**
     * Метод для создания защитной копии
     * @param original
     * @return User
     */
    private User copyUser(User original) {
        return User.builder()
                .id(original.getId())
                .name(original.getName())
                .build();
    }

    public static class CalcEventBuilder {
        private User user;

        public CalcEventBuilder user(User user) {
            this.user = user != null ? copyUser(user) : null;
            return this;
        }

        private User copyUser(User original) {
            return User.builder()
                    .id(original.getId())
                    .name(original.getName())
                    .build();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CalcEvent calcEvent = (CalcEvent) o;
        return Objects.equals(first, calcEvent.first)
                && Objects.equals(second, calcEvent.second)
                && Objects.equals(result, calcEvent.result)
                && Objects.equals(id, calcEvent.id)
                && Objects.equals(user, calcEvent.user)
                && Objects.equals(createDate, calcEvent.createDate)
                && Objects.equals(type, calcEvent.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, first, second, result, createDate, type);
    }
}