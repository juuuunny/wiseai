package com.wiseai.assignment.modules.meetingroom.application.port.out.command;

import com.wiseai.assignment.modules.meetingroom.domain.model.MeetingRoom;

public interface MeetingRoomCommandPort {
  MeetingRoom save(MeetingRoom meetingRoom);

  MeetingRoom update(MeetingRoom meetingRoom);

  void delete(Long id);
}

