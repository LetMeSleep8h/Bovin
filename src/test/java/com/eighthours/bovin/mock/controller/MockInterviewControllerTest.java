package com.eighthours.bovin.mock.controller;

import com.eighthours.bovin.mock.MockAnswerResponse;
import com.eighthours.bovin.mock.MockHistoryItem;
import com.eighthours.bovin.mock.MockInterviewService;
import com.eighthours.bovin.mock.MockQuestionResponse;
import com.eighthours.bovin.mock.dto.MockAnswerRequest;
import com.eighthours.bovin.mock.dto.MockFeedback;
import com.eighthours.bovin.mock.dto.MockQuestionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MockInterviewController.class)
class MockInterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MockInterviewService mockInterviewService;

    @Test
    void pickQuestionReturnsBestCandidate() throws Exception {
        when(mockInterviewService.pickQuestion(any())).thenReturn(new MockQuestionResponse(
                5L,
                7L,
                "jianxin-rongtong-backend-round1.md",
                "Q3 订单超时自动取消\n\n问题：延迟消息是什么时候发送的？"
        ));

        MockQuestionRequest request = new MockQuestionRequest();
        request.setQuery("订单超时自动取消 RabbitMQ TTL");
        request.setTargetCorpus("job");
        request.setCompany("建信融通");
        request.setRole("后端开发");
        request.setTopic("RabbitMQ");

        mockMvc.perform(post("/mock/question")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chunkId").value(5))
                .andExpect(jsonPath("$.documentId").value(7))
                .andExpect(jsonPath("$.sourceName").value("jianxin-rongtong-backend-round1.md"))
                .andExpect(jsonPath("$.questionText").value("Q3 订单超时自动取消\n\n问题：延迟消息是什么时候发送的？"));
    }

    @Test
    void submitAnswerReturnsStructuredFeedback() throws Exception {
        when(mockInterviewService.submitAnswer(any())).thenReturn(new MockAnswerResponse(
                21L,
                new MockFeedback(
                        "回答覆盖了事务核心原理",
                        List.of("提到了 AOP 代理"),
                        List.of("缺少自调用失效场景"),
                        List.of("补充代理对象调用条件"),
                        "Spring 事务依赖代理增强，自调用不会经过代理，因此事务不会生效。"
                )
        ));

        mockMvc.perform(post("/mock/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new MockAnswerRequest(
                                16L,
                                7L,
                                "jianxin-rongtong-backend-round1.md",
                                "Q16 事务自调用陷阱\n\n问题：为什么会失效？",
                                "因为事务是基于 AOP 代理实现的，自调用不会走代理。"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordId").value(21))
                .andExpect(jsonPath("$.feedback.summary").value("回答覆盖了事务核心原理"))
                .andExpect(jsonPath("$.feedback.strengths[0]").value("提到了 AOP 代理"))
                .andExpect(jsonPath("$.feedback.suggestedAnswer").value("Spring 事务依赖代理增强，自调用不会经过代理，因此事务不会生效。"));
    }

    @Test
    void historyReturnsRecentRecords() throws Exception {
        when(mockInterviewService.history(5)).thenReturn(List.of(
                new MockHistoryItem(
                        21L,
                        16L,
                        7L,
                        "jianxin-rongtong-backend-round1.md",
                        "Q16 事务自调用陷阱\n\n问题：为什么会失效？",
                        "因为事务是基于 AOP 代理实现的。",
                        new MockFeedback(
                                "回答基本正确",
                                List.of("提到了代理"),
                                List.of("缺少自调用"),
                                List.of("补充失效原因"),
                                "通过代理对象调用事务方法才会生效。"
                        ),
                        LocalDateTime.of(2026, 6, 10, 21, 0, 0)
                )
        ));

        mockMvc.perform(get("/mock/history").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recordId").value(21))
                .andExpect(jsonPath("$[0].chunkId").value(16))
                .andExpect(jsonPath("$[0].feedback.summary").value("回答基本正确"))
                .andExpect(jsonPath("$[0].sourceName").value("jianxin-rongtong-backend-round1.md"));
    }
}
