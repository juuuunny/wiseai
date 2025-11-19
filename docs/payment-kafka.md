# Kafka 기반 결제 처리 설계

## 목표
- 결제 생성 요청을 이벤트로 비동기 처리
- 외부 PG 호출 실패 시 재시도/추적 가능하게 함
- 멱등성(eventId) 기반으로 중복 처리 방지

## 이벤트 흐름
1. `PaymentCommandService.createPayment()`가 결제를 저장
2. 커밋 이후 `PaymentEventProducer`가 `payment.process` 토픽에 이벤트 발행
3. `PaymentProcessListener`가 이벤트를 소비하여 PG 호출
4. 처리 성공 시 Payment 상태 `COMPLETED`, 실패 시 `FAILED` 업데이트

## 멱등성 전략
- `payment_process_log` 테이블에 `(event_id, payment_id)` 기록
- Listener는 처리 전에 `event_id` 존재 여부 검사
- 중복 이벤트인 경우 PG 호출/상태 변경을 건너뜀

## 장애 대응
- 결제 처리 실패 시 `payment.process.dlq` 토픽으로 이벤트 이동 (`PaymentDlqProducer`)
- DLQ 레코드는 별도 재처리 스크립트/배치로 복구 예정

## Outbox 고려
- 현재 단계에서는 트랜잭션 싱크 후 `KafkaTemplate` 송신
- 추후 멱등성/재처리 강화가 필요하면 Outbox 테이블 + Debezium 구조로 확장 가능

