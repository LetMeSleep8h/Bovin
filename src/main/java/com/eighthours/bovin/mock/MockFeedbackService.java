package com.eighthours.bovin.mock;

import com.eighthours.bovin.mock.dto.MockFeedback;

public interface MockFeedbackService {

    MockFeedback review(String questionText, String answerText);
}
