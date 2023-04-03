### Web 확장 - 도메인 클래스 컨버터

도메인 클래스 컨버터는 HTTP 파라미터로 넘어온 엔티티의 아이디로 엔티티 객체를 조회해서 바인딩하는 기법이다.

* 도메인 클래스 컨버터 사용 전

```java

@GetMapping("/members/{id}")
public String findMember(@PathVariable("id") Long id) {
    Member member = memberRepository.findById(id).get();
    return member.getUsername();
}

```
<br/>

* 도메인 클래스 컨버터 사용 후

```java

@GetMapping("/members2/{id}")
public String findMember2(@PathVariable("id") Member member) {
    return member.getUsername();
}

```

두 상황 모두 같은 select 쿼리가 나간다. 도메인 클래스 컨버터도 리포지토리를 사용해서 엔티티를 찾는다.

<br/>

+) 주의

도메인 클래스 컨버터로 엔티티를 파라미터로 받으면, 이 엔티티는 단순 조회용으로만 사용해야 한다. <br/>
(트랜잭션이 없는 범위에서 엔티티를 조회했으므로, 엔티티를 변경해도 DB에 반영되지 않는다) <br/>
또한 PK를 인자로 받으려면 외부에 공개해야 되기 때문에 자주 사용하는 기능은 아니다.

---

### Web 확장 - 페이징과 정렬

스프링 데이터가 제공하는 페이징과 정렬 기능을 스프링 MVC에서 편리하게 사용할 수 있다.

```java

@GetMapping("/members")
public Page<Member> list(Pageable pageable) {
   Page<Member> page = memberRepository.findAll(pageable);
   return page;
}

```

스프링 MVC 파라미터로 Pageable을 받을 수 있다. Pageable은 인터페이스이고, <br/>
실제로 PageRequest 객체를 생성해서 요청 파라미터 정보를 받는다.

위의 findAll처럼 어떤 쿼리라도 jpa에 page가 내장되어 있기에 Pageable을 사용할 수 있다

<br/>

* 요청 파라미터

ex) /members?page=0&size=3&sort=id,desc&sort=username,desc

page: 현재 페이지, 0부터 시작한다. <br/>
size: page 설정한 페이지에 노출할 데이터 건수 <br/>
sort: 정렬 조건을 정의한다

<br/>

반환 타입 역시 Page를 지원한다.

ex) 요청 파라미터 /members?page=0

```java

{
    "content": [
        {
            "id": 100,
            "username": "user99",
            "age": 99,
            "team": null
        },
        {
            "id": 99,
            "username": "user98",
            "age": 98,
            "team": null
        },
        {
            "id": 98,
            "username": "user97",
            "age": 97,
            "team": null
        }
    ],
    "pageable": {
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "pageNumber": 0,
        "pageSize": 3,
        "unpaged": false,
        "paged": true
    },
    "last": false,
    "totalElements": 100,
    "totalPages": 34,
    "size": 3,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 3,
    "empty": false
}

```

<br/>

#### 기본값

기본적으로 페이징의 default size는 20으로 설정되어 있다. 이 기본값은 변경할 수 있다.(10으로 바꾸고 싶어!)

* 글로벌 설정

application.yml - 내용 추가

```java
data:
    web:
      pageable:
        default-page-size: 10
        max-page-size: 2000

```

* 개별 설정

@PageableDefault 애노테이션 사용 , 전체에 적용하는 것이 아닌 하나의 매핑에만 하고 싶을 때 사용

```java

@GetMapping("/members")
public Page<MemberDto> list(@PageableDefault(size = 5) Pageable pageable) {...

```

개별 설정이 글로벌 설정보다 우선순위가 높다.

<br/><br/><br/>

#### DTO 변환

Page<Member>는 엔티티를 그대로 API로 노출하기 때문에 다양한 문제가 발생한다. 그래서 엔티티를 꼭 DTO로 변환해서 반환해야 한다.


```java

@GetMapping("/members")
    public Page<MemberDto> list(Pageable pageable){
        Page<Member> page = memberRepository.findAll(pageable);
        Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));
        return map;
    }

```
  
축약
    
```java    
   
@GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size = 5) Pageable pageable){
        Page<MemberDto> map = memberRepository.findAll(pageable).map(m -> new MemberDto(m.getId(), m.getUsername(), null));
        return map;
    }    
    
    
```  
    
<br/>
    

DTO에 변수로 Member엔티티를 받는 과정    
    
```java
  
  @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size = 5) Pageable pageable) {
        return memberRepository.findAll(pageable)
          .map(MemberDto::new);
    }
  
```
<br/><br/>

#### Page를 1부터 시작하기
    
스프링 데이터는 Page를 0부터 시작한다. 먄약 1부터 시작하려면?

 

1. Pageable, Page를 파라미터와 응답 값으로 사용하지 않고, 직접 클래스를 만들어서 처리한다.
    
2. spring.data.web.pageable.one-indexed-parameters: true 설정을 추가한다. 

그런데 이 방법은 Page의 다른 속성들은(pageSize, pageNumber, ...) page가 0부터 시작한다고 가정한 값이 저장되어 있기 때문에 한계가 있다. <br/>
-> 제일 깔끔한 방법은 그냥 페이지를 0부터 시작하는 것이다.    
    
