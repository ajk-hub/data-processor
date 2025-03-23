package com.ashimjk.data.processor.lambda.entities;

public enum Status {

    NEW("NW"),
    PENDING("PN"),
    COMPLETED("CP"),
    NOT_APPLICABLE("NA");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
