package jpabook.jpashop.service.query;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
@Data
public class OrderDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderItemDTO> orderItems;
    //orderItem도 DTO로 변경
    //dto 생성자
    public OrderDto(Order order){
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
