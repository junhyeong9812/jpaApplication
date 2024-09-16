package jpabook.jpashop.repository.order.simplequery;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

//dto는 엔티티를 참조해도 상관없다.
@Data
public class OrderSimpleQueryDto{
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

//    public OrderSimpleQueryDto(Order order){
        //이렇게 jpa에서 dto로 받기 위해서는 이렇게 엔티티로 객체를 받을 수 없다.
    //그리고 o는 식별자로 반환해야 된다.
public OrderSimpleQueryDto(Long orderId,String name,LocalDateTime orderDate,OrderStatus orderStatus,Address address){
    this.orderId=orderId;
    this.name =name; //lazy 초기화
    this.orderDate =orderDate;
    this.orderStatus=orderStatus;
    this.address=address; //lazy 초기화


    }
}
