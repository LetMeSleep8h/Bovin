package com.eighthours.bovin.rag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rag_document")
public class RagDocument {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String sourceName;
    private String corpusType;
    private String documentType;
    private String rawMarkdown;
    private LocalDateTime createdAt;
}
