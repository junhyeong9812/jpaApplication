package jpabook.jpashop.api;

import jakarta.persistence.OneToMany;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

//Order
//Order->Member
//Order->Delivery
//xToOne(ManyToOne/OneToOne)
//ToMany 컬렉션 관계는 나중에 컬렉션에서 확인
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAll(new OrderSearch());
        //jpql로 Order만 가져오는데 이때 이에 연관된 쿼리를 전부 날려버려서 EARGR을 사용하면 안된다.
        //그리고 다른 API에서도 무조건 EARGR정보를 다 가져와서 성능도 난리가 난다.
//        return all;
        //이렇게 단순하게 정보를 반환하게 된다면?
        //orders에서 무한 루프에 빠지게 된다.
        //order>member로 가고 orders에 order에서 또 Member가 있어서 무한 루프에 빠지게 된다.
//        @OneToMany(mappedBy = "member")
//    @JsonIgnore
//        private List<Order> orders=new ArrayList<>();
//        이부분과 오더아이템 딜리버리에서도 이그노어로 양방향 관계를 끊어줘야 한다.
        //하지만 이래도 simpletype에러가 나오는데
        //지금 order를 가지고 왔는데 이때 member fetch가 lazy이기 때문에
        //지연로딩에서 프록시 객체를 넣어놓는데
        //하이버네이트에서 new ProxyMember()로 만들어놓는데 bytebuddy라는 에러가 나올 것
        //json라이브러리가 member를 가져오려는데 프록시여서 나오는 에러가 심플타입에러다.
        //지연로딩인 경우에는 json에 아무것도 하지말라고 설정해줄 수 있는데
        //이때 하이버네이트5모듈을 설치해야 된다.
        //implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5'
        //추가
//        @Bean
//        Hibernate5Module hibernate5Module() {
//            return new Hibernate5Module();
//        }
        //이렇게 등록 하면 값들이 null로 lazy들을 null로 나오게 해준다.
        //지연로직 부분을 프록시 가 아닌 null로 등록하도록 모듈이 이렇게 해준다.
        //하지만 이렇게 하면 엔티티에 따라 스팩도 달라지고 성능상 문제점도 많다.

        //만약 LAZY설정이 아닌
        for(Order order :all){
            order.getMember().getName();//LAZY를 강제 초기화하는 것
            order.getDelivery().getAddress();//LAZY를 강제 초기화하는 것
            //이렇게 getName을 활용하여 실제 엔티티정보를 넣도록 할 수 있다.
        }
        return all;
    }
    //하지만 API를 만들 때 이렇게 복잡하게 만들지 않는다.
    //회원정보 배송 상태같은 값들이 전부 필요한 게 아니라 필요한 데이터만 반환하도록 해야된다.
    //그래야 프론트와 원활하게 설계할 수 있다.
}
