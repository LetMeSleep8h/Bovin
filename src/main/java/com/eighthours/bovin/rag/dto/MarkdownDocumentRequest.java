package com.eighthours.bovin.rag.dto;

public record MarkdownDocumentRequest(
        String title,
        String sourceName,
        String corpusType,
        String documentType,
        String content
) {
}
