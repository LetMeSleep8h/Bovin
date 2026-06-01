package com.eighthours.bovin.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HybridSearchMergerTest {

    @Test
    void mergesKeywordAndVectorScoresForSameChunk() {
        HybridSearchMerger merger = new HybridSearchMerger(0.4, 0.6);

        List<SearchCandidate> merged = merger.merge(
                List.of(
                        new SearchCandidate(1L, "spring boot", "resume.md", 0.9, SearchSource.KEYWORD),
                        new SearchCandidate(2L, "redis", "resume.md", 0.5, SearchSource.KEYWORD)
                ),
                List.of(
                        new SearchCandidate(1L, "spring boot", "resume.md", 0.7, SearchSource.VECTOR),
                        new SearchCandidate(3L, "rag", "resume.md", 0.8, SearchSource.VECTOR)
                ),
                3
        );

        assertEquals(3, merged.size());
        assertEquals(1L, merged.get(0).chunkId());
        assertEquals(0.78, merged.get(0).score(), 0.0001);
        assertEquals(SearchSource.HYBRID, merged.get(0).source());
    }
}
