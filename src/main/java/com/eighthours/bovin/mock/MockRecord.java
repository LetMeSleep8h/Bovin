package com.eighthours.bovin.mock;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mock_record")
public class MockRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long chunkId;
    private Long documentId;
    private String sourceName;
    private String questionText;
    private String answerText;
    private String feedbackJson;
    private LocalDateTime createdAt;
}
