package com.eighthours.bovin.mock.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MockQuestionRequest {

    private String query;
    private String targetCorpus;
    private List<Long> excludeChunkIds;
    private String company;
    private String role;
    private String topic;

    public MockQuestionRequest(String query, String targetCorpus, List<Long> excludeChunkIds) {
        this.query = query;
        this.targetCorpus = targetCorpus;
        this.excludeChunkIds = excludeChunkIds;
    }
}
