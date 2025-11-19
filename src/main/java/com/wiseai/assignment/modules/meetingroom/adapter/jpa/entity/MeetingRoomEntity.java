package com.wiseai.assignment.modules.meetingroom.adapter.jpa.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.wiseai.assignment.modules.common.base.BaseTimeEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meeting_rooms")
@Getter
@NoArgsConstructor
public class MeetingRoomEntity extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(nullable = false)
  private Integer capacity;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal hourlyFee;

  @Column(columnDefinition = "TEXT")
  private String description;

  public MeetingRoomEntity(
      String name, Integer capacity, BigDecimal hourlyFee, String description) {
    this.name = name;
    this.capacity = capacity;
    this.hourlyFee = hourlyFee;
    this.description = description;
  }

  public void updateInfo(String name, Integer capacity, BigDecimal hourlyFee, String description) {
    this.name = name;
    this.capacity = capacity;
    this.hourlyFee = hourlyFee;
    this.description = description;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
