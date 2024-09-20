package jpabook.jpashop.repository.order.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final EntityManager em;
    
    //쿼리나 API에 의존되는 쿼리들은 별도의 패키지로 분리하여 리포지토리를 만든다.
    //화면과 관련된 것은 쿼리와 관련될 가능성이 크기 때문에 별도로 분리하여
    //라이프사이클을 분할할 수 있다.
    //핵심 비즈니스 로직 처리와 화면단 비즈니스 로직 처리의 라이프 사이클은 다르다.
    public List<OrderQueryDTO> findOrderQueryDtos() {
        //별도의 쿼리 리파지토리의 DTO를 생성한 이유는
        //orderApiCOntroller에 dto를 참조하게 되면 순환구조가
        //되기 때문에 별도의 쿼리 DTO를 같은 패키지에 생성
        List<OrderQueryDTO> result = findOrders();//쿼리1
        //오더아이템을 넣어줘야 한다.
        result.forEach(o->{//쿼리N개 >>N+1문제와 같다.
            List<OrderItemQueryDto> orderItems=findOrderItems(o.getOrderId());
            //직접 오더아이템들을 넣어준다.
            o.setOrderItems(orderItems);

        });
        return result;
    }
    //orderItem 조회
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query" +
                        ".OrderItemQueryDto(oi.order.id, i.name," +
                        "oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id= :orderId", OrderItemQueryDto.class
        ).setParameter("orderId",orderId)
                .getResultList();
    }

    //우선 오더의 값들을 오퍼레이션을 통해 넣는다.
    //* 1:N 관계(컬렉션)를 제외한 나머지를 한번에 조회
    private List<OrderQueryDTO> findOrders() {
        return em.createQuery("select new jpabook.jpashop.repository.order.query.OrderQueryDTO(o.id, m.name, o.orderDate," +
                        "o.status, d.address) from Order o" +
                " join o.member m" +
                " join o.delivery d",OrderQueryDTO.class)
                .getResultList();
    }
}
