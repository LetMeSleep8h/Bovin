package com.eighthours.bovin.mock;

public record MockQuestionResponse(
        Long chunkId,
        Long documentId,
        String sourceName,
        String questionText
) {
}
