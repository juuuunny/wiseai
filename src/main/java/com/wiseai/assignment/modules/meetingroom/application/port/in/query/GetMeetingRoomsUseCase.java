package com.wiseai.assignment.modules.meetingroom.application.port.in.query;

import java.util.List;

import com.wiseai.assignment.modules.meetingroom.application.dto.response.MeetingRoomResponse;

public interface GetMeetingRoomsUseCase {
  List<MeetingRoomResponse> getAllMeetingRooms();
}
