package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//@Controller @ResponseBody
@RestController//@Controller @ResponseBody두개를 합친 것
//json타입으로 변환해서 보내는 것
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
//        @Valid >>자동 벨리데이션이 가능하다.
        //json으로 온 body를 매핑해준다.
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
        //이때 단순한 등록 로직인데 Member등록 엔티티에
        //notEmpty를 넣었는데 화면에서 들어오면 데이터에 대해서
        //controller계층을 위한 검증이 엔티티에 들어있으면 안된다.
        //이렇게 구성되면 엔티티의 스팩을 변경했는데 이때
        //API스팩 자체가 변경된다.
        //그러면 프론트에서 개발할 때 API를 호출했는데 갑자기 API에 엔티티가
        //변경된 부분떄문에 json데이터 타입이 일치하지 않게되어
        //API가 동작하지 않게 된다.
        //엔티티는 많은 곳에서 사용하여 변경될 활률이 높은데
        //그래서 api의 스팩 자체가 매핑되면 안된다.
        //api스팩을 위한 별도의 DTO를 만드는게 중요하다.
        //엔티티를 파라미터로 받지말고 외부에 노출도 하지마!
    }
    //DTO를 활용한 V2버전
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);
        //엔티티 변환 시점에 컴파일 오류가 나기 때문에 API는 이 스팩에 대해
        //영향을 받지 않고 백엔드 컴파일로 바로 처리할 수 있다.
        return new CreateMemberResponse(id);
    }
    //request와 reponse가 별도의 격체를 만들게 된다.
    //개발자 입장에서는 엔티티만 보고 확인하는 게 쉽지않다.
    //API스팩을 DTO로 정리할 수 있어서 훨씬 객관적이고 편리하다.

    @Data
    static class CreateMemberRequest{
        @NotEmpty
        private String name;
    }//엔티티 변경 시 사이드 임팩트의 범위를 특정지을 수 없기 때문에
    //v2방식이 더 나은 방식이다.
    //API의 응답과 요청은 전부 DTO를 통해 응답 및 요청을 받아야 된다.
    @Data
    static class CreateMemberResponse{
        private Long id;
        public CreateMemberResponse(Long id){
            this.id=id;
        }
    }
}
