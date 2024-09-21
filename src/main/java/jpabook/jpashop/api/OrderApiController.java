package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDTO;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    //주문조회 v1 :엔티티 직접 노출
    @GetMapping("/api/v1/orders")
    public List<Order> orders1(){
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for (Order order : all) {
            //하이버네이트 기본 모듈을 통해 레이지 로딩을 호출해서 프록시가 초기화가 된 애들만 반환된다.
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            //ordetItems프록시 초기화
//            for (OrderItem orderItem : orderItems) {
//                orderItem.getItem().getName();
//                //아이템의 Name도 초기화
//            }
            //items람다식 변환
            orderItems.stream().forEach(o->o.getItem().getName());
            //주문과 관련된 모든 list를 돌리면서 아이템의 이름을 초기화
            //이렇게 강제 초기화를 해서 레이지 로딩으로 프록시를 강제 초기화
            //하이버네이트모듈은 원래 프록시를 안뿌리지만 강제 초기화로 인해 정보가 들어가는 것.

        }
        return all;
    }
    //이렇게 하면 1대다 관계로 상품명도 같이 출력하기 위해서 이렇게 객체 참조로 존재하기 때문에
    //강제 초기화해서 객체 그래프를 초기화하는 것
    //
    //주문조회 v2 :엔티티를 DTO로 변환 후 노출
    @GetMapping("/api/v2/orders")
    public List<OrderDTO> ordersV2(){
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<OrderDTO> result = orders.stream()
                .map(OrderDTO::new)
                //메소드 레퍼런스로 편리하게
                .collect(toList());
                //스태틱 임포트로 toList로 간략하게 만들 수 있다.
        return result;
    }//이때 지연로딩이 너무 많아서
    //이때 오더를 1번 조회 후 2개의 데이터에 대해서
    //member에 대한 조인을 하고 delivery에 대한 조회를 통해 address를 가져오고
    //이후 orderItems에 대한 정보를 또 조회하기 때문에
    //1(order)+2(delivery)+2(member)+4(orderItems) 이렇게 쿼리가 나가버린다.
    //이래서 이에 대한 최적화를 고민해야 된다.
    //이때 item이 lazy가 존재해서 모든 유저가 jpa1을 샀다면
    //지연로딩이 한번만 일어났겠지만 그게 아니다.
    //이렇게 되면 실시간 서비스에서 쿼리가 이렇게 많아지면 부담이 커지게 된다.
    @Data
    static class OrderDTO{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDTO> orderItems;
        //orderItem도 DTO로 변경
        //dto 생성자
        public OrderDTO(Order order){
            orderId=order.getId();
            name=order.getMember().getName();
            orderDate=order.getOrderDate();
            orderStatus=order.getStatus();
            address=order.getDelivery().getAddress();
            //orderItems=order.getOrderItems();이렇게만
            //넣으면 null값을 반환한다. 왜냐하면 orderItem이 엔티티이기 때문에
            //엔티티 초기화가 되지 않았기 때문이다.
//            order.getOrderItems().stream().forEach(o->o.getItem().getName());
            //프록시 초기화를 진행 후 다시 확인해보면 orderItem이 정상적으로
            //들어오는 것을 알 수 있다.
//            orderItems=order.getOrderItems();
            //하지만 DTO로 반환할 때 DTO안에 엔티티가 있으면 안된다.
            //래핑하는 것도 좋지 않다. 왜냐하면 오더아이템 엔티티 자체가 노출이 되기
            //때문이다.
            //엔티티에 관한 의존성을 반드시 끊어야 된다.
            //OrderItem조차 DTO로 전부 반환해야 한다.

            //그래서 이렇게 DTO로 변환해서 받아줘야 한다.
            orderItems=order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDTO(orderItem))
                    .collect(toList());
            //
        }
    }
    //이렇게 v2로 동작하면 Type definition error /no properties에러가 나오는데
    //getter setter가 없어서 나오는 에러이니 @Data를 꼭 넣어주자.
    //이때 orderItems가 안나오는데 이건 엔티티이기 때문에 나오지 않는다.

    //별도의 OrderItemDTO생성
    @Data
    static class OrderItemDTO{
        //상품명만 필요하기 때문에
        private String itemName;//상품명
        private int orderPrice;//주문 가격
        private int count;//주문 수량

        public OrderItemDTO(OrderItem orderItem){
            itemName=orderItem.getItem().getName();
            orderPrice=orderItem.getOrderPrice();
            count=orderItem.getCount();
        }
        //이렇게 래핑해서 내보내면 된다.
    }
    //연관된 모든 엔티티는 dto로 변환해서 내보내는 게 좋다.

    //엔티티를 DTO로 변환하는 작업을 fetch join을 통한 최적화
    @GetMapping("/api/v3/orders")
    public List<OrderDTO> ordersV3(){
        List<Order> orders=orderRepository.findAllWithItem();
        for (Order order : orders) {
            System.out.println("order = " + order+",id="+order.getId());
        }
//        order = jpabook.jpashop.domain.Order@2f0d68de,id=1
//        order = jpabook.jpashop.domain.Order@39c142fa,id=2
        //이렇게 처리가 되는것을 알 수 있다. 중복처리가 되는 것
        List<OrderDTO> result =orders.stream()
//                .map(OrderDTO::new)
                .map(o->new OrderDTO(o))
                .collect(toList());
        return result;
    }
    //이때 중복처리가 자동으로 되는데 영속성 컨텍스트에서 관리될때 동일한 엔티티는 동일한 객체로 간주되어
    //중복되지 않는 것처럼 처리되는데 이 메커니즘때문에 중복된 데이터가 자동으로 처리되는 것처럼 보인다.
    //Hibernate의 내부 캐시 및 영속성 컨텍스트 관리 메커니즘의 결과입니다.
    //또한 v2와 v3는 코드 자체는 같다.
    //리포지토리에서만 fetch조인 쿼리만 변경된 것이다.
    //하지만 여러번의 쿼리가 단일쿼리가 되도록 변경된 것
    //이렇게 한방 쿼리로 변경하는 게 훨씬 쉬워진다.
    //fetch join을 객체그래프만 그려주면 자동으로 처리해준다.
    //하지만 컬렉션에서 이러한 컬렉션 fetch조인이
    //db의 제약조건에 의해 단점이 존재하는데 페이징이 불가능하다.
    //1대다를 fetch조인을 하는 순간 페이징이 불가능하다.
    //.setFirstResult
    //.setMaxResults가 불가능하다.

    //주문 조회 V3.1 페이징과 한계돌파
    //order와 orderItem중 orderItem이 더 많으니 orderItem을 기준으로
    //join하는 DB의 성능에 의해 이러한 문제가 생기는데
    //이때문에 데이터를 예측할 수 없다.
    //이때 order를 기준으로 페이징해야 되는데
    //다(N)을 기준으로 row가 생성되는게 문제
    //데이터가 몇십 몇백만건이 나오는데 이걸 전송할 때
    //데이터를 fullimport를 할 경우 API로 제공하려는데
    //여러 join상황에서 최적화하기 위한 방법
    //페이징+컬렉션 엔티티를 함께 조회하려면?
    //1.ToOne관계를 모두 fetch조인을 한다.toOne관계는 row수를 증가시키지 않으니
    //페이징에 영향을 끼치지 않는다.
    //여기까진 리포지토리로 처리됨
    @GetMapping("/api/v3.1/orders")
    public List<OrderDTO> ordersV3_page(
        @RequestParam(value = "offset",defaultValue = "0") int offset,
        @RequestParam(value = "limit",defaultValue = "100") int limit
    ){
        List<Order> orders=orderRepository.findAllWithMemberDelivery(offset,limit);
        //findAllWithMemberDelivery()는 1대다 관계이기 때문에 페이징처리가 가능
        //파라미터 바인딩으로 limit offset설정이 들어간 것을 알 수 있다.
//        properties:
//        hibernate:
//#      show_sql: true #sysout으로 찍기 때문에 쓰면 안됨
//        format_sql: true
//        default_batch_fetch_size: 100
        //이렇게 default_batch_fetch_size: 100설정을 해준다.
        //offset이 0이면 그 명령은 지워져서 나간다.
//        이후 DTO에서
//        orderItems=order.getOrderItems().stream()
//                .map(orderItem -> new OrderItemDTO(orderItem))
//                .collect(toList());
        //위 과정에서 orderId가 userA와 userB의 정보를 in쿼리로 가져와버리는 것
        //oders와 관련된 orderItems를 in쿼리로 한번에 다 가져오는 것
        //미리 디폴트 사이즈에 대한 갯수를 미리 가져오는 것
        //orderItem의 item에 대해서 총 4번 호출되는 것도 이 옵션을 사용하면
        //한번에 4개를 다 가져온다.
        List<OrderDTO> result =orders.stream()
                .map(o->new OrderDTO(o))
                .collect(toList());
        return result;
        //이렇게 처리하면 기본적으로 member/delivery는 같이 가져오지만
        //orderItem 2번 item에 대해서 4번 총 7번의 조회가 일어난다.
        //하지만 배치설정을 통해 총 3번의 탐색만 되어서 데이터를 가져온다.
    }
    //2.컬렉션은 지연로딩으로 조회한다.
    //3.지연 로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size , @BatchSize 를 적용한다.
    //hibernate.default_batch_fetch_size: 글로벌 설정
    //@BatchSize: 개별 최적화
    //이 옵션을 사용하면 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size 만큼 IN 쿼리로 조회한다.

    //v3방식으로 조회를 한다면 order정보가 중복되어 생성되며
    //쿼리로 한번에 오지만 중복데이터에 대한 처리가 db에서 app으로 전송하여
    //전송량 자체가 많아진다.
    //전송 용량 자체가 많아지는 이슈가 생기는데
    //v3.1인 경우 쿼리는 order/member/delivery 1번
    //orderItem 1번 item 1번으로 총 3번의 쿼리가 나가지만
    //데이터 전송량이 필요한만큼만 나오도록 하고 중복이 없이 데이터가 깔끔하게 나올 수 있다.
    //필요한 값만 정확하게 가져오도록 할 수 있다. 테이블 단위 인쿼리이기 때문에 중복이
    //없다. 단 쿼리를 날릴 때 한방쿼리가 좋지만
    //데이터가 많은 경우 한방에 패치조인보단 분할 배치처리를 하는게 더 좋다.
    //또한 페이징이 가능하다는 이점도 존재한다.
    //대부분은 한번에 가져오는 게 좋지만 데이터량이 많다면 트레이오프 부분에 따라
    //데이터 분할이 중요하다. 배치 사이즈는 1000개정도가 적당하다.
    //배치사이즈가 작아지면 사이즈를 작게 가져와서 부하가 줄지만 시간은 좀 증가한다.
    //  was랑DB가 잘 버티면 높은게 좋다.
    //10개에서 1000개든 갯수는 똑같다.
    //사이즈가 적으면 lazy일 경우 뒤의 데이터는 안봐서 좋지만 보통은
    //데이터를 끝까지 다 돌린다.
    //DB에서 페이징해서 와야지 중간에 와서 거르는건 좋지 않다.
    //그래서 메모리는 루프가 다 돌때까지 기다려야 하기 떄문에
    //list가 다 찰때까지 기다린다.
