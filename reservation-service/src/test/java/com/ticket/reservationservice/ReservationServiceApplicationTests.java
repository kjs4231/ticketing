package com.ticket.reservationservice;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootTest
class ReservationServiceApplicationTests {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RedissonClient redissonClient() {
            Config config = new Config();
            config.useSingleServer()
                    .setAddress("redis://localhost:6379");
            return Redisson.create(config);
        }
    }

    @Test
    void contextLoads() {
    }

}
