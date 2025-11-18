package com.wiseai.assignment.modules.meetingroom.application.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wiseai.assignment.modules.meetingroom.application.dto.response.MeetingRoomResponse;
import com.wiseai.assignment.modules.meetingroom.application.port.in.query.GetMeetingRoomUseCase;
import com.wiseai.assignment.modules.meetingroom.application.port.in.query.GetMeetingRoomsUseCase;
import com.wiseai.assignment.modules.meetingroom.application.port.out.query.MeetingRoomQueryPort;
import com.wiseai.assignment.modules.meetingroom.domain.exception.MeetingRoomException;
import com.wiseai.assignment.modules.meetingroom.domain.status.MeetingRoomErrorStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingRoomQueryService
    implements GetMeetingRoomsUseCase, GetMeetingRoomUseCase {

  private final MeetingRoomQueryPort meetingRoomQueryPort;

  @Override
  @Transactional(readOnly = true)
  public List<MeetingRoomResponse> getAllMeetingRooms() {
    return meetingRoomQueryPort.findAll().stream()
        .map(
            meetingRoom ->
                new MeetingRoomResponse(
                    meetingRoom.getId(),
                    meetingRoom.getName(),
                    meetingRoom.getCapacity(),
                    meetingRoom.getHourlyFee(),
                    meetingRoom.getDescription()))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public MeetingRoomResponse getMeetingRoom(Long id) {
    return meetingRoomQueryPort
        .findById(id)
        .map(
            meetingRoom ->
                new MeetingRoomResponse(
                    meetingRoom.getId(),
                    meetingRoom.getName(),
                    meetingRoom.getCapacity(),
                    meetingRoom.getHourlyFee(),
                    meetingRoom.getDescription()))
        .orElseThrow(() -> new MeetingRoomException(MeetingRoomErrorStatus.NOT_FOUND));
  }
}

