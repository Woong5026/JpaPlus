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




