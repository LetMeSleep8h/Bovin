package com.eighthours.bovin.rag.dto;

public record RagSearchHit(
        Long chunkId,
        String chunkText,
        String sourceName,
        String source,
        double score
) {
}
