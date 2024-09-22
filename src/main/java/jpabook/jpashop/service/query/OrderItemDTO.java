package jpabook.jpashop.service.query;

import jpabook.jpashop.domain.OrderItem;
import lombok.Data;

@Data
public class OrderItemDTO{
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
