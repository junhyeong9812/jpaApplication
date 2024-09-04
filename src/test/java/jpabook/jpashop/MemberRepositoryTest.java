//package jpabook.jpashop;
//
//import jpabook.jpashop.domain.Member;
//import org.assertj.core.api.Assertions;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.transaction.annotation.Transactional;
//
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class MemberRepositoryTest {
//    @Autowired
//    MemberRepository memberRepository;
//    @Test
//    @Transactional
//    @Rollback(value = false)
//    public void testMember() throws Exception{
//        //given
//        Member member =new Member();
//        member.setUsername("memberA");
//
//        //when
//        Long saveId = memberRepository.save(member);
//        Member findMember = memberRepository.find(saveId);
//        //then
//        Assertions.assertThat(
//                findMember.getId())
//                .isEqualTo(member.getId());
//        Assertions.assertThat(
//                findMember.getUsername())
//                .isEqualTo(member.getUsername());
//        //이렇게만 구성하면 트렌젝션이 존재하지 않아서 에러가 나오는데
//        //jpa는 트렌젝션 내부에서 동작하기 때문에 에러가 나오는 것
//        //create상태이기 때문에 테이블은 생성되지만 테스트이기 때문에
//        //롤백이 되어 데이터는 안들어간다.
//        Assertions.assertThat(findMember)
//                .isEqualTo(member);
//        //이 두 member객체는 서로 같다.
//        //왜냐하면 같은 영속성 컨텍스트에 존재하는 member정보를 가져오기
//        //때문이다.
//
//
//    }
//}