//    jvm과는 별도로

    //주문 조회 V4: JPA에서 DTO 직접 조회
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDTO> orderV4(){
        return orderQueryRepository.findOrderQueryDtos();
    }
    //하지만 이것도 결국 N+1이다.

    //주문 조회 V5: JPA에서 DTO 직접 조회 N+1해결
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDTO> orderV5(){
        return orderQueryRepository.findAllByDto_optimization();
    }

//    주문 조회 V6: JPA에서 DTO로 직접 조회, 플랫 데이터 최적화
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDTO> orderV6(){

        List<OrderFlatDto> Flats = orderQueryRepository.findAllByDto_flat();
        //이때 중복을 포함해서 각각 4개가 나오는 것을 볼 수 있다.
        //주문번호가 중복으로 생성되어버린다.
        //한방 쿼리로 모든정보를 가져올 수 있고 페이징도 가능하다.
        //하지만 오더기준 페이징이 불가능하다. 이떄는 orderItems가 기준이 되어버리는 것
        //결국 페이징 불가능
        //또한 api스팩을 OrderQueryDTO로 맞춰야된다면?
        //orderFlatDto에서 orderId를 기준으로 직접 OrderQueryDTO형식으로 바꾸면 되는데
        //
        return Flats.stream()
                .collect(groupingBy(o -> new OrderQueryDTO(o.getOrderId(),o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDTO(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());
        //직접 메모리에서 작업하는 것
        //이떄 안되는이유는 OrderQueryDTO에서 equalsAndHashCode지정이 안되서
        //그룹핑이 안되는 것
        //DB에서 app에 전달하는 데이터가 중복 데이터가 추가되어 V5보단 느릴 수 있고
        //애플리케이션에서 추가작업이 많다.
        //페이징이 order기준으로 불가능하다.

        //하지만 Query를 1번만 날려서 데이터를 가져올 수 있다.
    }


}

