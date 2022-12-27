package adapters

import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch


class KafkaAdapter {
    private var result: ConcurrentHashMap<String, CountDownLatch> = ConcurrentHashMap()
    private var producer: KafkaProducer<String, String>? = null
    private var consumer: KafkaConsumer<String, String>? = null
    private var bootstrapServers: String? = null
    private var rsTopic: String? = null
    var topicMulti: String? = null
    var topicNum: String? = null
    private var groupId: String? = null
    private var consumingThread: Thread? = null

    private fun extractEnvVars() {
        groupId = System.getenv("KAFKA_GROUP_ID")
        if (groupId == null) {
            groupId = "matrix.workers"
        }
        rsTopic = System.getenv("KAFKA_RS_TOPIC")
        if (rsTopic == null) {
            rsTopic = "matrix.worker.rs_topic"
        }
        bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS")
        if (bootstrapServers == null) {
            bootstrapServers = "192.168.1.106:29092"
        }
        topicMulti = System.getenv("KAFKA_MATRIX")
        if (topicMulti == null) {
            topicMulti = "matrix.worker.rq_matrix"
        }
        topicNum = System.getenv("KAFKA_NUMBER")
        if (topicNum == null) {
            topicNum = "matrix.worker.rq_number"
        }
    }

    private fun createProducer(): KafkaProducer<String, String> {
        val properties = Properties()
        properties.setProperty("bootstrap.servers", bootstrapServers)
        properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        return KafkaProducer(properties)
    }

    private fun createConsumer(): KafkaConsumer<String, String> {
        val properties = Properties()
        properties.setProperty("bootstrap.servers", bootstrapServers)
        properties.setProperty("group.id", groupId)
        properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        properties.setProperty("auto.offset.reset", "latest")
        return KafkaConsumer(properties)
    }

    fun putMessage(uuid: String, rqTopic: String?, cdl: CountDownLatch) {
        result.putIfAbsent(uuid, cdl)
        try {
            producer?.send(ProducerRecord(rqTopic, uuid))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun init() {
        extractEnvVars()
        consumer = createConsumer()
        consumer!!.subscribe(setOf(rsTopic))
        producer = createProducer()
        consumingThread = Thread {
                while (true) {
                    val records: ConsumerRecords<String, String> =
                        consumer!!.poll(Duration.ofMillis(1000))
                    if (records.count() == 0) {
                        continue
                    }
                    records.iterator().forEach {
                        val uuid: String = it.value()
                        result[uuid]!!.countDown()
                        result.remove(uuid)
                    }
                }
        }
        consumingThread!!.start()
    }

}

