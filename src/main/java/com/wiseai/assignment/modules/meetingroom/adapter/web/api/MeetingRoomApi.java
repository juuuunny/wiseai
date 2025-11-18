package com.wiseai.assignment.modules.meetingroom.adapter.web.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.wiseai.assignment.modules.common.dto.response.SuccessResponse;
import com.wiseai.assignment.modules.meetingroom.application.dto.response.MeetingRoomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "MeetingRoom", description = "회의실 관리 API")
public interface MeetingRoomApi {

  @Operation(summary = "회의실 목록 조회", description = "전체 회의실 목록을 조회합니다.")
  @GetMapping("/meeting-rooms")
  ResponseEntity<SuccessResponse<List<MeetingRoomResponse>>> getMeetingRooms();

  @Operation(summary = "회의실 단건 조회", description = "ID로 특정 회의실을 조회합니다.")
  @GetMapping("/meeting-rooms/{id}")
  ResponseEntity<SuccessResponse<MeetingRoomResponse>> getMeetingRoom(@PathVariable Long id);
}
