package com.ashimjk.data.processor.lambda.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "CUSTOMER_STAGING")
public class Staging {

    @Id
    private Long customerId;
    private String surname;
    private int cScore;
    private int age;
    private int tenure;
    private String status;

}
