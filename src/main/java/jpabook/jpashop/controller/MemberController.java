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

}
