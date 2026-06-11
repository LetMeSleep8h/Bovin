package com.eighthours.bovin.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownChunkerTest {

    @Test
    void splitsMarkdownByHeadingAndParagraphs() {
        MarkdownChunker chunker = new MarkdownChunker(240, 40);
        String markdown = """
                # Bovin

                Bovin is an interview agent for Java engineers.

                ## Project Experience

                Built a Spring Boot service with Redis caching.

                Improved retrieval accuracy with hybrid search.

                ## Education

                Computer science background.
                """;

        List<ChunkFragment> chunks = chunker.chunk(markdown);

        assertEquals(3, chunks.size());
        assertTrue(chunks.get(0).text().contains("Bovin is an interview agent"));
        assertTrue(chunks.get(1).text().contains("Project Experience"));
        assertTrue(chunks.get(1).text().contains("hybrid search"));
        assertTrue(chunks.get(2).text().contains("Education"));
    }

    @Test
    void splitsInterviewQuestionsIntoDedicatedChunks() {
        MarkdownChunker chunker = new MarkdownChunker(800, 120);
        String markdown = """
                # 建信融通 后端开发 一面面经

                ## 面经元信息

                - 公司：建信融通
                - 岗位：后端开发

                ## Q1 自我介绍与项目概述

                - 主题：自我介绍 / 项目经历
                问题：
                自我介绍，介绍自己做过的项目，以及项目中主要做了哪些模块？

                ## Q2 订单模块完整流程

                - 主题：项目深挖 / 订单业务
                问题：
                具体讲讲你刚刚提到的订单模块，完整业务流程是怎么样的？

                ## 反问 1 面试轮次

                问题：
                实习生应聘有几轮面试？
                """;

        List<ChunkFragment> chunks = chunker.chunk(markdown);

        assertEquals(4, chunks.size());
        assertTrue(chunks.get(0).text().contains("面经元信息"));
        assertTrue(chunks.get(1).text().contains("Q1 自我介绍与项目概述"));
        assertTrue(chunks.get(2).text().contains("Q2 订单模块完整流程"));
        assertTrue(chunks.get(3).text().contains("反问 1 面试轮次"));
    }
}
