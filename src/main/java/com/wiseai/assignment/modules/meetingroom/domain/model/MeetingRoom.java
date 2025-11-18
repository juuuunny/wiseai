package com.wiseai.assignment.modules.meetingroom.domain.model;

import java.math.BigDecimal;

import com.wiseai.assignment.modules.meetingroom.domain.exception.MeetingRoomException;
import com.wiseai.assignment.modules.meetingroom.domain.status.MeetingRoomErrorStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeetingRoom {

  private final Long id;
  private final String name;
  private final Integer capacity;
  private final BigDecimal hourlyFee;
  private final String description;

  public static MeetingRoom create(String name, Integer capacity, BigDecimal hourlyFee, String description) {
    validateName(name);
    validateCapacity(capacity);
    validateHourlyFee(hourlyFee);

    return MeetingRoom.builder()
        .name(name.trim())
        .capacity(capacity)
        .hourlyFee(hourlyFee)
        .description(description)
        .build();
  }

  public MeetingRoom withId(Long id) {
    return MeetingRoom.builder()
        .id(id)
        .name(name)
        .capacity(capacity)
        .hourlyFee(hourlyFee)
        .description(description)
        .build();
  }

  public MeetingRoom updateInfo(String name, Integer capacity, BigDecimal hourlyFee, String description) {
    validateName(name);
    validateCapacity(capacity);
    validateHourlyFee(hourlyFee);

    return MeetingRoom.builder()
        .id(id)
        .name(name.trim())
        .capacity(capacity)
        .hourlyFee(hourlyFee)
        .description(description)
        .build();
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new MeetingRoomException(MeetingRoomErrorStatus.INVALID_NAME);
    }
    if (name.length() > 50) {
      throw new MeetingRoomException(MeetingRoomErrorStatus.INVALID_NAME);
    }
  }

  private static void validateCapacity(Integer capacity) {
    if (capacity == null || capacity < 1) {
      throw new MeetingRoomException(MeetingRoomErrorStatus.INVALID_CAPACITY);
    }
  }

  private static void validateHourlyFee(BigDecimal hourlyFee) {
    if (hourlyFee == null || hourlyFee.signum() < 0) {
      throw new MeetingRoomException(MeetingRoomErrorStatus.INVALID_HOURLY_FEE);
    }
  }
}

