package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Delivery {
    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;
    @OneToOne(mappedBy = "delivery",fetch = FetchType.LAZY)
    private Order order;

    @Embedded
    private Address address;
    @Enumerated(EnumType.STRING)//무조건 String으로 사용
    //숫자로 하면 내용이 추가되도 이전 순서를 그대로 가지고 있어서 순서가 꼬일 가능성이 존재
    private DeliveryStatus status; //READY,COMP
}
