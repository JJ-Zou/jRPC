package com.zjj;

import com.zjj.config.spring.annotation.EnableJRpc;
import com.zjj.config.spring.initializer.JRpcApplicationContextInitializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableJRpc
public class JRpcApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(JRpcApplication.class);
        springApplication.addInitializers(new JRpcApplicationContextInitializer());
        springApplication.run(args);
    }


    @Override
    public void run(String... args) throws Exception {
    }
}
