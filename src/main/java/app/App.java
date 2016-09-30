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
import app.config.RedisObjectSerializer;
import app.data.crypt.Crypto;
import app.data.model.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;

@SpringBootApplication
@EnableJms
//@EnableCaching
//@EnableScheduling
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Bean
    RedisObjectSerializer redisObjectSerializer() {
        return new RedisObjectSerializer();
    }

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

    @Bean
    RedisTemplate<String, Collection> template(RedisConnectionFactory connectionFactory,
        RedisObjectSerializer serializer) {
        RedisTemplate<String, Collection> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        return template;
    }

    @Bean
    public JmsListenerContainerFactory<?> defaultFactory(ConnectionFactory connectionFactory,
        DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        return factory;
    }

    // Serialize message content to json using TextMessage
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args);
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
//    @Autowired FavorRepository favorRepo;
//    @Autowired CollectionTagRepository colTagRepo;
//
//    @PostConstruct
//    public void flushDb() {
//        factory.getConnection().flushDb();
//        favorRepo.dropAll();
//        colTagRepo.dropAll();
//    }
}
