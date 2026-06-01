package com.eighthours.bovin.rag;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bovin.rag")
public record RagProperties(
        int chunkSize,
        int chunkOverlap,
        int keywordTopK,
        int vectorTopK,
        double keywordWeight,
        double vectorWeight
) {
}
