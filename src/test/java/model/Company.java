package model;

import lombok.Data;

@Data
public class Company {
    // Поля объекта: название компании и описание
    private String name = "Sherlock Holmes";
    private String description = "The mystery story of Bohemia";

    // Конструкторы
    public Company() {
    }

    public Company(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Метод для получения JSON-строки, представляющей объект Company
    public String getJsonString() {
        return "{\"name\": \"" + this.name + "\", \"description\": \"" + this.description + "\"}";
    }
}