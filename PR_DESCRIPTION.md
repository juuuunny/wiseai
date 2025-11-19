# [feat] 결제 상태 조회 API 구현 #40

## 변경 사항

- GET /payments/{paymentId}/status 엔드포인트 추가
- PaymentStatusQueryService 구현
- PaymentStatusResponse DTO 생성
- 결제 상태 조회 테스트 추가
- PaymentErrorStatus에 PAYMENT_GATEWAY_ERROR 추가

## 구현 내용

### API 엔드포인트
- `GET /payments/{paymentId}/status` - 결제 ID로 결제 상태 조회

### 응답 데이터
- paymentId: 결제 ID
- status: 결제 상태 (PENDING, COMPLETED, FAILED, CANCELLED)
- transactionId: 거래 ID (완료된 경우)

### 테스트
- 결제 상태 조회 성공 케이스
- 존재하지 않는 결제 조회
- 잘못된 ID 검증 (0)
- PENDING 상태 조회
