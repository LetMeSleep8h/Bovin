package com.eighthours.bovin.mock.dto;

import java.util.List;

public record MockFeedback(
        String summary,
        List<String> strengths,
        List<String> missingPoints,
        List<String> improvements,
        String suggestedAnswer
) {
}
