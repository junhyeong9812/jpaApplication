//package jpabook.jpashop;
//
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import jpabook.jpashop.domain.Member;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public class MemberRepository {
//    @PersistenceContext
//    private EntityManager em;
//    //스프링 부트가 자동 주입을 해준다.
//
//    public Long save(Member member){
//        em.persist(member);
//        return member.getId();
//    }
//    //커멘드와 쿼리는 분리하는 게 좋다
//    //저장하면 리턴 값을 만드는 게 아니라 단일 id정보만 반환하도록 설계
//
//    public Member find(Long id){
//        return em.find(Member.class,id);
//    }
//
//}
