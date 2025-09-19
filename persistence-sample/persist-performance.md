# JPA vs JDBC 성능 차이 분석

## 📊 성능 테스트 개요

Spring Boot 환경에서 JPA와 JDBC의 배치 처리 성능을 비교 분석한 문서입니다.

### 테스트 환경
- **Framework**: Spring Boot 3.x
- **Database**: H2 (In-Memory)
- **테스트 데이터**: 100, 500, 1000, 2500, 5000건
- **측정 항목**: 처리 시간(ms), 초당 처리량(ops/sec)

## 🔍 성능 차이의 근본적인 원인

### 1. 아키텍처 레벨의 차이

#### JDBC (Low-level)
```kotlin
// 직접 SQL 실행 - 중간 계층 없음
jdbcTemplate.batchUpdate("UPDATE test_entity SET name = ? WHERE id = ?", ...)
```

**특징:**
- Database Driver와 직접 통신
- 최소 오버헤드: SQL → PreparedStatement → DB
- 단순한 데이터 구조: 원시 타입 배열만 사용

#### JPA (High-level)
```kotlin
// 여러 추상화 계층 거침
entity.name = "Updated"  // 변경 감지 트리거
entityManager.flush()     // 변경사항 동기화
```

**특징:**
- 다층 구조: Entity → EntityManager → Hibernate → JDBC → DB
- 복잡한 처리: 변경 감지, 캐시 관리, 라이프사이클 관리

### 2. 변경 감지(Dirty Checking) 오버헤드

#### JPA의 변경 감지 프로세스

```java
// JPA가 내부적으로 수행하는 작업
1. 엔티티 로드 시 스냅샷 저장
2. flush() 시점에 현재 상태와 스냅샷 비교
3. 변경된 필드만 찾아서 UPDATE SQL 생성
4. 1차 캐시에서 관리
```

**비용이 발생하는 지점:**
- **스냅샷 저장**: 각 엔티티마다 원본 상태 복사본 유지
- **필드별 비교**: 모든 필드를 하나씩 비교 (reflection 사용)
- **동적 SQL 생성**: 런타임에 SQL 문장 생성

### 3. 1차 캐시(First-Level Cache) 관리

#### 메모리 사용 패턴 비교

```kotlin
// JPA - 모든 엔티티를 메모리에 유지
val entities = repository.findAll()  // 5000개 로드
// 5000개 엔티티 + 5000개 스냅샷 = 메모리에 10000개 객체

// JDBC - 단순 데이터만 처리
jdbcTemplate.batchUpdate(...)  // 파라미터 배열만 메모리 사용
```

**성능 영향:**
- **GC 압박**: 대량 객체로 인한 Garbage Collection 빈발
- **캐시 검색**: 엔티티 조회 시 캐시 순회 비용
- **메모리 대역폭**: 객체 그래프 탐색 오버헤드

### 4. SQL 생성 방식의 차이

#### JDBC - 컴파일 타임 SQL
```kotlin
// SQL이 미리 정의됨
"UPDATE test_entity SET name = ?, amount = ? WHERE id = ?"
```
- 정적 SQL 문자열
- 파싱 비용 최소화
- 준비된 구문 재사용

#### JPA - 런타임 SQL 생성
```java
// Hibernate가 동적으로 생성
UPDATE test_entity 
SET name = ?, amount = ?, updated_at = ?  // 변경된 필드만
WHERE id = ?
```
- 변경된 필드만 포함하는 동적 SQL
- 매번 SQL 생성 비용 발생
- 최적화된 쿼리지만 생성 오버헤드 존재

### 5. 배치 처리 최적화 수준

#### JDBC Batch의 실제 동작
```sql
-- JDBC는 실제로 이렇게 전송
BEGIN BATCH
  UPDATE ... WHERE id = 1;
  UPDATE ... WHERE id = 2;
  UPDATE ... WHERE id = 3;
  ...
COMMIT BATCH
```
- 네트워크 라운드트립 최소화
- 데이터베이스 레벨 배치 최적화
- 단일 트랜잭션으로 처리

#### JPA의 배치 처리
```java
// hibernate.jdbc.batch_size = 100 설정해도
// 각 엔티티마다 별도 처리
for (entity : entities) {
    1. 변경 감지 수행
    2. SQL 생성
    3. 배치에 추가 (조건부)
    4. 캐시 동기화
}
```
- 엔티티별 개별 처리
- 조건부 배치 적용
- 추가 동기화 오버헤드

### 6. 프록시와 지연 로딩 체크

```java
// JPA는 각 필드 접근 시 체크
entity.getName()  
// 1. 프록시인지 확인
// 2. 초기화 여부 확인
// 3. 필요시 쿼리 실행
// 4. 값 반환
```

### 7. 트랜잭션 동기화 오버헤드

#### JPA의 트랜잭션 관리
각 작업마다 수행되는 체크사항:
1. 트랜잭션 컨텍스트 확인
2. EntityManager 동기화 상태 체크
3. FlushMode 확인
4. CASCADE 옵션 처리
5. 이벤트 리스너 호출 (@PreUpdate, @PostUpdate)

### 8. 통계 및 로깅 오버헤드

