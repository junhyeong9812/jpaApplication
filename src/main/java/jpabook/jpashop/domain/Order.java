package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")//FK설정
    private Member member;
    //테이블은 FK하나만 변경하면 되기 때문에 둘 중 하나를 주인으로 잡으면 되는데
    //이때 Order가 맴버와 연관관계에서 주인이 된다.

    //JPQL select o from order o;-> order가 100개라면 처음 쿼리1개와
    //그 오더에 대한 맴버를 단건 조회로 100개 가져와서 N+1문제라 한다.

//    @BatchSize(size = 1000)
    @OneToMany(mappedBy = "order",cascade =CascadeType.ALL)
    private List<OrderItem> orderItems =new ArrayList<>();
//,cascade =CascadeType.ALL는 오더를 저장할 때 오더아이템 객체를 넣어놓으면
    //같이 저장되도록 한다.
    //엔티티는 각각 persist하는게 기본이지만 cascade를 통핸 한번에 가능하게 할 수 있다.
    @OneToOne(fetch = FetchType.LAZY,cascade =CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;
    //

    private LocalDateTime orderDate;//하이버네이트가 자동으로 지원을 해준다.

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER,CANCEL]

    //연관관계 메소드를 통해 양방향 관계를 위해 값을 넣어준다.
    public void setMember(Member member){
        this.member=member;
        member.getOrders().add(this);
    }//이렇게 양방향 연관관계를 매핑
    public void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
    //==생성 메서드==//
    public static Order createOrder(Member member,Delivery delivery,OrderItem... orderItems){
        Order order= new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for(OrderItem orderItem : orderItems){
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }
    //==비즈니스 로직==/
//    주문 취소
    public void cancel(){
        if(delivery.getStatus()==DeliveryStatus.COMP){
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }
        this.setStatus(OrderStatus.CANCEL);
        for(OrderItem orderItem: orderItems){
            orderItem.cancel();
            //오더아이템들 켄슬
        }

    }
    //==조회 로직==//
//    전체 주문 가격 조회
    public int getTotalPrice(){
        int totalPrice=0;
        for (OrderItem orderItem : orderItems){
            totalPrice+=orderItem.getTotalPrice();
        }
        return totalPrice;
    }
}
