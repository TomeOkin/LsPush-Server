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
import app.data.local.FavorRepository;
import app.data.model.Favor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
//@EnableCaching
//@EnableScheduling
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

//    @Bean
//    RedisObjectSerializer redisObjectSerializer() {
//        return new RedisObjectSerializer();
//    }
//
//    @Bean
//    MessageListenerAdapter listenerAdapter(CollectionReceiver receiver, RedisObjectSerializer serializer) {
//        MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, "receiveMessage");
//        adapter.setSerializer(serializer);
//        return adapter;
//    }
//
//    @Bean
//    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
//        MessageListenerAdapter listenerAdapter) {
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.addMessageListener(listenerAdapter, new PatternTopic("collection"));
//        return container;
//    }
//
//    @Bean
//    RedisTemplate<String, Collection> template(RedisConnectionFactory connectionFactory,
//        RedisObjectSerializer serializer) {
//        RedisTemplate<String, Collection> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(serializer);
//        return template;
//    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args);
    }

    @Autowired FavorRepository favorRepository;
    @PostConstruct
    public void testFavor() {
        favorRepository.dropAll();

        Favor.Data data = new Favor.Data();
        data.date = DateTime.now().toDate();
        data.uid = "101";
        favorRepository.addFavor(5, data);
        favorRepository.addFavor(5, data);
        data = new Favor.Data();
        data.date = DateTime.now().toDate();
        data.uid = "102";
        favorRepository.addFavor(5, data);

        favorRepository.removeFavor(5, "101");
    }

    // 初始化配置
    @Autowired LsPushProperties lsPushProperties;

    @PostConstruct
    public void initCrypt() {
        try {
            Crypto.init(lsPushProperties.getPublicKey(), lsPushProperties.getPrivateKey());
        } catch (Exception e) {
            logger.error("create Crypto instance failure", e);
        }
    }

//    // 目前处于测试阶段，启动服务器时先清空数据库
//    @Autowired RedisConnectionFactory factory;
//
//    @PostConstruct
//    public void flushDb() {
//        factory.getConnection().flushDb();
//    }
}
