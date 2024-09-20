package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore//jsonIgnore는 순환구조에 빠질 수 있기 때문에 필수
    private Order order;
    //여기가 FK이기 떄문에 order를 실질적으로 참조하지 않는다.

    private int orderPrice;//주문 가격
    private int count;//주문 수량

//    protected OrderItem(){}

    //생성 메소드
    public static OrderItem createOrderItem(Item item,int orderPrice,int count){
        OrderItem orderItem=new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);
        item.removeStock(count);

        return orderItem;
    }

    //비즈니스 로직
    public void cancel(){
        getItem().addStock(count);
    }
    //조회 로직
    //주문 상품 가격 조회
    public int getTotalPrice() {
     return getOrderPrice()*getCount();
    }
}
