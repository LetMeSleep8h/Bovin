package com.eighthours.bovin.rag.dto;

public record MarkdownDocumentResponse(
        Long documentId,
        int chunkCount
) {
}
