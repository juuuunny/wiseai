package com.wiseai.assignment.modules.meetingroom.application.port.in.query;

import com.wiseai.assignment.modules.meetingroom.application.dto.response.MeetingRoomResponse;

public interface GetMeetingRoomUseCase {
  MeetingRoomResponse getMeetingRoom(Long id);
}

