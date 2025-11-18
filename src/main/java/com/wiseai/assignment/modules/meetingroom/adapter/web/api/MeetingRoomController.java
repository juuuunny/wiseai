package com.wiseai.assignment.modules.meetingroom.adapter.web.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.wiseai.assignment.modules.common.dto.response.SuccessResponse;
import com.wiseai.assignment.modules.meetingroom.application.dto.response.MeetingRoomResponse;
import com.wiseai.assignment.modules.meetingroom.application.port.in.query.GetMeetingRoomUseCase;
import com.wiseai.assignment.modules.meetingroom.application.port.in.query.GetMeetingRoomsUseCase;
import com.wiseai.assignment.modules.meetingroom.domain.status.MeetingRoomSuccessStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MeetingRoomController implements MeetingRoomApi {

  private final GetMeetingRoomsUseCase getMeetingRoomsUseCase;
  private final GetMeetingRoomUseCase getMeetingRoomUseCase;

  @Override
  public ResponseEntity<SuccessResponse<List<MeetingRoomResponse>>> getMeetingRooms() {
    List<MeetingRoomResponse> meetingRooms = getMeetingRoomsUseCase.getAllMeetingRooms();
    return ResponseEntity.ok(
        SuccessResponse.of(MeetingRoomSuccessStatus.OK_GET_MEETING_ROOMS, meetingRooms));
  }

  @Override
  public ResponseEntity<SuccessResponse<MeetingRoomResponse>> getMeetingRoom(Long id) {
    MeetingRoomResponse meetingRoom = getMeetingRoomUseCase.getMeetingRoom(id);
    return ResponseEntity.ok(
        SuccessResponse.of(MeetingRoomSuccessStatus.OK_GET_MEETING_ROOM, meetingRoom));
  }
}