```java
// Hibernate Statistics가 수집하는 정보
- 쿼리 실행 횟수 기록
- 캐시 hit/miss 율 계산
- 엔티티 로드/업데이트 카운트
- 세션 메트릭스
```

## 📈 성능 차이 정량화

### 일반적인 성능 비율 (대략적)

| 기술 | 상대 성능 | 특징 |
|------|-----------|------|
| JDBC Batch Update | 1x (기준) | 최고 성능, 낮은 수준 제어 |
| JPA Bulk Update (JPQL) | 1.5-2x slower | 단일 쿼리, 엔티티 우회 |
| JPA Batch Optimized | 3-5x slower | 주기적 flush/clear |
| JPA Dirty Checking | 5-10x slower | 순수 변경 감지, 대량 데이터에서 급격히 저하 |

## 🎯 UPDATE 전략별 특징

### 1. JDBC Batch Update
```kotlin
jdbcTemplate.batchUpdate(
    "UPDATE test_entity SET name = ? WHERE id = ?",
    batchArgs
)
```
- ✅ 최고 성능
- ✅ 메모리 효율적
- ❌ 수동 SQL 작성
- ❌ 타입 안정성 부족

### 2. JPA Dirty Checking
```kotlin
entities.forEach { entity ->
    entity.name = "Updated"
}
entityManager.flush()
```
- ✅ 가장 간편한 사용
- ✅ 엔티티 라이프사이클 완전 지원
- ❌ 대량 데이터 성능 저하
- ❌ 메모리 사용량 높음

### 3. JPA Batch Optimized
```kotlin
entities.forEachIndexed { index, entity ->
    entity.name = "Updated"
    if (index % 100 == 0) {
        entityManager.flush()
        entityManager.clear()
    }
}
```
- ✅ 메모리 관리 개선
- ✅ 중간 규모 데이터에 적합
- ⚠️ 수동 최적화 필요
- ❌ 캐시 이점 상실

### 4. JPA Bulk Update (JPQL)
```kotlin
entityManager.createQuery(
    "UPDATE TestEntity e SET e.name = :name WHERE e.id IN :ids"
)
.setParameter("name", "Updated")
.setParameter("ids", idList)
.executeUpdate()
```
- ✅ 단일 쿼리로 처리
- ✅ 우수한 성능
- ❌ 엔티티 라이프사이클 우회
- ❌ 캐시 동기화 필요

## 💡 최적화 가이드

### JPA 성능 개선 방법

#### 1. 배치 사이즈 튜닝
```properties
spring.jpa.properties.hibernate.jdbc.batch_size=100
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

#### 2. 주기적 flush/clear
```kotlin
val batchSize = 100
entities.forEachIndexed { index, entity ->
    // 작업 수행
    if ((index + 1) % batchSize == 0) {
        entityManager.flush()
        entityManager.clear()
    }
}
```

#### 3. 읽기 전용 트랜잭션
```kotlin
@Transactional(readOnly = true)
fun readOnlyOperation() {
    // 변경 감지 비활성화
}
```

#### 4. StatelessSession 사용
```kotlin
val statelessSession = sessionFactory.openStatelessSession()
// 1차 캐시, 변경 감지 없음
```

## 🔧 선택 기준

### JPA를 선택해야 할 때
- ✅ 복잡한 비즈니스 로직
- ✅ 엔티티 간 연관관계 관리 필요
- ✅ 개발 생산성 우선
- ✅ 중소 규모 데이터 (< 1000건)
- ✅ 유지보수성 중요

### JDBC를 선택해야 할 때
- ✅ 대량 데이터 처리 (> 10000건)
- ✅ 단순 CRUD 작업
- ✅ 극한의 성능 필요
- ✅ 복잡한 네이티브 쿼리
- ✅ 특수한 데이터베이스 기능 사용

### 하이브리드 접근법
```kotlin
@Service
class DataService(
    private val repository: TestEntityRepository,  // JPA for complex logic
    private val jdbcTemplate: JdbcTemplate        // JDBC for batch operations
) {
    // 복잡한 비즈니스 로직
    fun complexBusinessLogic(id: Long) = repository.findById(id)
    
    // 대량 배치 처리
    fun batchUpdate(data: List<Data>) = jdbcTemplate.batchUpdate(...)
}
```

## 📝 결론

### 핵심 요약
1. **성능 차이는 추상화 레벨에서 기인**: JPA는 많은 기능을 제공하는 대가로 성능 오버헤드 발생
2. **적절한 도구 선택이 중요**: 모든 상황에 최적인 단일 솔루션은 없음
3. **하이브리드 접근 권장**: 상황에 따라 JPA와 JDBC를 적절히 혼용

### 실무 권장사항
- **일반 CRUD**: JPA 사용 (개발 생산성)
- **배치 처리**: JDBC 또는 JPA Bulk Operations
- **복잡한 쿼리**: JPQL 또는 Native Query
- **대량 데이터 마이그레이션**: 순수 JDBC

### 성능 모니터링
```kotlin
// 개발 환경에서 SQL 로깅 활성화
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql=TRACE
spring.jpa.properties.hibernate.generate_statistics=true
```

이러한 이해를 바탕으로 각 프로젝트의 요구사항에 맞는 최적의 기술을 선택하고, 필요시 적절한 최적화를 적용하는 것이 중요합니다.