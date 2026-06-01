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
}
