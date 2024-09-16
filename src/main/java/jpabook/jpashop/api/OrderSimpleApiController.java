package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

//Order
//Order->Member
//Order->Delivery
//xToOne(ManyToOne/OneToOne)
//ToMany 컬렉션 관계는 나중에 컬렉션에서 확인
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAll(new OrderSearch());
        //jpql로 Order만 가져오는데 이때 이에 연관된 쿼리를 전부 날려버려서 EARGR을 사용하면 안된다.
        //그리고 다른 API에서도 무조건 EARGR정보를 다 가져와서 성능도 난리가 난다.
//        return all;
        //이렇게 단순하게 정보를 반환하게 된다면?
        //orders에서 무한 루프에 빠지게 된다.
        //order>member로 가고 orders에 order에서 또 Member가 있어서 무한 루프에 빠지게 된다.
//        @OneToMany(mappedBy = "member")
//    @JsonIgnore
//        private List<Order> orders=new ArrayList<>();
//        이부분과 오더아이템 딜리버리에서도 이그노어로 양방향 관계를 끊어줘야 한다.
        //하지만 이래도 simpletype에러가 나오는데
        //지금 order를 가지고 왔는데 이때 member fetch가 lazy이기 때문에
        //지연로딩에서 프록시 객체를 넣어놓는데
        //하이버네이트에서 new ProxyMember()로 만들어놓는데 bytebuddy라는 에러가 나올 것
        //json라이브러리가 member를 가져오려는데 프록시여서 나오는 에러가 심플타입에러다.
        //지연로딩인 경우에는 json에 아무것도 하지말라고 설정해줄 수 있는데
        //이때 하이버네이트5모듈을 설치해야 된다.
        //implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5'
        //추가
//        @Bean
//        Hibernate5Module hibernate5Module() {
//            return new Hibernate5Module();
//        }
        //이렇게 등록 하면 값들이 null로 lazy들을 null로 나오게 해준다.
        //지연로직 부분을 프록시 가 아닌 null로 등록하도록 모듈이 이렇게 해준다.
        //하지만 이렇게 하면 엔티티에 따라 스팩도 달라지고 성능상 문제점도 많다.

        //만약 LAZY설정이 아닌
        for(Order order :all){
            order.getMember().getName();//LAZY를 강제 초기화하는 것
            order.getDelivery().getAddress();//LAZY를 강제 초기화하는 것
            //이렇게 getName을 활용하여 실제 엔티티정보를 넣도록 할 수 있다.
        }
        return all;
    }
    //하지만 API를 만들 때 이렇게 복잡하게 만들지 않는다.
    //회원정보 배송 상태같은 값들이 전부 필요한 게 아니라 필요한 데이터만 반환하도록 해야된다.
    //그래야 프론트와 원활하게 설계할 수 있다.
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2(){
        //이때 반환하는 값도 list가 아닌
        //별도의 DTO형식으로 Result로 감싸서 보내는 게 좋다.
        //결국 N+1을 일으키게 된다.
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
//                .map(o -> new SimpleOrderDto(o))
                .map(SimpleOrderDto::new)
                //람다 레퍼런스로 이렇게 묶을 수 있다.
                //map은 a->b로 변경하는 작업
                .collect(Collectors.toList());
                //값을 collect를 통해 list형식으로 변환
        return result;
        //API스팩에 맞춰서 설계한 것
//        "orderId": 1,
//                "name": "userA",
//                "orderDate": "2024-09-15T18:28:09.37286",
//                "orderStatus": "ORDER",
//                "address": {
//            "city": "서울",
//                    "street": "1",
//                    "zipcode": "1111"
//        } //이렇게 value object로 들어오는데 
        //값타입 관련 공부 필요
    }

    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        public SimpleOrderDto(Order order){
            //이렇게 엔티티로 받는건 괜찮다 중요하지 않은 곳에서 엔티티로 받는 것은
            //크게 중요하지 않는다.
            orderId=order.getId();
            name = order.getMember().getName(); //lazy 초기화
            orderDate = order.getOrderDate();
            orderStatus=order.getStatus();
            address=order.getDelivery().getAddress(); //lazy 초기화
            //이렇게 해놓으면 엔티티 수정 시 바로 컴파일 에러가 나오기 때문에
            //유지보수가 훨씬 편리해진다.
            //꼭 DTO로 바꿔서 보내는 게 좋다.

        }
    }
    //이때 v1/v2가 lazyLoding으로 통해 너무 많은 쿼리가 발생된다.
    //현재 오더/맴버/딜리버리 3개의 쿼리를 조회하기 때문에
    //오더에서 그 내부에 맴버에 대해 쿼리를 날리고 딜리버리에도 쿼리를 날려서
    //데이터를 가져오게 된다.
    //이때 오더 맴버 딜리버리르 조회하고 이후에 맴버의 ID값 하나씩만 넘겨서 확인하고
    //처음 주문도 3번 두번째 주문도 3번쿼리를 해버린다.
    //order 조회 ->SQL1번 -> 주문 2개
    //결과 주문 2개-> 맴버와 딜리버리에 대한 검색 루프가 두번 돈다
    //처음 검색할 때 주문에 등록된 id를 통해서 그 id만 가져오기 때문에
    //다음 쿼리 시에도 그 주문에 해당하는 id로 또 검색하게 되는 것
    //왜냐면 첫 탐색에서는 영속성 컨텍스트 내부에는 id로 조회한 단일 데이터만 들어있어서
    //또 두번째 오더에 대한 단일 id조회를 해버려서 2개를 조회할 때 쿼리가 5개가
    //나간다.
