package com.eighthours.bovin.mock.controller;

import com.eighthours.bovin.mock.MockAnswerResponse;
import com.eighthours.bovin.mock.MockHistoryItem;
import com.eighthours.bovin.mock.MockInterviewService;
import com.eighthours.bovin.mock.MockQuestionResponse;
import com.eighthours.bovin.mock.dto.MockAnswerRequest;
import com.eighthours.bovin.mock.dto.MockQuestionRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mock")
public class MockInterviewController {

    private final MockInterviewService mockInterviewService;

    public MockInterviewController(MockInterviewService mockInterviewService) {
        this.mockInterviewService = mockInterviewService;
    }

    @PostMapping("/question")
    public MockQuestionResponse pickQuestion(@RequestBody MockQuestionRequest request) {
        return mockInterviewService.pickQuestion(request);
    }

    @PostMapping("/answer")
    public MockAnswerResponse submitAnswer(@RequestBody MockAnswerRequest request) {
        return mockInterviewService.submitAnswer(request);
    }

    @GetMapping("/history")
    public List<MockHistoryItem> history(@RequestParam(defaultValue = "10") int limit) {
        return mockInterviewService.history(limit);
    }
}
