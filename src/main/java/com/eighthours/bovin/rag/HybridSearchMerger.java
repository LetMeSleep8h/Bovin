package com.eighthours.bovin.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class HybridSearchMerger {

    private final double keywordWeight;
    private final double vectorWeight;

    @Autowired
    public HybridSearchMerger(RagProperties properties) {
        this(properties.keywordWeight(), properties.vectorWeight());
    }

    HybridSearchMerger(double keywordWeight, double vectorWeight) {
        this.keywordWeight = keywordWeight;
        this.vectorWeight = vectorWeight;
    }

    public List<SearchCandidate> merge(List<SearchCandidate> keywordHits, List<SearchCandidate> vectorHits, int topK) {
        Map<Long, SearchAccumulator> merged = new LinkedHashMap<>();

        keywordHits.forEach(hit -> merged.computeIfAbsent(hit.chunkId(), ignored -> new SearchAccumulator(hit))
                .keywordScore = hit.score());
        vectorHits.forEach(hit -> merged.computeIfAbsent(hit.chunkId(), ignored -> new SearchAccumulator(hit))
                .vectorScore = hit.score());

        List<SearchCandidate> ranked = new ArrayList<>();
        merged.values().forEach(value -> ranked.add(value.toCandidate(keywordWeight, vectorWeight)));
        ranked.sort(Comparator.comparingDouble(SearchCandidate::score).reversed()
                .thenComparing(SearchCandidate::chunkId));
        return ranked.stream().limit(topK).toList();
    }

    private static final class SearchAccumulator {
        private final SearchCandidate candidate;
        private double keywordScore;
        private double vectorScore;

        private SearchAccumulator(SearchCandidate candidate) {
            this.candidate = candidate;
        }

        private SearchCandidate toCandidate(double keywordWeight, double vectorWeight) {
            double mergedScore = (keywordScore * keywordWeight) + (vectorScore * vectorWeight);
            SearchSource source = keywordScore > 0 && vectorScore > 0 ? SearchSource.HYBRID : candidate.source();
            return candidate.withScore(mergedScore, source);
        }
    }
}
