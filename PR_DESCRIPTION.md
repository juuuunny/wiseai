## ✅ PR 유형

어떤 변경 사항이 있었나요?

- [x] 새로운 기능 추가

---

## 📝 작업 내용

이번 PR에서 작업한 내용을 간략히 설명해주세요(이미지 첨부 가능)

### 통합 테스트 작성 (#45)

- **예약-결제 통합 플로우 테스트**: 예약 생성 → 결제 처리 → 결제 상태 조회 전체 플로우 검증
- **Deadlock 방지 통합 테스트**: 동시 예약 생성, 시간 교환 시나리오 등 Deadlock 방지 로직 검증
- **Mock 결제 API 통합 테스트**: PaymentGateway Mock을 사용한 결제 처리 테스트

### 주요 구현 내용

1. **통합 테스트 인프라 구성**
   - `IntegrationTestConfig`: Kafka 관련 컴포넌트 Mock 처리
   - `application-test.yaml`: 테스트 환경 설정 (H2, Kafka 제외)
   - `PaymentKafkaConfig`에 `@Profile("!test")` 추가

2. **통합 테스트 작성**
   - `ReservationPaymentIntegrationTest`: 예약-결제 통합 플로우, 중복 방지 테스트
   - `DeadlockPreventionIntegrationTest`: Deadlock 방지 시나리오 테스트
   - `MockPaymentApiIntegrationTest`: Mock 결제 게이트웨이 테스트

3. **테스트 환경 설정**
   - H2 인메모리 DB 사용
   - Kafka, Redis 관련 컴포넌트 Mock 처리
   - RedissonClient Mock 처리

---

## ✏️ 관련 이슈

본인이 작업한 내용이 어떤 Issue Number와 관련이 있는지만 작성해주세요

- Resolves : #45

---

## 🧪 테스트

- [x] 단위 테스트 작성 및 통과
- [x] 통합 테스트 작성 및 통과
- [x] 전체 테스트 실행 (166개 테스트 모두 통과)
- [x] Spotless 포맷팅 적용

---

## 📊 테스트 결과

- **총 테스트 수**: 166개
- **통과**: 166개
- **실패**: 0개
- **통합 테스트**: 9개 (모두 통과)
