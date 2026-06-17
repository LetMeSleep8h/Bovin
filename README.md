# Bovin

> 基于 `Spring Boot + PostgreSQL + pgvector + Spring AI` 的面试知识库与模拟面试系统，支持面经导入、题目级切块、向量化检索、混合召回，以及 AI 复盘闭环。

## 项目定位

Bovin 主要解决两个问题：

1. 将零散的面经、题库、项目问答等非结构化资料沉淀为可检索的知识库。
2. 在知识库基础上实现“抽题 -> 作答 -> 复盘 -> 历史回看”的最小模拟面试闭环。

这个项目的重点不在于单纯调用模型，而在于把文档结构化、检索、存储、反馈整合成一个完整的后端工程链路。

## 项目能做什么

- 支持将 Markdown 面经导入知识库
- 支持题目级切块，避免标题、元信息和题目正文混在一起
- 支持文本向量化并写入 `PostgreSQL + pgvector`
- 支持关键词检索与向量检索的混合召回
- 支持模拟面试抽题、提交回答、AI 复盘、历史记录查看

## 核心技术栈

- `Spring Boot 4.0.6`
- `Spring AI 2.0.0-M8`
- `PostgreSQL`
- `pgvector`
- `MyBatis-Plus`
- `OpenAI Compatible API`
- `JUnit + MockMvc`

## 系统架构图

```mermaid
%%{init: {'theme': 'base', 'themeVariables': { 'primaryColor': '#ffffff', 'primaryTextColor': '#000000', 'primaryBorderColor': '#000000', 'lineColor': '#000000', 'secondaryColor': '#ffffff', 'tertiaryColor': '#ffffff', 'clusterBkg': '#ffffff', 'clusterBorder': '#000000', 'fontFamily': 'monospace' }}}%%
flowchart LR
    A["调用方 / 前端 / curl"] --> B["Spring Boot API"]

    subgraph APP["Bovin Application"]
        B --> C["RAG Controller / Service"]
        B --> D["Mock Interview Controller / Service"]
        C --> E["MarkdownChunker"]
        C --> F["EmbeddingService"]
        C --> G["HybridSearchMerger"]
        D --> H["MockFeedbackService"]
    end

    E --> I["RagDocumentMapper / RagChunkMapper"]
    G --> I
    D --> J["MockRecordMapper"]

    I --> K["PostgreSQL"]
    J --> K

    K --> L["rag_document"]
    K --> M["rag_chunk (vector, fts)"]
    K --> N["mock_record"]

    F --> O["Embedding Model"]
    H --> P["Chat Model"]
```

## 核心数据流

### 1. 导入链路

```mermaid
%%{init: {'theme': 'base', 'themeVariables': { 'primaryColor': '#ffffff', 'primaryTextColor': '#000000', 'primaryBorderColor': '#000000', 'lineColor': '#000000', 'secondaryColor': '#ffffff', 'tertiaryColor': '#ffffff', 'clusterBkg': '#ffffff', 'clusterBorder': '#000000', 'fontFamily': 'monospace' }}}%%
flowchart TD
    A["POST /api/rag/documents"] --> B["RagController"]
    B --> C["RagService.ingestMarkdown()"]
    C --> D["插入 rag_document"]
    C --> E["MarkdownChunker 题目级切块"]
    E --> F["遍历每个 chunk"]
    F --> G["EmbeddingModel 生成向量"]
    G --> H["插入 rag_chunk"]
    D --> H
    H --> I["返回 documentId / chunkCount"]
```

### 2. 检索链路

```mermaid
%%{init: {'theme': 'base', 'themeVariables': { 'primaryColor': '#ffffff', 'primaryTextColor': '#000000', 'primaryBorderColor': '#000000', 'lineColor': '#000000', 'secondaryColor': '#ffffff', 'tertiaryColor': '#ffffff', 'clusterBkg': '#ffffff', 'clusterBorder': '#000000', 'fontFamily': 'monospace' }}}%%
flowchart TD
    A["POST /api/rag/search"] --> B["RagController"]
    B --> C["RagService.search()"]
    C --> D["query -> embedding"]
    C --> E["keywordSearch"]
    C --> F["vectorSearch"]
    D --> F
    E --> G["HybridSearchMerger"]
    F --> G
    G --> H["返回命中 chunk 列表"]
```

### 3. 模拟面试链路

```mermaid
%%{init: {'theme': 'base', 'themeVariables': { 'primaryColor': '#ffffff', 'primaryTextColor': '#000000', 'primaryBorderColor': '#000000', 'lineColor': '#000000', 'secondaryColor': '#ffffff', 'tertiaryColor': '#ffffff', 'clusterBkg': '#ffffff', 'clusterBorder': '#000000', 'fontFamily': 'monospace' }}}%%
flowchart TD
    A["POST /api/mock/question"] --> B["MockInterviewService.pickQuestion()"]
    B --> C["复用 RAG 检索抽题"]
    C --> D["返回最相关题目块"]

    E["POST /api/mock/answer"] --> F["MockInterviewService.submitAnswer()"]
    F --> G["ChatModel 生成结构化复盘"]
    G --> H["写入 mock_record"]
    H --> I["返回 feedback"]

    J["GET /api/mock/history"] --> K["查询历史记录"]
```

## 核心存储设计

### `rag_document`

用于保存原始文档主记录。

- `title`
- `source_name`
- `corpus_type`
- `document_type`
- `raw_markdown`

### `rag_chunk`

用于保存切块后的检索单元，是 RAG 的核心表。

- `document_id`
- `chunk_index`
- `heading`
- `chunk_text`
- `fts_text`
- `embedding VECTOR(1536)`

### `mock_record`

用于保存模拟面试答题记录与 AI 复盘结果。

- `chunk_id`
- `document_id`
- `source_name`
- `question_text`
- `answer_text`
- `feedback_json`

## 关键接口

### RAG 接口

- `POST /api/rag/documents`
  - 导入 Markdown 文档并完成切块、向量化、入库
- `POST /api/rag/search`
  - 基于关键词检索与向量检索返回命中片段
