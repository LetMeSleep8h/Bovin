package com.eighthours.bovin.rag.service;

import com.eighthours.bovin.rag.ChunkFragment;
import com.eighthours.bovin.rag.HybridSearchMerger;
import com.eighthours.bovin.rag.MarkdownChunker;
import com.eighthours.bovin.rag.RagProperties;
import com.eighthours.bovin.rag.SearchCandidate;
import com.eighthours.bovin.rag.SearchSource;
import com.eighthours.bovin.rag.dto.MarkdownDocumentRequest;
import com.eighthours.bovin.rag.dto.MarkdownDocumentResponse;
import com.eighthours.bovin.rag.dto.RagSearchHit;
import com.eighthours.bovin.rag.dto.RagSearchRequest;
import com.eighthours.bovin.rag.dto.RagSearchResponse;
import com.eighthours.bovin.rag.entity.RagChunk;
import com.eighthours.bovin.rag.entity.RagDocument;
import com.eighthours.bovin.rag.mapper.RagChunkMapper;
import com.eighthours.bovin.rag.mapper.RagChunkRow;
import com.eighthours.bovin.rag.mapper.RagDocumentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RagService {

    private final RagDocumentMapper ragDocumentMapper;
    private final RagChunkMapper ragChunkMapper;
    private final MarkdownChunker markdownChunker;
    private final EmbeddingService embeddingService;
    private final HybridSearchMerger hybridSearchMerger;
    private final RagProperties ragProperties;

    public RagService(RagDocumentMapper ragDocumentMapper,
                      RagChunkMapper ragChunkMapper,
                      MarkdownChunker markdownChunker,
                      EmbeddingService embeddingService,
                      HybridSearchMerger hybridSearchMerger,
                      RagProperties ragProperties) {
        this.ragDocumentMapper = ragDocumentMapper;
        this.ragChunkMapper = ragChunkMapper;
        this.markdownChunker = markdownChunker;
        this.embeddingService = embeddingService;
        this.hybridSearchMerger = hybridSearchMerger;
        this.ragProperties = ragProperties;
    }

    @Transactional
    public MarkdownDocumentResponse ingestMarkdown(MarkdownDocumentRequest request) {
        RagDocument document = new RagDocument();
        document.setTitle(request.title());
        document.setSourceName(request.sourceName() == null || request.sourceName().isBlank() ? request.title() : request.sourceName());
        document.setCorpusType(request.corpusType());
        document.setDocumentType(request.documentType());
        document.setRawMarkdown(request.content());
        ragDocumentMapper.insert(document);

        List<ChunkFragment> chunks = markdownChunker.chunk(request.content());
        for (int index = 0; index < chunks.size(); index++) {
            ChunkFragment fragment = chunks.get(index);
            RagChunk chunk = new RagChunk();
            chunk.setDocumentId(document.getId());
            chunk.setChunkIndex(index);
            chunk.setHeading(fragment.heading());
            chunk.setChunkText(fragment.text());
            chunk.setFtsText(fragment.heading() == null || fragment.heading().isBlank()
                    ? fragment.text()
                    : fragment.heading() + "\n" + fragment.text());
            chunk.setEmbedding(embeddingService.embedAsVector(chunk.getFtsText()));
            ragChunkMapper.insertChunk(chunk);
        }
        return new MarkdownDocumentResponse(document.getId(), chunks.size());
    }

    public RagSearchResponse search(RagSearchRequest request) {
        int finalTopK = request.topK() == null || request.topK() <= 0
                ? Math.max(ragProperties.keywordTopK(), ragProperties.vectorTopK())
                : request.topK();
        List<SearchCandidate> keywordHits = ragChunkMapper.keywordSearch(request.query(), request.targetCorpus(), ragProperties.keywordTopK())
                .stream()
                .map(this::toKeywordCandidate)
                .toList();
        String queryEmbedding = embeddingService.embedAsVector(request.query());
        List<SearchCandidate> vectorHits = ragChunkMapper.vectorSearch(queryEmbedding, request.targetCorpus(), ragProperties.vectorTopK())
                .stream()
                .map(this::toVectorCandidate)
                .toList();

        List<RagSearchHit> hits = hybridSearchMerger.merge(keywordHits, vectorHits, finalTopK).stream()
                .map(candidate -> new RagSearchHit(
                        candidate.chunkId(),
                        candidate.chunkText(),
                        candidate.sourceName(),
                        candidate.source().name(),
                        candidate.score()))
                .toList();
        return new RagSearchResponse(request.query(), hits);
    }

    private SearchCandidate toKeywordCandidate(RagChunkRow row) {
        return new SearchCandidate(row.getChunkId(), row.getChunkText(), row.getSourceName(), row.getScore(), SearchSource.KEYWORD);
    }

    private SearchCandidate toVectorCandidate(RagChunkRow row) {
        return new SearchCandidate(row.getChunkId(), row.getChunkText(), row.getSourceName(), row.getScore(), SearchSource.VECTOR);
    }
}
