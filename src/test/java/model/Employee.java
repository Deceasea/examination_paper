package model;

import lombok.Data;

@Data
public class Employee {
    private int id;
    private String firstName;
    private String middleName;
    private String lastName;
    private int companyId;
    private String email;
    private String phone;
    private String birthdate;
    private boolean isActive;

    public Employee() {
        this.id = 0;
        this.firstName = "John";
        this.middleName = "Doe";
        this.lastName = "Smith";
        this.companyId = 0;
        this.email = "john.doe@gmail.com";
        this.phone = "123-456-7890";
        this.birthdate = "1992-03-05";
        this.isActive = true;
    }

    public Employee(String firstName, String middleName, String lastName, int companyId, String email, String phone, String birthdate, boolean isActive) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.companyId = companyId;
        this.email = email;
        this.phone = phone;
        this.birthdate = birthdate;
        this.isActive = isActive;
    }

    public String getJsonString(int companyId) {
        return "{\"firstName\": \"" + this.firstName + "\"," +
                " \"middleName\": \"" + this.middleName + "\"," +
                " \"lastName\": \"" + this.lastName + "\"," +
                " \"companyId\":" + companyId + "," +
                " \"email\": \"" + this.email + "\"," +
                " \"phone\": \"" + this.phone + "\"," +
                " \"birthdate\": \"" + this.birthdate + "\"," +
                " \"isActive\": \"" + this.isActive + "\"}";
    }
}