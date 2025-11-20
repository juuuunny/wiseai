package com.wiseai.assignment.modules.reservation.adapter.jpa.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.wiseai.assignment.modules.common.base.BaseTimeEntity;
import com.wiseai.assignment.modules.reservation.domain.enums.ReservationStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor
public class ReservationEntity extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long meetingRoomId;

  @Column(nullable = false)
  private Long userId;

  @Column(nullable = false)
  private LocalDateTime startTime;

  @Column(nullable = false)
  private LocalDateTime endTime;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ReservationStatus status;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal totalAmount;

  public ReservationEntity(
      Long meetingRoomId,
      Long userId,
      LocalDateTime startTime,
      LocalDateTime endTime,
      ReservationStatus status,
      BigDecimal totalAmount) {
    this.meetingRoomId = meetingRoomId;
    this.userId = userId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.status = status;
    this.totalAmount = totalAmount;
  }

  public void updateStatus(ReservationStatus status) {
    this.status = status;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
