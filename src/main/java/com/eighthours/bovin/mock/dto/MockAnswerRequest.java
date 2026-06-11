package com.eighthours.bovin.mock.dto;

public record MockAnswerRequest(
        Long chunkId,
        Long documentId,
        String sourceName,
        String questionText,
        String answer
) {
}
