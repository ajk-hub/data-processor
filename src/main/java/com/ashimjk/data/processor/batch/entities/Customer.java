package com.ashimjk.data.processor.batch.entities;

import jakarta.persistence.Id;
import lombok.Data;

@Data
public class Customer {

    @Id
    private Long customerId;
    private String surname;
    private int cScore;
    private String country;
    private String gender;
    private int age;
    private int tenure;
    private double balance;
    private int hasCreditCard;
    private int isActive;
    private double estimatedSalary;

}
