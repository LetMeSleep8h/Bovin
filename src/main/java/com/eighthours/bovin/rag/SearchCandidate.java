package com.eighthours.bovin.rag;

public record SearchCandidate(
        Long chunkId,
        String chunkText,
        String sourceName,
        double score,
        SearchSource source
) {
    public SearchCandidate withScore(double mergedScore, SearchSource mergedSource) {
        return new SearchCandidate(chunkId, chunkText, sourceName, mergedScore, mergedSource);
    }
}
