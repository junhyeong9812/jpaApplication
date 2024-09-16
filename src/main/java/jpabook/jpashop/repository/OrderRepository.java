package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;
    public void save(Order order){
        em.persist(order);
    }
    public Order findOne(Long id){
        return em.find(Order.class,id);
    }

    //검색에는 동적 쿼리가 필요
    public List<Order> findAll(OrderSearch orderSearch){
        //조건이 다 있다면
//        em.createQuery("select o from Order o join o.member m " +
//                "where o.status=:status" +
//                " and m.name=:name",Order.class)
//                .setParameter("status",orderSearch.getOrderStatus())
//                .setParameter("name",orderSearch.getMemberName())
//                .setMaxResults(1000)//.setFirstResult>>페이징 처리
//                .getResultList();
        //검색 조건에 둘 다 있으면 이렇게 쿼리를 짜면 되지만 만약 둘 다 없거나 둘 중 하나도
        //없다면?
        //이걸 동적 쿼리로 해결해야 된다.
        //동적 쿼리 방식
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }
        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }
    //JPA Criteria로 처리
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName()
                            + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        //최대 1000건
        return query.getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {
        //order를 가져올 때 맴버와 딜리버리까지 가져오도록 설계
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
        //select절에서 한번 쿼리로 오더 맴버 딜리버리를 조인해서 모든 데이터를
        //한번에 가져오는 것
        //이렇게 하면 프록시가 아닌 진짜 객체로 전부 가져온다.
        //이런 형식을 fetch조인이라 한다.
        //sql join을 jpa의 fetch문법으로 가져오는 것
        //fetch조인 연관된 엔티티 객체를 같이 가져오도록 할 수 있다.
        //n+1문제가 대부분 90%정도의 성능문제를 일으키기 때문에
        //fetch join만 잘 써도 어느정도 성능 최적화가 가능하다.
//        select o1_0.order_id,
//                d1_0.delivery_id,d1_0.city,d1_0.street,d1_0.zipcode,d1_0.status
//                ,m1_0.member_id,m1_0.city,m1_0.street,m1_0.zipcode,m1_0.name,o1_0.
//                order_date,o1_0.status from orders o1_0 join member m1_0 on m1_0.member_id=o1_0.member_id
//        join delivery d1_0 on d1_0.delivery_id=o1_0.delivery_id
        //이렇게 깔끔하게 객체 그래프로 가져오는 것을 볼 수 있다.
        //inner join을 하고 싶으면 left join fetch를 하면 된다.
//        이처럼 fetch조인만 잘 써도 성능 최적화가 가능하다.
    }
    
//    //DTO로 바로 구현
//    public List<OrderSimpleQueryDto> findOrderDtos() {
////        return em.createQuery(
////                "select o from Order o" +
////                        " join o.member m" +
////                        " join o.delivery d",OrderSimpleQueryDto.class
////        ).getResultList();
//        //이렇게 하면 바로 매핑이 안되서 엔티티나 임베더블만 반환되고
//        //dto는 안된다.
//        //""내에서 엔티티가 식별자로 넘어가버리기 때문에 (o)로 못넣는다.
//        return em.createQuery(
//                "select new jpabook.jpashop.repository" +
//                        ".OrderSimpleQueryDto(o.id,m.name,o.orderDate,o.status,d.address)" +
//                        " from Order o" +
//                        " join o.member m" +
//                        " join o.delivery d",OrderSimpleQueryDto.class
//        ).getResultList();
//        //이건 논리적으로 계층이 깨져있는 상태이기 때문에
//        //리파지토리가 화면에 의존하게 되는 것
//        //API스팩이 바뀌면 이 리파지토리 자체를 변경해야된다.
//        //이러한 단점이 존재한다.
//        //v3와 v4의 성능차이가 많이 난다.
//        //대부분에서 네트워크가 좋아서도 있지만
//        //대부분 성능애서 from innerjoin쪽에서
//        //먹는다
//        //인덱스를 안타고 필드가 추가된다고 성능에 대한 유무는 크지 않다.
//        //인덱스가 잘못 잡혔을 때 문제가 생기는거지
//        //이런 select객체는 문제가 생기지 않는다.
//        //하지만 select가 너무 많으면 이때는 고민해봐야 한다.
//        //트래픽이 심할 경우에는 (고객이 실시간으로 누르는 API일 경우
//        //최적화에 대한 고민을 하게 된다.
//    }



//    public List<Order> findAll(OrderSearch orderSearch) {
//
//        QOrder order = QOrder.order;
//        QMember member = QMember.member;
//
//        return query
//                .select(order)
//                .from(order)
//                .join(order.member, member)
//                .where(statusEq(orderSearch.getOrderStatus()),
//                        nameLike(orderSearch.getMemberName()))
//                .limit(1000)
//                .fetch();
//    }
//지금 위의 빌드 타입을 이렇게 간략하게 설계할 수 있다.


}
