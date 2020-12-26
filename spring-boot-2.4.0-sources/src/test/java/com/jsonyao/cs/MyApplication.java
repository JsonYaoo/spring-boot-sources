package com.jsonyao.cs;

import com.jsonyao.cs.Controller.TestDispatcherServletController;
import com.jsonyao.cs.Controller.TestRequestMappingController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动测试类
 */
@SpringBootApplication
public class MyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }

    /**
     * class-04-spring-boot工程测试内容
     */
    private void testClass04SpringBoot(){
        /**
         * 1、@RequestMapping测试
         */
        Class<?> testRequestMappingControllerClazz = TestRequestMappingController.class;

        /**
         * 2、DispatherSerlvet调用链路测试
         */
        Class<?> testControllerClazz = TestDispatcherServletController.class;
    }
}
