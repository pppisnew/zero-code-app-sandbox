package com.peng.zerocodeappsandbox;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.peng.zerocodeappsandbox.mapper")
public class ZeroCodeAppSandboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZeroCodeAppSandboxApplication.class, args);
    }

}
