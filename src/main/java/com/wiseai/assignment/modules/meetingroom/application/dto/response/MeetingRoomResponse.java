package com.wiseai.assignment.modules.meetingroom.application.dto.response;

import java.math.BigDecimal;

public record MeetingRoomResponse(
    Long id, String name, Integer capacity, BigDecimal hourlyFee, String description) {}
