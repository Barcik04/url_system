package com.example.url_system.dtos;

import com.example.url_system.models.Plan;

import java.time.Instant;

public record UserPlanResponse(
        Plan plan,
        Boolean cancellationScheduled,
        Instant subscriptionEnd
) {
}
