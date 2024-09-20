package jpabook.jpashop.repository.order.query;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderQueryDTO {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderItemQueryDto> orderItems;

    public OrderQueryDTO(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
//        this.orderItems = orderItems;
        //, List<OrderItemQueryDto> orderItems를 빼는 이유는
        //new operation에서 jpql을 짜도 컬렉션을 바로 넣을 수 없기 때문에
        //createquery에서도 뉴 오퍼에이션에서 dto의 1줄로 컬렉션을 제외한 값만 넣어야 한다
        //
    }
}
