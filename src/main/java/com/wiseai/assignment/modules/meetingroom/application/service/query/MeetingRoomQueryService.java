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
public class MeetingRoomQueryService implements GetMeetingRoomsUseCase, GetMeetingRoomUseCase {

  private final MeetingRoomQueryPort meetingRoomQueryPort;

  @Override
  @Transactional(readOnly = true)
  public List<MeetingRoomResponse> getAllMeetingRooms() {
    log.debug("회의실 목록 조회 요청");
    List<MeetingRoomResponse> result =
        meetingRoomQueryPort.findAll().stream()
            .map(
                meetingRoom ->
                    new MeetingRoomResponse(
                        meetingRoom.getId(),
                        meetingRoom.getName(),
                        meetingRoom.getCapacity(),
                        meetingRoom.getHourlyFee(),
                        meetingRoom.getDescription()))
            .toList();
    log.debug("회의실 목록 조회 완료: {}개", result.size());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public MeetingRoomResponse getMeetingRoom(Long id) {
    log.debug("회의실 단건 조회 요청: id={}", id);
    MeetingRoomResponse result =
        meetingRoomQueryPort
            .findById(id)
            .map(
                meetingRoom ->
                    new MeetingRoomResponse(
                        meetingRoom.getId(),
                        meetingRoom.getName(),
                        meetingRoom.getCapacity(),
                        meetingRoom.getHourlyFee(),
                        meetingRoom.getDescription()))
            .orElseThrow(
                () -> {
                  log.warn("회의실을 찾을 수 없음: id={}", id);
                  return new MeetingRoomException(MeetingRoomErrorStatus.NOT_FOUND);
                });
    log.debug("회의실 단건 조회 완료: id={}", id);
    return result;
  }
}
