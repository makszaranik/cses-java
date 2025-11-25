package com.example.demo.config;

import com.example.demo.model.user.UserEntity;
import com.example.demo.service.executor.stage.StageExecutor;
import com.example.demo.service.user.UserService;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.messaging.DefaultMessageListenerContainer;
import org.springframework.data.mongodb.core.messaging.MessageListenerContainer;

import java.util.List;
import java.util.Optional;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

    @Bean
    AuditorAware<String> auditorAware(UserService userService) {
        return () -> Optional.ofNullable(userService.getCurrentUser()).map(UserEntity::getId);
    }

    @Bean
    MessageListenerContainer messageListenerContainer(MongoTemplate mongoTemplate){
         return new DefaultMessageListenerContainer(mongoTemplate);
    }

}
