package com.wiseai.assignment.modules.meetingroom.adapter.jpa.mapper;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.meetingroom.adapter.jpa.entity.MeetingRoomEntity;
import com.wiseai.assignment.modules.meetingroom.domain.model.MeetingRoom;

@Component
public class MeetingRoomEntityMapper {

  public MeetingRoom toDomain(MeetingRoomEntity entity) {
    return MeetingRoom.builder()
        .id(entity.getId())
        .name(entity.getName())
        .capacity(entity.getCapacity())
        .hourlyFee(entity.getHourlyFee())
        .description(entity.getDescription())
        .build();
  }

  public MeetingRoomEntity toEntity(MeetingRoom meetingRoom) {
    MeetingRoomEntity entity =
        new MeetingRoomEntity(
            meetingRoom.getName(),
            meetingRoom.getCapacity(),
            meetingRoom.getHourlyFee(),
            meetingRoom.getDescription());
    // ID가 있는 경우에만 설정 (update 시)
    if (meetingRoom.getId() != null) {
      entity.setId(meetingRoom.getId());
    }
    return entity;
  }

  public void updateEntity(MeetingRoomEntity entity, MeetingRoom meetingRoom) {
    entity.updateInfo(
        meetingRoom.getName(),
        meetingRoom.getCapacity(),
        meetingRoom.getHourlyFee(),
        meetingRoom.getDescription());
  }
}
