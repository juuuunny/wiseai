# [feat] 결제 완료 시 예약 상태 자동 확정 처리 #36

## 변경 사항

- PaymentProcessListener에서 결제 완료 후 Reservation.confirm() 호출
- 결제 완료 시 예약 상태 PENDING → CONFIRMED로 자동 변경
- 결제-예약 통합 로직 구현

## 테스트

- PaymentProcessListenerTest 업데이트
- 결제 완료 시 예약 확정 검증 추가

