package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository//스프링 빈으로 컴포넌트 스캔 대상이 되어서 등록됨
@RequiredArgsConstructor
public class MemberRepositoryOld {
//    @PersistenceContext//em을 자동으로 주입해준다.DI해줌
    //스프링이 생성한 em을 자동 주입을 해준다~ 팩토리에서 꺼낼 필요가 없다~
    private final EntityManager em;
    //스프링 jpa를 사용하면 PersistenceContext>>Autowired를 사용할 수 있다.

//    @PersistenceUnit
//      이걸로 팩토리도 주입받을 수 있다.
    public void save(Member member){
        em.persist(member);
    }
    public Member findOne(Long id){
        return em.find(Member.class,id);
    }
    //리스트 조회
    public List<Member> findAll(){
        return em.createQuery("select m from Member m",Member.class
        ).getResultList();//엔티티 객체 대상 쿼리
    }
    //사용자 이름을 통해
    public List<Member> findByName(String name){
        return em.createQuery("select m from Member m where m.name=:name",Member.class)
                .setParameter("name",name)
                .getResultList();
    }
    

}
