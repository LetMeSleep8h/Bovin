package com.eighthours.bovin.mock;

import com.eighthours.bovin.mock.dto.MockAnswerRequest;
import com.eighthours.bovin.mock.dto.MockFeedback;
import com.eighthours.bovin.mock.dto.MockQuestionRequest;
import com.eighthours.bovin.mock.mapper.MockRecordMapper;
import com.eighthours.bovin.rag.dto.RagSearchHit;
import com.eighthours.bovin.rag.dto.RagSearchRequest;
import com.eighthours.bovin.rag.dto.RagSearchResponse;
import com.eighthours.bovin.rag.service.RagService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MockInterviewServiceTest {

    @Test
    void picksQuestionChunkOverMetadataAndSavesFeedback() {
        RagService ragService = mock(RagService.class);
        MockRecordMapper mockRecordMapper = mock(MockRecordMapper.class);
        MockFeedbackService feedbackService = mock(MockFeedbackService.class);
        MockInterviewService service = new MockInterviewService(ragService, mockRecordMapper, feedbackService);

        when(ragService.search(any(RagSearchRequest.class))).thenReturn(new RagSearchResponse(
                "事务自调用",
                List.of(
                        new RagSearchHit(2L, 7L, "建信融通 后端开发 一面面经", "meta.md", "VECTOR", 0.9),
                        new RagSearchHit(9L, 7L, "Q16 事务自调用陷阱\n\n问题：为什么会失效？", "interview.md", "HYBRID", 0.7)
                )
        ));
        when(feedbackService.review(any(), any())).thenReturn(new MockFeedback(
                "回答覆盖了核心概念",
                List.of("提到了代理机制"),
                List.of("缺少自调用场景说明"),
                List.of("补充 why self invocation fails"),
                "通过代理对象调用事务方法才会生效。"
        ));

        MockQuestionResponse question = service.pickQuestion(new MockQuestionRequest("事务自调用", "job", List.of()));
        MockAnswerResponse answer = service.submitAnswer(new MockAnswerRequest(
                question.chunkId(),
                question.documentId(),
                question.sourceName(),
                question.questionText(),
                "因为事务是基于AOP代理实现的"
        ));

        assertEquals(9L, question.chunkId());
        assertEquals("interview.md", question.sourceName());
        assertNotNull(answer.feedback());
        assertEquals("回答覆盖了核心概念", answer.feedback().summary());

        ArgumentCaptor<MockRecord> captor = ArgumentCaptor.forClass(MockRecord.class);
        verify(mockRecordMapper).insert(captor.capture());
        assertEquals(9L, captor.getValue().getChunkId());
        assertEquals("因为事务是基于AOP代理实现的", captor.getValue().getAnswerText());
    }

    @Test
    void returnsHistoryWithParsedFeedback() {
        RagService ragService = mock(RagService.class);
        MockRecordMapper mockRecordMapper = mock(MockRecordMapper.class);
        MockFeedbackService feedbackService = mock(MockFeedbackService.class);
        MockInterviewService service = new MockInterviewService(ragService, mockRecordMapper, feedbackService);

        MockRecord record = new MockRecord();
        record.setId(11L);
        record.setChunkId(9L);
        record.setDocumentId(7L);
        record.setSourceName("interview.md");
        record.setQuestionText("Q16 事务自调用陷阱");
        record.setAnswerText("事务是基于代理的");
        record.setFeedbackJson("""
                {"summary":"回答基本正确","strengths":["提到了代理"],"missingPoints":["缺少自调用"],"improvements":["补充失效原因"],"suggestedAnswer":"通过代理对象调用才生效"}
                """);
        doReturn(List.of(record)).when(mockRecordMapper).selectRecent(10);

        List<MockHistoryItem> history = service.history(10);

        assertEquals(1, history.size());
        assertEquals("回答基本正确", history.get(0).feedback().summary());
        assertEquals("Q16 事务自调用陷阱", history.get(0).questionText());
    }
}
