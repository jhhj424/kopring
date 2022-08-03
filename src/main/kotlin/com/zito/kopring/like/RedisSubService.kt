package com.zito.kopring.like

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zito.kopring.logger
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class RedisSubService(
    private val redisTemplate: RedisTemplate<Any, Any>
) : MessageListener {

    private val log by logger()

    override fun onMessage(message: Message, pattern: ByteArray?) {
        try {
            val key = jacksonObjectMapper().readValue(message.body, LikeMessageDto::class.java).key
            log.info("Redis msg sub, key: {}", key)
            val likeCount = redisTemplate.opsForSet().size(key)
            log.info("Key: {}, likeCount: {}", key, likeCount)
        } catch (e: IOException) {
            log.error("Redis sub error, error: {}", e.printStackTrace())
        }
    }
}
