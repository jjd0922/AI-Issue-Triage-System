# AI-Issue-Triage-System

운영 환경에서 발생하는 CS/장애 이슈를 LLM과 RAG로 분석하고, 개발 대응 우선순위와 처리 방향을 자동 제안하는 백엔드 시스템입니다.

## 기술 스택

- Java 17
- Spring Boot 3.x
- Gradle
- MySQL 8.4
- Kafka
- Redis
- Docker Compose

## 로컬 실행

인프라 실행:

```powershell
docker compose up -d
```

애플리케이션 실행:

```powershell
.\gradlew.bat bootRun
```

테스트 실행:

```powershell
.\gradlew.bat test
```

인프라 종료:

```powershell
docker compose down
```

데이터까지 삭제:

```powershell
docker compose down -v
```

## 로컬 인프라

| Component | Host |
| --- | --- |
| MySQL | `localhost:3306` |
| Kafka | `localhost:9092` |
| Redis | `localhost:6379` |

MySQL 기본 계정:

| Name | Value |
| --- | --- |
| Database | `ai_issue_triage` |
| User | `ai_issue` |
| Password | `ai_issue` |
| Root password | `root` |

## API

Base URL:

```text
http://localhost:8080/api/v1
```

이슈 등록:

```http
POST /issues
Content-Type: application/json
```

```json
{
  "title": "결제는 완료됐는데 주문이 생성되지 않았습니다.",
  "content": "고객은 결제 성공 문자를 받았지만 주문 내역이 비어 있습니다.",
  "source": "CUSTOMER_SERVICE"
}
```

주요 API:

```http
GET /issues/{issueId}
GET /issues?page=0&size=20&sort=createdAt,desc
GET /issues/{issueId}/analysis
POST /issues/{issueId}/analysis/retry
```

## 분석 흐름

```text
이슈 등록 API 호출
-> Issue 저장
-> 트랜잭션 커밋 후 Kafka 이벤트 발행
-> Kafka Consumer 수신
-> Mock AI 분석
-> KnowledgeDocument 기반 RAG Mock 검색
-> IssueAnalysis 저장
-> Issue 상태 ANALYZED 변경
-> Redis 캐시 저장
-> 분석 결과 API 조회
```

## Kafka

분석 요청 topic:

```text
issue-analysis-requested
```

Payload:

```json
{
  "eventId": "evt-uuid",
  "issueId": 1,
  "title": "결제는 완료됐는데 주문이 생성되지 않았습니다.",
  "requestedAt": "2026-05-08T10:00:00"
}
```

## 테스트 전략

- 도메인 상태 전이 단위 테스트
- Application service 단위 테스트
- REST Controller slice 테스트
- Kafka producer/consumer 단위 테스트
- Redis adapter 단위 테스트
- 핵심 흐름 통합 테스트

핵심 흐름 통합 테스트는 외부 Docker 인프라 없이 fake port를 사용합니다. 실제 MySQL/Kafka/Redis 연동 검증은 Docker Compose 환경에서 수동 또는 후속 Testcontainers 테스트로 확장합니다.
