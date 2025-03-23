package com.ashimjk.data.processor.lambda.models;

public record StagingItem(String transactionId, int offset, int limit) {
}
