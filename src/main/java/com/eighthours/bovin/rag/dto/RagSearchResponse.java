package com.eighthours.bovin.rag.dto;

import java.util.List;

public record RagSearchResponse(
        String normalizedQuery,
        List<RagSearchHit> hits
) {
}
