package com.eighthours.bovin.mock;

import com.eighthours.bovin.mock.dto.MockFeedback;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class AiMockFeedbackService implements MockFeedbackService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    public AiMockFeedbackService(ChatModel chatModel, ObjectMapper objectMapper) {
        this.chatModel = chatModel;
        this.objectMapper = objectMapper;
    }

    @Override
    public MockFeedback review(String questionText, String answerText) {
        String response = chatModel.call("""
                你是一名后端开发面试官，请根据面试题和候选人回答生成复盘。
                输出必须是严格 JSON，不要输出 markdown，不要输出解释。
                JSON 字段固定为：
                summary, strengths, missingPoints, improvements, suggestedAnswer

                面试题：
                %s

                候选人回答：
                %s
                """.formatted(questionText, answerText));
        try {
            return objectMapper.readValue(stripCodeFence(response), MockFeedback.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse mock feedback", exception);
        }
    }

    private String stripCodeFence(String response) {
        String trimmed = response == null ? "" : response.trim();
        if (trimmed.startsWith("```")) {
            int firstLineEnd = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
                return trimmed.substring(firstLineEnd + 1, lastFence).trim();
            }
        }
        return trimmed;
    }
}
