package com.eighthours.bovin.rag.controller;

import com.eighthours.bovin.rag.dto.MarkdownDocumentRequest;
import com.eighthours.bovin.rag.dto.MarkdownDocumentResponse;
import com.eighthours.bovin.rag.dto.RagSearchRequest;
import com.eighthours.bovin.rag.dto.RagSearchResponse;
import com.eighthours.bovin.rag.service.RagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/documents")
    public MarkdownDocumentResponse ingestMarkdown(@RequestBody MarkdownDocumentRequest request) {
        return ragService.ingestMarkdown(request);
    }

    @PostMapping("/search")
    public RagSearchResponse search(@RequestBody RagSearchRequest request) {
        return ragService.search(request);
    }
}
