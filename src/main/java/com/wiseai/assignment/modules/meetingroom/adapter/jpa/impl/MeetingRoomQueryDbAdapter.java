package com.wiseai.assignment.modules.meetingroom.adapter.jpa.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.wiseai.assignment.modules.meetingroom.adapter.jpa.mapper.MeetingRoomEntityMapper;
import com.wiseai.assignment.modules.meetingroom.adapter.jpa.repository.MeetingRoomJpaRepository;
import com.wiseai.assignment.modules.meetingroom.application.port.out.query.MeetingRoomQueryPort;
import com.wiseai.assignment.modules.meetingroom.domain.model.MeetingRoom;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MeetingRoomQueryDbAdapter implements MeetingRoomQueryPort {

  private final MeetingRoomJpaRepository meetingRoomJpaRepository;
  private final MeetingRoomEntityMapper meetingRoomEntityMapper;

  @Override
  public List<MeetingRoom> findAll() {
    return meetingRoomJpaRepository.findAllByOrderByIdAsc().stream()
        .map(meetingRoomEntityMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<MeetingRoom> findById(Long id) {
    return meetingRoomJpaRepository.findById(id).map(meetingRoomEntityMapper::toDomain);
  }
}
