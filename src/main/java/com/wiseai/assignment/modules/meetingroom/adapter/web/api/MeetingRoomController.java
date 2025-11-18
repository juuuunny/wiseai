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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MeetingRoomController implements MeetingRoomApi {

  private final GetMeetingRoomsUseCase getMeetingRoomsUseCase;
  private final GetMeetingRoomUseCase getMeetingRoomUseCase;

  @Override
  public ResponseEntity<SuccessResponse<List<MeetingRoomResponse>>> getMeetingRooms() {
    log.debug("회의실 목록 조회 API 요청");
    List<MeetingRoomResponse> meetingRooms = getMeetingRoomsUseCase.getAllMeetingRooms();
    log.debug("회의실 목록 조회 완료: {}개", meetingRooms.size());
    return ResponseEntity.ok(
        SuccessResponse.of(MeetingRoomSuccessStatus.OK_GET_MEETING_ROOMS, meetingRooms));
  }

  @Override
  public ResponseEntity<SuccessResponse<MeetingRoomResponse>> getMeetingRoom(Long id) {
    log.debug("회의실 단건 조회 API 요청: id={}", id);
    MeetingRoomResponse meetingRoom = getMeetingRoomUseCase.getMeetingRoom(id);
    log.debug("회의실 단건 조회 완료: id={}", id);
    return ResponseEntity.ok(
        SuccessResponse.of(MeetingRoomSuccessStatus.OK_GET_MEETING_ROOM, meetingRoom));
  }
}
