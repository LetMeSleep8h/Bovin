package com.eighthours.bovin.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class MarkdownChunker {

    private static final Pattern QUESTION_HEADING_PATTERN = Pattern.compile("^(Q\\d+|反问\\s*\\d+).*$");

    private final int chunkSize;
    private final int chunkOverlap;

    @Autowired
    public MarkdownChunker(RagProperties properties) {
        this(properties.chunkSize(), properties.chunkOverlap());
    }

    MarkdownChunker(int chunkSize, int chunkOverlap) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public List<ChunkFragment> chunk(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return List.of();
        }

        List<ChunkFragment> sections = new ArrayList<>();
        String currentHeading = "";
        StringBuilder sectionBuilder = new StringBuilder();

        for (String line : markdown.split("\\R")) {
            if (line.startsWith("#")) {
                appendSection(sections, currentHeading, sectionBuilder);
                currentHeading = line.replaceFirst("^#+\\s*", "").trim();
                sectionBuilder = new StringBuilder();
                sectionBuilder.append(currentHeading).append("\n\n");
                continue;
            }

            if (sectionBuilder.length() > 0 || !line.isBlank()) {
                sectionBuilder.append(line).append("\n");
            }
        }

        appendSection(sections, currentHeading, sectionBuilder);
        return splitOversizedSections(sections);
    }

    private void appendSection(List<ChunkFragment> sections, String heading, StringBuilder sectionBuilder) {
        String text = sectionBuilder.toString().trim();
        if (!text.isBlank() && !isHeadingOnlySection(heading, text)) {
            sections.add(new ChunkFragment(heading, text));
        }
    }

    private List<ChunkFragment> splitOversizedSections(List<ChunkFragment> sections) {
        List<ChunkFragment> chunks = new ArrayList<>();
        for (ChunkFragment section : sections) {
            if (section.text().length() <= chunkSize) {
                chunks.add(section);
                continue;
            }

            int start = 0;
            while (start < section.text().length()) {
                int end = Math.min(start + chunkSize, section.text().length());
                chunks.add(new ChunkFragment(section.heading(), section.text().substring(start, end).trim()));
                if (end == section.text().length()) {
                    break;
                }
                start = Math.max(end - chunkOverlap, start + 1);
            }
        }
        return chunks;
    }

    private boolean isHeadingOnlySection(String heading, String text) {
        if (heading == null || heading.isBlank()) {
            return false;
        }
        if (QUESTION_HEADING_PATTERN.matcher(heading).matches()) {
            return false;
        }
        return heading.equals(text.trim());
    }
}
