```yaml
name: bovin-rag-import
description: Use when importing interview notes, question lists, or markdown content into the Bovin PostgreSQL plus pgvector RAG service, especially when a user needs question-level markdown and a ready-to-run curl command for /api/rag/documents.
```

# Bovin RAG Import

Convert raw interview notes into question-level Markdown and produce a shell command that imports the content into Bovin's RAG endpoint.

## When To Use

- The user pasted a Chinese interview transcript, question list, or 面经 and wants it stored in Bovin.
- The user asks how to generate the `curl` command for `POST /api/rag/documents`.
- The user wants content reshaped so Bovin chunks it as one question per chunk.

Do not use this skill for generic RAG theory questions with no need to generate importable content.

## Workflow

1. Inspect Bovin config before emitting commands.
   Check:
- `src/main/resources/application.yaml`
- `src/main/java/.../MarkdownDocumentRequest.java`
- `src/main/java/.../MarkdownChunker.java`
2. Confirm the runtime endpoint from config.
   Default in this project:
- base URL: `http://localhost:8086`
- context path: `/api`
- ingest endpoint: `/rag/documents`
3. Rewrite the source content into question-level Markdown.
4. Produce a single shell command that:
- uses `CONTENT=$(cat <<'EOF' ... EOF)`
- uses `jq -n` to build JSON safely
- posts to `POST /api/rag/documents`
5. Use these defaults unless the user specifies otherwise:
- `corpusType: "job"`
- `documentType: "interview"`

## Markdown Shape

Prefer this structure:

```md
# 公司或主题标题

## 面经元信息
- 公司：xxx
- 岗位：xxx
- 类型：xxx

## Q1 题目标题
- 主题：xxx
- 考点：xxx

问题：
这里写题目正文
```

Rules:

- One interview question per `## Qn` section.
- Keep `面经元信息` separate from question sections.
- If there is a follow-up, put it under `追问：`.
- If there is an implementation requirement, put it under `要求：`.

## Command Template

Emit commands in this form:

```bash
CONTENT=$(cat <<'EOF'
# 标题
...
EOF
)

jq -n \
  --arg title "标题" \
  --arg sourceName "file-name.md" \
  --arg corpusType "job" \
  --arg documentType "interview" \
  --arg content "$CONTENT" \
  '{title:$title,sourceName:$sourceName,corpusType:$corpusType,documentType:$documentType,content:$content}' \
| curl -X POST http://localhost:8086/api/rag/documents \
  -H "Content-Type: application/json" \
  -d @-
```

## Output Requirements

When responding to the user:

- First give the cleaned Markdown if they need to inspect content.
- Then give one ready-to-run `bash` command.
- Keep the answer concise.
- Do not manually escape JSON by hand when `jq` can do it safely.

## Bovin-Specific Notes

- Bovin stores the raw markdown in `rag_document`.
- Bovin then chunks markdown and stores per-chunk vectors in `rag_chunk`.
- The embedding vector is created by the embedding model, not by PostgreSQL itself.
- `pgvector` stores and searches vectors; it does not generate them.