package com.eighthours.bovin.rag.controller;

import com.eighthours.bovin.rag.dto.MarkdownDocumentResponse;
import com.eighthours.bovin.rag.dto.RagSearchHit;
import com.eighthours.bovin.rag.dto.RagSearchResponse;
import com.eighthours.bovin.rag.service.RagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RagController.class)
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RagService ragService;

    @Test
    void ingestMarkdownReturnsDocumentSummary() throws Exception {
        when(ragService.ingestMarkdown(any())).thenReturn(new MarkdownDocumentResponse(7L, 29));

        mockMvc.perform(post("/rag/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TestMarkdownDocumentRequest(
                                "抖音团队后端面试题库",
                                "douyin-backend-interview.md",
                                "job",
                                "interview",
                                "# Title\n\n## Q1\n问题：Java 的类加载器是什么？"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(7))
                .andExpect(jsonPath("$.chunkCount").value(29));
    }

    @Test
    void searchReturnsHits() throws Exception {
        when(ragService.search(any())).thenReturn(new RagSearchResponse(
                "双亲委派模型解决了什么问题",
                List.of(new RagSearchHit(
                        12L,
                        7L,
                        "Q13 双亲委派解决了什么问题\n\n问题：那双亲委派模型解决了什么问题呢？",
                        "douyin-backend-interview.md",
                        "VECTOR",
                        0.88
                ))
        ));

        mockMvc.perform(post("/rag/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TestRagSearchRequest(
                                "双亲委派模型解决了什么问题",
                                "job",
                                5
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.normalizedQuery").value("双亲委派模型解决了什么问题"))
                .andExpect(jsonPath("$.hits[0].chunkId").value(12))
                .andExpect(jsonPath("$.hits[0].documentId").value(7))
                .andExpect(jsonPath("$.hits[0].sourceName").value("douyin-backend-interview.md"));
    }

    private record TestMarkdownDocumentRequest(
            String title,
            String sourceName,
            String corpusType,
            String documentType,
            String content
    ) {
    }

    private record TestRagSearchRequest(
            String query,
            String targetCorpus,
            Integer topK
    ) {
    }
}
