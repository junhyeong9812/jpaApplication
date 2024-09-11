package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)//스프링 트랜젝션을 선호
@RequiredArgsConstructor//final만 있는 애들로 생성자를 만들어준다.
public class MemberService {
//    @Autowired
    private final MemberRepository memberRepository;
    //필드 인젝션
    //>>테스트를 할 때 변경할 수 없다.
    //그래서 보통은 setter인젝션을 사용한다.
//    @Autowired
//    public void setMemberRepository(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }//테스트 코드를 직접 주입할 수 있다.

    //가장 좋은 건 이와 같은 생성자 인젝션이다.
//    @Autowired
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }//테스트 케이스에서도 필요한 주입을 해줄 수 있다.
    //그래서 그냥 @Autowired가 없어도 생성자로 있으면 해준다. 그래서 필드를
//    final 로 하는걸 권장

    //회원가입
    @Transactional(readOnly = false)
    public Long join(Member member){
        validateDuplicateMember(member);//중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    //중복 회원 로직
    private void validateDuplicateMember(Member member){
        //
        List<Member> findMembers = memberRepository.findByName(member.getName());
        //db에 동시에 호출하면 문제가 생길 수 있는데 이때 멀티 스레드를고려해야 된다.
        //그래서 Name은 unique제약 조건이 있는 게 좋다.
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    //회원 전체 조회
//    @Transactional(readOnly = true)
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    //회원 단건 조회
    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);
        //더치체크로 자동 저장이 될 것
    }
}
