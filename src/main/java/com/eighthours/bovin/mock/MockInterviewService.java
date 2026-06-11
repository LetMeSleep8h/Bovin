package com.eighthours.bovin.mock;

import com.eighthours.bovin.mock.dto.MockAnswerRequest;
import com.eighthours.bovin.mock.dto.MockFeedback;
import com.eighthours.bovin.mock.dto.MockQuestionRequest;
import com.eighthours.bovin.mock.mapper.MockRecordMapper;
import com.eighthours.bovin.rag.dto.RagSearchHit;
import com.eighthours.bovin.rag.dto.RagSearchRequest;
import com.eighthours.bovin.rag.dto.RagSearchResponse;
import com.eighthours.bovin.rag.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class MockInterviewService {

    private final RagService ragService;
    private final MockRecordMapper mockRecordMapper;
    private final MockFeedbackService feedbackService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MockInterviewService(RagService ragService,
                                MockRecordMapper mockRecordMapper,
                                MockFeedbackService feedbackService,
                                ObjectMapper objectMapper) {
        this.ragService = ragService;
        this.mockRecordMapper = mockRecordMapper;
        this.feedbackService = feedbackService;
        this.objectMapper = objectMapper;
    }

    MockInterviewService(RagService ragService,
                         MockRecordMapper mockRecordMapper,
                         MockFeedbackService feedbackService) {
        this(ragService, mockRecordMapper, feedbackService, new ObjectMapper());
    }

    public MockQuestionResponse pickQuestion(MockQuestionRequest request) {
        RagSearchResponse response = ragService.search(new RagSearchRequest(
                buildSearchQuery(request),
                request.getTargetCorpus(),
                20
        ));
        List<Long> excludedIds = request.getExcludeChunkIds() == null ? List.of() : request.getExcludeChunkIds();
        RagSearchHit hit = response.hits().stream()
                .filter(candidate -> !excludedIds.contains(candidate.chunkId()))
                .sorted(Comparator.comparingInt(this::questionPriority))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No interview question found"));

        return new MockQuestionResponse(hit.chunkId(), hit.documentId(), hit.sourceName(), hit.chunkText());
    }

    public MockAnswerResponse submitAnswer(MockAnswerRequest request) {
        MockFeedback feedback = feedbackService.review(request.questionText(), request.answer());
        MockRecord record = new MockRecord();
        record.setChunkId(request.chunkId());
        record.setDocumentId(request.documentId());
        record.setSourceName(request.sourceName());
        record.setQuestionText(request.questionText());
        record.setAnswerText(request.answer());
        try {
            record.setFeedbackJson(objectMapper.writeValueAsString(feedback));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize mock feedback", exception);
        }
        mockRecordMapper.insert(record);
        return new MockAnswerResponse(record.getId(), feedback);
    }

    public List<MockHistoryItem> history(int limit) {
        return mockRecordMapper.selectRecent(limit).stream()
                .map(this::toHistoryItem)
                .toList();
    }

    private String buildSearchQuery(MockQuestionRequest request) {
        return Stream.of(request.getCompany(), request.getRole(), request.getTopic(), request.getQuery())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .reduce((left, right) -> left + " " + right)
                .orElseThrow(() -> new IllegalArgumentException("Query is required"));
    }

    private int questionPriority(RagSearchHit hit) {
        if (isQuestionChunk(hit.chunkText())) {
            return 0;
        }
        if (hit.chunkText() != null && hit.chunkText().contains("面经元信息")) {
            return 2;
        }
        return 1;
    }

    private boolean isQuestionChunk(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        return trimmed.startsWith("Q") || trimmed.startsWith("反问") || trimmed.contains("\n问题：");
    }

    private MockHistoryItem toHistoryItem(MockRecord record) {
        try {
            MockFeedback feedback = objectMapper.readValue(record.getFeedbackJson(), MockFeedback.class);
            return new MockHistoryItem(
                    record.getId(),
                    record.getChunkId(),
                    record.getDocumentId(),
                    record.getSourceName(),
                    record.getQuestionText(),
                    record.getAnswerText(),
                    feedback,
                    record.getCreatedAt()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse history feedback", exception);
        }
    }
}
