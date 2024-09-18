package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
