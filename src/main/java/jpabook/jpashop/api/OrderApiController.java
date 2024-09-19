package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;

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
}
