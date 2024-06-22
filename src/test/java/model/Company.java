package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Company {
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    private String address; // New field for company address

    // Additional fields that might be useful for working with a company
    private int id; // Company ID
    private boolean active; // Company activity status
    private java.sql.Timestamp deletedAt; // Deletion timestamp for the company

    // Empty constructor for Lombok
    public Company() {
    }

    // Constructor with main fields
    public Company(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and setters for additional fields
    // Lombok can be used to automatically generate them

    // Getter and setter for company ID
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and setter for company activity status
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Getter and setter for deletion timestamp
    public java.sql.Timestamp getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(java.sql.Timestamp deletedAt) {
        this.deletedAt = deletedAt;
    }

    // Getter and setter for company address
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // Method to obtain a JSON string representing the Company object
    public String getJsonString() {
        return "{\"name\": \"" + this.name + "\", \"description\": \"" + this.description + "\", \"address\": \"" + this.address + "\"}";
    }
}
