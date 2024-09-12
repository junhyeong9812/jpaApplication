package jpabook.jpashop;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
//- userA
//    - JPA1 BOOK
//    - JPA2 BOOK
//- userB
//    - SPRING1 BOOK
//    - SPRING2 BOOK
@Component
@RequiredArgsConstructor
public class initDb {
    private final InitService initService;

    @PostConstruct
    //애플리케이션 로딩 시점에 호출 하기 위함
//    이 어노테이션이 붙은 메서드는 해당 빈(Bean)이 생성된 후,
//    의존성 주입이 완료된 직후에 호출
    public void init(){
        initService.dbInit1();
        //스프링 라이프 사이클이 있어서 별도의 빈으로 동작해서 트렌젝션을 넣어줘야 한다.
        initService.dbInit2();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService{
        private final EntityManager em;
        public void dbInit1(){
            Member member = createMember("userA", "서울", "1", "1111");
            em.persist(member);

            Book book1 = createBook("JPA1 BOOK", 10000, 100);
            em.persist(book1);
            Book book2 = createBook("JPA2 BOOK", 20000, 100);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            //...을 통해 위와 같이 여러개의 orderItem을 넘긴다.
            em.persist(order);
        }


        public void dbInit2(){
            Member member = new Member();
            member.setName("UserB");
            member.setAddress(new Address("진주","2","1122"));
            em.persist(member);

            Book book1 = createBook("SPRING1 BOOK", 20000, 200);
            em.persist(book1);
            Book book2 = createBook("SPRING2 BOOK", 40000, 300);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);
            Order order = Order.createOrder(member, createDelivery(member), orderItem1, orderItem2);
            //...을 통해 위와 같이 여러개의 orderItem을 넘긴다.
            em.persist(order);
        }
        private Member createMember(String name, String city, String street,
                                    String zipcode) {
            Member member = new Member();
            member.setName(name);
            member.setAddress(new Address(city, street, zipcode));
            return member;
        }
        private Book createBook(String name, int price, int stockQuantity) {
            Book book = new Book();
            book.setName(name);
            book.setPrice(price);
            book.setStockQuantity(stockQuantity);
            //ctrl alt p로 파라미터로 변경
            return book;
        }
        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }
    }
    //결국 주문이나 회원이나 주문정보를 엮어서 주문을 만들텐데
    //이제는 연관관계가 걸린 API를 설계를 하면서 어떻게 해결하는 지
    //및 지연 성능 최적화

}


