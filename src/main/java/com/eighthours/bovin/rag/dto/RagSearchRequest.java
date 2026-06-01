package com.eighthours.bovin.rag.dto;

public record RagSearchRequest(
        String query,
        String targetCorpus,
        Integer topK
) {
}