//    하지만 이때 ENGER를 사용하면 될까?? >>JPQL이 많아질수록
    //사용하면 안된다.
    //예측불가능한 쿼리가 나갈 수도 있어서 데이터에 대한 확실성이 없다.
    //양방향이 걸려있어서 쿼리 예측이 안되어 유지보수에서도 어려움이 생길 수 있다.
    //이때 왜 이렇게 될까? ENGER로 하면 ORDER를 가져와서
    //이후 맴버와 딜리버리정보를 전부 조회하려고 예측 불가능한 쿼리가 생성되는 것
    //그래서 LAZY로 해놓고 fetch Join 튜닝을 통해 n+1문제를 해결해야 된다.

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3(){
        List<Order> orders=orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
        return result;
        //실무에서는 이런 객체 그래프를 만드는게 정해져있어서
        //별도 설계 후 만들면 좋다.
        //이렇게 select로 전부 긁어오기 때문에 이에 대한 최적화도 필요하다.

    }
    //JPA에서 DTO로 바로 조회

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> orderV4() {
        return orderSimpleQueryRepository.findOrderDtos();
        //리포지토리에 DTO 조회는 API스팩이 리포지토리에 존재하게 되는 것
        //이걸 해결하기 위해서는?
        //성능 최적화 쿼리용 리포자토리를 별도로 생성한다.

    }
    //이렇게 dto조인을 하면 select절에서 원하는 것만 select가 가능하다.
    //fetch조인은 엔티티의 모든 정보를 다 가져오지만 DTO로 포멧하면
    //훨씬 쉽게 dto에 맞게 가져올 수 있다.
    //이때 v4/v3는 우열을 가리기 어렵다.
    //v3는 order로 원하는 것만 패치조인으로 원하는 것만 가져왔지만
    //외부 모습을 건드리지 않은 상태로 내부에 원하는 것만 패치조인으로
    //튜닝이 가능한건데
    //V4는 쿼리를 한번 할때 SQL짤때 작성하여 데이터를 가져올 수 있따.
    //하지만 V4는 재사용성이 낮지만 v3는 재사용성이 높다.
    //사용성 VS 조회 네트워크 성능
    //DTO은 엔티티가 아니라 변경이 불가능하다.
    //그리고V4는 코드가 지저분해서
}
