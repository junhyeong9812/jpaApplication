package jpabook.jpashop.controller;

import jpabook.jpashop.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class HomeController {



    @RequestMapping("/")
    public String home(){
        log.info("homeController");
        return "home";
    }
}
