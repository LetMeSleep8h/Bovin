package com.eighthours.bovin.mock;

import com.eighthours.bovin.mock.dto.MockFeedback;

import java.time.LocalDateTime;

public record MockHistoryItem(
        Long recordId,
        Long chunkId,
        Long documentId,
        String sourceName,
        String questionText,
        String answerText,
        MockFeedback feedback,
        LocalDateTime createdAt
) {
}
