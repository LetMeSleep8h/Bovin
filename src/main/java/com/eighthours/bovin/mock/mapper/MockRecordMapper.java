package com.eighthours.bovin.mock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eighthours.bovin.mock.MockRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MockRecordMapper extends BaseMapper<MockRecord> {

    @Select("""
            SELECT id, chunk_id, document_id, source_name, question_text, answer_text, feedback_json, created_at
            FROM mock_record
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<MockRecord> selectRecent(@Param("limit") int limit);
}
