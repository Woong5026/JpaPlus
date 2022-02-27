스프링 데이터 JPA는 리포지토리 사용시 자동으로 구현체를 생성해준다. 어떤 구현체를 생성해 주는 것일까?

-> 스프링 데이터 JPA가 제공하는 공통 인터페이스의 구현체는 SimpleJpaRepository이다.

* SimpleJpaRepository

```java

@Repository
@Transactional(readOnly = true)
public class SimpleJpaRepository<T, ID> ...{

   @Transactional
   public <S extends T> S save(S entity) {
   
       if (entityInformation.isNew(entity)) {
       em.persist(entity);
       return entity;
       } else {
       return em.merge(entity);
       }
   }
   ...
}

```

@Repository 적용: 컴포넌트 스캔의 대상이 되고, JPA 예외를 스프링이 추상화한 예외로 변환한다.

-> 리포지토리 기술을 JPA에서 Jdbc Template 등의 다른 기술로 바꾸더라도 기존 비즈니스 로직에 영향이 최소화된다.

<br/><br/>

@Transactional 적용: JPA의 모든 변경은 트랜잭션 안에서 동작한다.

스프링 데이터 JPA는 CURD를 트랜잭션 처리한다. 그래서 스프링 데이터 JPA는 사용 계층에서 트랜잭션이 없어도 변경이 가능하다.

<br/><br/>

* @Transactional(readOnly = true)

데이터를 단순히 조회만 하고 변경하지 않는 트랜잭션에서 readOnly = true 옵션을 사용하면

플러시를 생략해서 약간의 성능 향상을 얻을 수 있음

<br/><br/>

* save() 메서드

대표적으로 save() 메서드를 표시했는데 로직은 다음과 같다.

1. 새로운 엔티티면 저장(persist)

2. 새로운 엔티티가 아니면 병합(merge)



