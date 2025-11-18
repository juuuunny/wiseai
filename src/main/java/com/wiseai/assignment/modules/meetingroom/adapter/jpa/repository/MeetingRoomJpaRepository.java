package com.wiseai.assignment.modules.meetingroom.adapter.jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wiseai.assignment.modules.meetingroom.adapter.jpa.entity.MeetingRoomEntity;

public interface MeetingRoomJpaRepository extends JpaRepository<MeetingRoomEntity, Long> {
  List<MeetingRoomEntity> findAllByOrderByIdAsc();
}
