package com.jsonyao.cs.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@Controller
@RestController
@RequestMapping("/testController")
public class TestController {

    @RequestMapping("/testRequestMapping")
    public void testRequestMapping(){
        System.out.println("Test RequestMapping~~~");
    }

    @RequestMapping("/testRestController")
    public String testRestController(){
        System.out.println("Test RestController~~~");
        return "Test RestController~~~";
    }

}
