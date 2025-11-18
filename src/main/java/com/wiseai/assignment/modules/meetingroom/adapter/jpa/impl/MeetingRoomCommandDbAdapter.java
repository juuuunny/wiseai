package com.wiseai.assignment.modules.meetingroom.adapter.jpa.impl;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.meetingroom.adapter.jpa.entity.MeetingRoomEntity;
import com.wiseai.assignment.modules.meetingroom.adapter.jpa.mapper.MeetingRoomEntityMapper;
import com.wiseai.assignment.modules.meetingroom.adapter.jpa.repository.MeetingRoomJpaRepository;
import com.wiseai.assignment.modules.meetingroom.application.port.out.command.MeetingRoomCommandPort;
import com.wiseai.assignment.modules.meetingroom.domain.model.MeetingRoom;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingRoomCommandDbAdapter implements MeetingRoomCommandPort {

  private final MeetingRoomJpaRepository meetingRoomJpaRepository;
  private final MeetingRoomEntityMapper meetingRoomEntityMapper;

  @Override
  public MeetingRoom save(MeetingRoom meetingRoom) {
    MeetingRoomEntity entity = meetingRoomEntityMapper.toEntity(meetingRoom);
    MeetingRoomEntity saved = meetingRoomJpaRepository.save(entity);
    return meetingRoomEntityMapper.toDomain(saved).withId(saved.getId());
  }

  @Override
  public MeetingRoom update(MeetingRoom meetingRoom) {
    MeetingRoomEntity entity =
        meetingRoomJpaRepository
            .findById(meetingRoom.getId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "MeetingRoom not found with id: " + meetingRoom.getId()));
    meetingRoomEntityMapper.updateEntity(entity, meetingRoom);
    MeetingRoomEntity updated = meetingRoomJpaRepository.save(entity);
    return meetingRoomEntityMapper.toDomain(updated);
  }

  @Override
  public void delete(Long id) {
    meetingRoomJpaRepository.deleteById(id);
  }
}
