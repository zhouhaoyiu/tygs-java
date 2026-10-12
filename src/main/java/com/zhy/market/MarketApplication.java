package com.zhy.market;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;

/**
 * @author Administrator
 */
@MapperScan("com.zhy.market.mapper")
@SpringBootApplication()
public class MarketApplication {
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(
                connector -> connector.setProperty("relaxedQueryChars", "|{}[]\\"));
        return factory;
    }

    public static void main(String[] args) {
        SpringApplication.run(MarketApplication.class, args);
    }
}
