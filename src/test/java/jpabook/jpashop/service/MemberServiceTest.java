//package jpabook.jpashop.service;
//
//import jakarta.persistence.EntityManager;
//import jpabook.jpashop.domain.Member;
//import jpabook.jpashop.repository.MemberRepository;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.junit.jupiter.api.Assertions.*;
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@Transactional
//public class MemberServiceTest {
//    @Autowired
//    MemberService memberService;
//    @Autowired
//    MemberRepository memberRepository;
//
//    @Autowired
//    EntityManager em;
//    //em.flush를 통하면 트렌젝션에서 강제로 flush를 통해 인서트 쿼리를 날릴 수 있다.
//
//    @Test
//    @Rollback(value = false)
//    public void 회원가입() throws Exception{
//        //given
//        Member member = new Member();
//        member.setName("kim");
//
//        //when
//        Long saveId = memberService.join(member);
//        //테스트에서는 @Transactional가 롤백을 하기 떄문에 별도의 인서트 문이 안나간다.
////        2024-09-04T16:24:43.824+09:00  INFO 7344 --- [jpashop]
////        [    Test worker] p6spy                                    :
////        #1725434683824 | took 0ms | rollback | connection 3|
////        url jdbc:h2:tcp://localhost/~/jpashop
//
//        //then
////        em.flush();
//        assertEquals(member,memberRepository.findById(saveId));
//    }
//    @Test(expected = IllegalStateException.class)
//    //IllegalStateException를 통해 에러를 잡으면 테스트를 통과하도록
//    //try/catch를 생략 할 수 있다.
//    public void 중복_회원_예외() throws Exception{
//        //given
//        Member member1=new Member();
//        member1.setName("kim1");
//        Member member2=new Member();
//        member2.setName("kim1");
//
//        //when
//        memberService.join(member1);
////        try {
//            memberService.join(member2);
////        }catch (IllegalStateException e){
////            return;
////        }//이렇게 catch에 잘 잡히는 것을 볼 수 있다.
//        //then
//        fail("예외가 발생해야 한다.");
//    }
//
//}