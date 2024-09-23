package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member,Long> {
    //이때 jpaRepository를 참조하고 타입과 id값의 타입을 <>안에 넣어주면 된다.
    //JpaRepository에 이미 기본적인 save나 find같은 함수들은 다 만들어져있다.
//    구현체도 spring jpa가 알아서 만들어준다.

    //이때 해결이 안되는 게 findOne>>findById로 변경되었고
    //스프링 jpa는 Optional로 반환하기 때문에
    List<Member> findByName(String name);
    //이렇게만 작성해도 이름 규칙에 따라서
    //select m from Member m where m.name= :name
    //위와 같이 쿼리를 생성한다.
    //findBy????가 select m from Member m where m.????=?
    //가 되는 것이다.
    //이렇게 기본적인 메소드들이 전부 있기 때문에 훨씬 편리하다.
    //그리고 메서드 이름을 통해 구현되도록 할 수 있다.
    //메서드명을 읽어서 구현체가 자동으로 앱 실행시점에 주입하여 생성된다.
    
    //하지만 이렇게 편리하지만 결국 JPA를 사용해서 편리한 기능을 제공할 뿐이지
    //jpa자체를 잘 이해하여 이용하는 게 중요하다.
    //실무에서는 jpa나 jpql을 사용할 일이 정말 많다.
    //em,영속성 컨텍스트,jpql을 잘 모르면 장애가 나기 정말 좋다.
    //하지만 JPA의 한계를 모르고 실무에서 사용하면 그 제약에 의해
    //롤백해야될 수 있다.

}
