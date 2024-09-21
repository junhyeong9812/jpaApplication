package jpabook.jpashop.repository.order.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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



    //findOrderQueryDtos()의 N=1 컬렉션에서 최적화를 하기 위해서는?
    public List<OrderQueryDTO> findAllByDto_optimization() {
        //order는 그대로 가져온다.
        List<OrderQueryDTO> result = findOrders();
        //이전에는 루프를 돈다는 단점이 존재하였는데
        //이번에는 한번에 데이터를 가져올껀데
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        //id를 배열로 가져온다.
        //in절을 통해 한번에 가져온다.
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);
        //컬렉션을 통해 key가 OrderId이고 값은 ItemDTO로 변경된다.
        //메모리에 Map을 올려놓고 루프를 통해 넣어주는 것
        //이걸 key를 통해 order에 넣어주면 된다.
        result.forEach(o->o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return result;
        //앞에서는 루프마다 쿼리를 날렸지만
        // 이제는 처음 쿼리를 메모리에 맵형식으로 가져와서
        //메모리에서 메핑을 해주기 때문에 쿼리가 총 두번만 나간다.
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.order.query" +
                                ".OrderItemQueryDto(oi.order.id, i.name," +
                                "oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id IN :orderIds ", OrderItemQueryDto.class
                ).setParameter("orderIds", orderIds)
                .getResultList();
        //쿼리 한번에 배열의 items를 한번에 가져온 것
        //이후 map을 통해 최적화해주는 게 좋다
        Map<Long, List<OrderItemQueryDto>> orderItemMap =
                orderItems
                        .stream()
                        .collect(Collectors.groupingBy(OrderItemQueryDto -> OrderItemQueryDto.getOrderId()));
        return orderItemMap;
    }
    //주문 조회 V6: JPA에서 DTO로 직접 조회, 플랫 데이터 최적화
    public List<OrderFlatDto> findAllByDto_flat() {
        //db에서 쿼리를 한번에 전부 가져오도록 하면
        return em.createQuery(
                "select new " +
                        "jpabook.jpashop.repository.order.query.OrderFlatDto(o.id,m.name,o.orderDate,o.status,d.address,i.name,oi.orderPrice,oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i",OrderFlatDto.class
        ).getResultList();
        //이렇게 큰 쿼리로 반환한다면?
        //이때 중복을 포함해서 각각 4개가 나오는 것을 볼 수 있다.
        //주문번호가 중복으로 생성되어버린다.
    }
}
