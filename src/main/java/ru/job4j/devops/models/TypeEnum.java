package ru.job4j.devops.models;

public enum TypeEnum {
    OPEN, REVIEW, APPROVED, REJECTED;

    public static TypeEnum getTypeEnum(String type) {
        return TypeEnum.valueOf(type.toUpperCase());
    }
}
