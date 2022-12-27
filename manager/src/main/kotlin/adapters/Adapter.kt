package adapters

import mu.KLogger
import mu.KotlinLogging


class Adapters {
    var kafka: KafkaAdapter = KafkaAdapter()
    var logger: KLogger
    var redis: RedisAdapter

    init {
        kafka.init()
        logger = KotlinLogging.logger {}
        redis = RedisAdapter()
    }
}