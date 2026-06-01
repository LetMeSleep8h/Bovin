package com.eighthours.bovin.rag.mapper;

import lombok.Data;

@Data
public class RagChunkRow {

    private Long chunkId;
    private Long documentId;
    private String title;
    private String sourceName;
    private String corpusType;
    private String documentType;
    private String chunkText;
    private Double score;
}
