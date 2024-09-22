package jpabook.jpashop.service.query;

import jpabook.jpashop.api.OrderApiController;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {
    //프로젝트가 커지면 비지니스 구조에 맞게 명확하게 분리하는 게 좋다.
    //도메인을 분리하는 게 좋다.
    //여기서 컨트롤러의 변환 로직들을 돌리는 함수들을 만들어서 사용하는 것
    private final OrderRepository orderRepository;

    public List<OrderDto> ordersV3OSIV(){
        List<Order> orders=orderRepository.findAllWithItem();
        for (Order order : orders) {
            System.out.println("order = " + order+",id="+order.getId());
        }

        List<OrderDto> result =orders.stream()
                .map(o->new OrderDto(o))
                .collect(toList());
        return result;
    }
    //이런식으로 코드를 분리하는 게 좋은 것





}
