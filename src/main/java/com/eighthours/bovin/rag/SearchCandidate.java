package com.eighthours.bovin.rag;

public record SearchCandidate(
        Long chunkId,
        Long documentId,
        String chunkText,
        String sourceName,
        double score,
        SearchSource source
) {
    public SearchCandidate withScore(double mergedScore, SearchSource mergedSource) {
        return new SearchCandidate(chunkId, documentId, chunkText, sourceName, mergedScore, mergedSource);
    }
}
