package com.zito.kopring.config

import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping
import com.zito.kopring.like.RedisSubService
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer


@Configuration
class RedisConfig(
    val redisProperties: RedisProperties,
    val objectMapper: ObjectMapper
) {

    @Primary
    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<Any, Any> {
        val copyObjectMapper: ObjectMapper = objectMapper.copy()
        copyObjectMapper.activateDefaultTyping(
            copyObjectMapper.polymorphicTypeValidator,
            DefaultTyping.NON_FINAL,
            As.PROPERTY
        )
        val genericJackson2JsonRedisSerializer = GenericJackson2JsonRedisSerializer(copyObjectMapper)
        val redisTemplate = RedisTemplate<Any, Any>()
        redisTemplate.stringSerializer = StringRedisSerializer()
        redisTemplate.hashKeySerializer = StringRedisSerializer()
        redisTemplate.hashValueSerializer = genericJackson2JsonRedisSerializer
        redisTemplate.valueSerializer = genericJackson2JsonRedisSerializer
        redisTemplate.setConnectionFactory(redisConnectionFactory)
        return redisTemplate
    }

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val configuration = RedisStandaloneConfiguration()
        configuration.hostName = redisProperties.host
        configuration.port = redisProperties.port
        return LettuceConnectionFactory(configuration)
    }

    @Bean
    fun messageListenerAdapter(): MessageListenerAdapter {
        return MessageListenerAdapter(RedisSubService(redisTemplate(redisConnectionFactory())))
    }

    @Bean
    fun redisContainer(): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(redisConnectionFactory())
        container.addMessageListener(messageListenerAdapter(), topic())
        return container
    }

    @Bean
    fun topic(): ChannelTopic {
        return ChannelTopic("LikeTopic")
    }
}
