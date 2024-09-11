package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    //-----수정-----//
    //수정은 /api/v2/members/{id}로 put형식으로
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,
                                               @RequestBody @Valid UpdateMemberRequest request){
        memberService.update(id,request.getName());
        //서비스에서 맴버 자체를 반환해도 되는데
        //그렇게 하게 되면 Member를 반환하면 준영속 상태로 넘어오게 된다.
        //결국 업데이트를 하고 맴버에 대해 쿼리를 날리게 된다. 그래서 업데이트 기능과 셀렉트까지 되어 버리는 것
        Member findMember = memberService.findOne(id);
        //그래서 이렇게 위에서 업데이트를 끝내고 별도로 다시 한번 조회해서 그 데이터를 넘겨준다.
        //이렇게 커멘드와 쿼리는 분리하는 스타일로 하면 유지보수 측면에서 상당히 메리트가 존재한다.
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());

    }
    //응답과 요청에 대한 DTO를 별도로 만드는데
    //이때 API스팩이 보통 다르기 때문에 별도로 구축하는 게 맞다.
    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long id;
        private String name;
    }
    //엔티티에서는 getter만 사용
    //DTO에서는 Lombok은 더 넓게 써도 된다.

    @Data
    static class UpdateMemberRequest{
        private String name;
    }

    //-----회원 조회-----//
    // api/v1/members >get방식
    //단순조회라 테이블을 변경할 일이 없기 때문에 ddl-auto:none으로 변경
    //db초기화가 되지 않도록 none세팅
    //조회할 때 데이터를 자꾸 넣어야 되는게 번거로우니 create가 아닌 none으로 변경
    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        return memberService.findMembers();
    }
    //api가 단순하지만 지금 문제는 order정보도 같이 가져오는 것을 볼 수 있다.
    //Member객체 자체를 직접 노출하게 되면 엔티티 정보가 외부로 전부 노출이 된다.
    //JsonIgnore를 맴버 엔티티의 Orders에 걸어놓으면 엔티티 요청에서 제외할 수 있다.
    //하지만 다른 API를 만들 때 Order정보를 가져오지 못하면? 
    //그래서 엔티티에서는 JsonIgnore를 안쓰는 게 좋고 이렇게 하면 결국 API스팩을 위한 설정이 작성된 것
    //결국 API스팩이 변경되는 것
    //그래서 협업할 때는 엔티티를 직접 반환하는 게 아닌 지정된 스팩을 고정 시키기 위해서 DTO형식으로
    //반환해야 된다.
    @GetMapping("/api/v2/members")
    public Result membersV2(){
        List<Member> findMembers = memberService.findMembers();
        List<MemberDTO> collect = findMembers.stream().map(m -> new MemberDTO(m.getName()))
                .collect(Collectors.toList());
        //배열을 바로 내보내면 json배열 타입으로 나가기 때문에 아래의 Result처럼 data로 한번 묶어서 내보내야
        //한다.
        return new Result(collect.size(),collect);

    }
    @Data
    @AllArgsConstructor
    static class Result<T>{
        //만약 총 list의 갯수가 필요하다면 이처럼 데이터 타입을 넣으면 된다.
        //그러면 오브젝트 타입에 데이터를 추가할 수 있따.
        private int count;
        private T data;
    }
//    T는 제네릭(Generic) 타입을 의미합니다.
//    이는 자바에서 클래스나 메서드가 여러 타입의 데이터를 처리할 수 있도록 해주는 기능입니다.
//    즉, 특정 타입에 고정되지 않고, 다양한 타입을 받을 수 있게 해줍니다.
//
//    위 코드에서 T는 Result 클래스를 생성할 때 실제 타입으로 대체됩니다.
//    예를 들어, Result<List<MemberDTO>>로 사용된다면 T는 List<MemberDTO>로 대체됩니다. 이는 Result 클래스가 다양한 타입의 데이터를 가질 수 있도록 유연성을 제공하기 위한 방법입니다.
//
//    따라서 T는 그저 타입을 나타내는 별명일 뿐이고,
//    실제로 Result 객체를 생성할 때에야 비로소 그 타입이 구체적으로 결정됩니다.
    @Data
    @AllArgsConstructor
    static class MemberDTO{

        private String name;
        //API 스팩에서는 DTO와 1대1 관계가 되어야 한다.
        //정말 필요한 것만 호출해서 반환해줘야 한다.
    //유지보수 입장에서도 DTO와 API스팩과 일치해야 유지보수가 편리하다.
    }
    //API는 항상 스팩이 일정할 수 있도록 DTO로 만들어서 반환해라
}
