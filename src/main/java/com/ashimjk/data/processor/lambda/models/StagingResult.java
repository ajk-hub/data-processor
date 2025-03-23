package com.ashimjk.data.processor.lambda.models;

public record StagingResult(String transactionId, int limit, boolean success) {
}
