package com.peng.zerocodeappsandbox;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.peng.zerocodeappsandbox.mapper")
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
public class ZeroCodeAppSandboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZeroCodeAppSandboxApplication.class, args);
    }

}
