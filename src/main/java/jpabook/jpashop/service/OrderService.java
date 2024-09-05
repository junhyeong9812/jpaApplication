package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    
    //주문
    @Transactional
    public Long order(Long memberId,Long itemId,int count){
        //엔티티 조회
        Member member=memberRepository.findOne(memberId);
        Item item =itemRepository.findOne(itemId);

        //배송정보 생성
        Delivery delivery =new Delivery();
        delivery.setAddress(member.getAddress());
        //주문상품 생성
        OrderItem orderItem=OrderItem.createOrderItem(item, item.getPrice(),count);

        //주문 생성
        Order order = Order.createOrder(member,delivery,orderItem);
        //원래는 배송정보 및 주문 정보도 전부 따로 persist를 해줘야 하지만 cascade설정이
        //되어 있어서 이대로 오더만 저장하면 나머지도 전부 저장된다고 보면 된다.
        //이런 케스케이드의 범위는 참조 주인이 1개일 경우에만 사용해야 된다.

        //주문 저장
        orderRepository.save(order);
        return order.getId();


    }
    //코드는 제약 스타일로 구성하는 게 좋다.

    //취소
    @Transactional
    public void cancelOrder(Long orderId){
        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        //주문 취소
        order.cancel();
    }
    
    //검색
//    public List<Order> findOrder(OrderSearch orderSearch){
//        return orderRepository.findAll(orderSearch);
//    }
}
