package com.isacc.event.spring;

import javax.annotation.Resource;

import com.isacc.event.spring.service.SomeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * description
 *
 * @author isacc 2019/07/30 1:22
 * @since 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestSomeService {

    @Resource
    private SomeService someService;

    @Test
    public void doSomething() {
        someService.doSomething("i love u");
    }
}
