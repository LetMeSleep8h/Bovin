package com.eighthours.bovin.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eighthours.bovin.rag.entity.RagChunk;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RagChunkMapper extends BaseMapper<RagChunk> {

    @Insert("""
            INSERT INTO rag_chunk (document_id, chunk_index, heading, chunk_text, fts_text, embedding)
            VALUES (#{documentId}, #{chunkIndex}, #{heading}, #{chunkText}, #{ftsText}, CAST(#{embedding} AS vector))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertChunk(RagChunk chunk);

    @Select("""
            <script>
            SELECT c.id AS chunk_id,
                   c.document_id,
                   d.title,
                   d.source_name,
                   d.corpus_type,
                   d.document_type,
                   c.chunk_text,
                   ts_rank_cd(c.fts, websearch_to_tsquery('simple', #{query})) AS score
            FROM rag_chunk c
            JOIN rag_document d ON d.id = c.document_id
            WHERE c.fts @@ websearch_to_tsquery('simple', #{query})
            <if test='targetCorpus != null and targetCorpus != "" and targetCorpus != "both"'>
              AND d.corpus_type = #{targetCorpus}
            </if>
            ORDER BY score DESC, c.id ASC
            LIMIT #{topK}
            </script>
            """)
    List<RagChunkRow> keywordSearch(@Param("query") String query, @Param("targetCorpus") String targetCorpus, @Param("topK") int topK);

    @Select("""
            <script>
            SELECT c.id AS chunk_id,
                   c.document_id,
                   d.title,
                   d.source_name,
                   d.corpus_type,
                   d.document_type,
                   c.chunk_text,
                   1 - (c.embedding &lt;=&gt; CAST(#{embedding} AS vector)) AS score
            FROM rag_chunk c
            JOIN rag_document d ON d.id = c.document_id
            <where>
              <if test='targetCorpus != null and targetCorpus != "" and targetCorpus != "both"'>
                d.corpus_type = #{targetCorpus}
              </if>
            </where>
            ORDER BY c.embedding &lt;=&gt; CAST(#{embedding} AS vector), c.id ASC
            LIMIT #{topK}
            </script>
            """)
    List<RagChunkRow> vectorSearch(@Param("embedding") String embedding, @Param("targetCorpus") String targetCorpus, @Param("topK") int topK);
}
