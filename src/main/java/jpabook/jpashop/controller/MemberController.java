package jpabook.jpashop.controller;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping(value = "/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }
    @PostMapping(value = "/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {
//        @Valid를 사용하면 NotEmpty를 사용할 수 있다.
        //스프링에서 벨리데이션을 편하게 사용할 수 있다.
        //BindingResult result 란 오류에 걸리면 팅기는데
        //BindingResult result 안에 오류를 담아서
        //그 에러가 존재하면 아래처럼 다시 페이지를 넘길 수 있다.
        if (result.hasErrors()) {
            return "members/createMemberForm";
        }
        //그러면 에러 코드를 createMemberForm에 뿌려줄 수 있게 된다.
        //항상 데이터를 폼 형식으로 받아서 그걸 컨트롤러나 서비스에서 정제해서 데이터를
        //넘기는 게 좋다.
        Address address = new Address(form.getCity(), form.getStreet(),
                form.getZipcode());
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);
        memberService.join(member);
        return "redirect:/";
        //재로딩을 방지하기 위한 리다이렉트
    }
    //추가
    @GetMapping(value = "/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }
    //요구사항이 단순하지 않을 경우 폼이나 DTO를 사용해서 데이터를 받는 게 좋다.
    //엔티티는 최대한 순수하게 설계해야 된다.
    //엔티티는 최대한 순수한 상태를 유지하는 게 좋다.
    //api를 설계할 때는 외부로 API를 반환하면 안된다.
    //api의 스팩이 엔티티의 변경에 따라 계속 변경되면 안되기 때문에
    //dto를 사용해서 항상 데이터를 가려야된다.

}
