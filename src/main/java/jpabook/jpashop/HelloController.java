package jpabook.jpashop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {
    @GetMapping("hello")
    public String hello(Model model){
        model.addAttribute("data","hello!");
        //model이라는 곳에 데이터를 실어서 view로 넘길 수 있다.
        //data: hello! 라는 형식으로 데이터를 넘기고
        //return을 통해 templates의 .html파일을 띄워준다.
        return "hello";
        //지금의 경우 templates의 hello.html파일을 열어주는 것
    }
}
