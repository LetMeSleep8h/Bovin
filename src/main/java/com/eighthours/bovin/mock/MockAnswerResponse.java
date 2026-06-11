package com.eighthours.bovin.mock;

import com.eighthours.bovin.mock.dto.MockFeedback;

public record MockAnswerResponse(
        Long recordId,
        MockFeedback feedback
) {
}
