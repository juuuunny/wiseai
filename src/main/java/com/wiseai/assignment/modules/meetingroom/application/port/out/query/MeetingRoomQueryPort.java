package com.wiseai.assignment.modules.meetingroom.application.port.out.query;

import java.util.List;
import java.util.Optional;

import com.wiseai.assignment.modules.meetingroom.domain.model.MeetingRoom;

public interface MeetingRoomQueryPort {
  List<MeetingRoom> findAll();

  Optional<MeetingRoom> findById(Long id);
}

