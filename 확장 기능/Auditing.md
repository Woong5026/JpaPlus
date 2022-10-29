엔티티를 생성, 변경할 때 변경한 사람과 시간을 추적하고 싶다면?

-> 등록일, 수정일, 등록자, 수정자 <br/>
+) 특히 실무에서는 등록일, 수정일을 표기해주는 것이 도움이 된다.

### 순수 JPA 사용

순수 JPA를 사용하면 다음과 같다.

* JpaBaseEntity

```java

@MappedSuperclass
@Getter
public class JpaBaseEntity {

    @Column(updatable = false)
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
}

```

이렇게 @MappedSuperclass를 생성하고 Member 엔티티에서 상속 받으면 Member Table을 생성할 때, 부모 클래스의 필드들을 컬럼으로 추가하게 된다.

@PrePersist와 @PreUpdate는 JPA의 이벤트 어노테이션이다.


* Member

```java
public class Member extends JpaBaseEntity {...

```

<br/>

+) @EntityListener?

감시할 대상 Entity에 AuditingEntityListener를 등록 및 필드 정의 <br/>
해당 엔티티 클래스에 @EntityListener를 선언하고, 이 안에 AuditingEntityListener를 등록한다.  

엔티티를 DB에 적용하기 이전 이후에 커스텀 콜백을 요청할 수 있는 어노테이션으로,<br/>
해당 엔티티 라이프 사이클 중 특점 시점에 원하는 로직을 처리할 수 있게 한다. <br/>
JPA에서는 다음과 같은 7가지 이벤트를 지원한다. 

@PrePersist : 새로운 엔티티에 대해 persist가 호출되기 전 <br/>
@PreUpdate : 엔티티 업데이트 작업 전 <br/>
@PreRemove : 엔티티가 제거되기 전  <br/>
@PostPersist : 새로운 엔티티에 대해 persist가 호출된 후 <br/>
@PostUpdate : 엔티티가 업데이트된 후 <br/>
@PostRemove : 엔티티가 삭제된 후 <br/>
@PostLoad : Select조회가 일어난 직후에 실행

<br/>

---

* 스프링 데이터 JPA 사용

먼저 스프링 부트 설정 클래스에 @EnableJpaAuditing을 적용한다.

```java

** @EnableJpaAuditing
@SpringBootApplication
public class JpaPlusApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaPlusApplication.class, args);
    }

    ** @Bean
    public AuditorAware<String> auditorProvider(){
        return  () -> Optional.of(UUID.randomUUID().toString());
    }
}

```

그리고 @EntityListeners(AuditingEntityListener.class)를 엔티티에 적용한다.( 생성자 , 수정자를 랜덤으로 생성하기 위해)

<br/><br/>

@CreatedDate (org.springframework.data) <br/>
데이터 생성 날짜 자동 저장 어노테이션

@LastModifiedDate (org.springframework.data) <br/>
데이터 수정 날짜 자동 저장 어노테이션

@CreatedBy (org.springframework.data) <br/>
데이터 생성자 자동 저장 어노테이션

@LastModifiedBy (org.springframework.data) <br/>
데이터 수정자 자동 저장 어노테이션

<br/>

* BaseEntity

```java

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseEntity {
   @CreatedDate
   @Column(updatable = false)
   private LocalDateTime createdDate;
   
   @LastModifiedDate
   private LocalDateTime lastModifiedDate;
   
   @CreatedBy
   @Column(updatable = false)
   private String createdBy;
   
   @LastModifiedBy
   private String lastModifiedBy;
}

```

@CreatedDate, @LastModifiedDate 어노테이션으로 보다 편리하게 Auditing을 사용할 수 있다.

<br/><br/>

이 때, 등록자, 수정자를 처리해주는 AuditorAware 스프링 빈을 등록한다.

```java

@EnableJpaAuditing
@SpringBootApplication
public class DataJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataJpaApplication.class, args);
	}

	@Bean
	public AuditorAware<String> auditorProvider() {
		return () -> Optional.of(UUID.randomUUID().toString());
	}

}

```

등록, 수정될 때마다 AuditorAware 메서드를 호출해서 등록자, 수정자를 처리한다.

+) 예제에서는 랜덤 UUID를 받았지만 실제로는 세션 정보나, 시큐리티 로그인 정보에서에서 ID를 꺼내 사용한다.

<br/><br/>

참고로 실무에서 대부분의 엔티티는 등록시간, 수정시간이 필요하지만 등록자, 수정자는 없을 수도 있다. <br/>
그래서 다음과 같이 Base 타입을 분리하고, 원하는 타입을 선택해서 상속한다.

* BaseTimeEntity

```java

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseTimeEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;
}

```

* BaseEntity

```java

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseEntity extends BaseTimeEntity {

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;
}

```

<br/>

+) @MappedSuperclass가 아닌 임베디드 값 타입을 사용하면 안될까? <br/>
-> 이것은 상속을 사용하냐 위임을 사용하냐의 차이다. <br/>
객체지향의 일반적인 법칙에 따르면 상속보다는 위임이 좋겠지만, JPA에서는 상속을 사용하는게 더욱 편리하다.

예를 들어 임베디드 타입이 다음과 같이 있다고 하자.

```java
class TraceDate {
  TYPE createdDate;
  TYPE updatedDate;
}


```
만약 JPQL을 사용한다면 항상 임베디드 타입을 적어주어야 하는 불편함이 있다.

```java

select m from Member m where m.traceDate.createdDate > ?


```

@MappedSuperclass, 상속을 사용하면 다음과 같이 간단하고 쉽게 풀린다.

```java

select m from Member m where m.createdDate > ?


```
