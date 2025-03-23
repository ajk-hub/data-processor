package com.ashimjk.data.processor.lambda.models;

public record PublishResult(String transactionId, int offset, boolean success) {
}
