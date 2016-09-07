/*
 * Copyright 2016 TomeOkin
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app;

import app.config.LsPushProperties;
import app.data.crypt.Crypto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Autowired LsPushProperties lsPushProperties;
    @Autowired RedisConnectionFactory factory;

    @PostConstruct
    public void initCrypt() {
        try {
            Crypto.init(lsPushProperties.getPublicKey(), lsPushProperties.getPrivateKey());
        } catch (Exception e) {
            logger.error("create Crypto instance failure", e);
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args);
    }

    // 目前处于测试阶段，启动服务器时先清空数据库
    @PostConstruct public void flushDb() {
        factory.getConnection().flushDb();
    }
}
