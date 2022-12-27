package matrixService


import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import matrix.MDL
import matrix.MN
import matrix.Matrix
import matrix.MatrixData
import mu.KLogger
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import redis.clients.jedis.Jedis
import java.time.Duration
import java.util.*


class Service {
    private val logger: KLogger = KotlinLogging.logger {}
    private var jedis: Jedis? = null
    var bootstrapServers: String? = null
    var groupId: String? = null
    var topicMulti: String? = null
    var topicNum: String? = null
    var rsTopic: String? = null
    var rqTopic: String? = null
    private fun setUpRedis() {
        var jedisHost = System.getenv("REDIS_HOST")
        if (jedisHost == null) {
            jedisHost = "192.168.1.106"
        }
        var jedisPort = System.getenv("REDIS_PORT")
        if (jedisPort == null) {
            jedisPort = "6379"
        }
        jedis = Jedis(jedisHost, jedisPort.toInt())
    }

    private fun extractEnvVars() {
        groupId = System.getenv("KAFKA_GROUP_ID")
        if (groupId == null) {
            groupId = "matrix.workers"
        }
        rqTopic = System.getenv("KAFKA_RQ_TOPIC")
        if (rqTopic == null) {
            rqTopic = "matrix.worker.rq_number"
        }
        topicMulti = System.getenv("KAFKA_MATRIX")
        if (topicMulti == null) {
            topicMulti = "matrix.worker.rq_matrix"
        }
        topicNum = System.getenv("KAFKA_NUMBER")
        if (topicNum == null) {
            topicNum = "matrix.worker.rq_number"
        }
        rsTopic = System.getenv("KAFKA_RS_TOPIC")
        if (rsTopic == null) {
            rsTopic = "matrix.worker.rs_topic"
        }
        bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS")
        if (bootstrapServers == null) {
            bootstrapServers = "192.168.1.106:29092"
        }
    }

    private fun generateConsumer(): KafkaConsumer<String, String> {
        val properties = Properties()
        properties.setProperty("bootstrap.servers", bootstrapServers)
        properties.setProperty("group.id", groupId)
        properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        properties.setProperty("auto.offset.reset", "latest")
        return KafkaConsumer(properties)
    }

    private fun generateProducer(): KafkaProducer<String, String> {
        val properties = Properties()
        properties.setProperty("bootstrap.servers", bootstrapServers)
        properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        return KafkaProducer(properties)
    }

    private fun pullMatrices(uuid: String): MDL {
        val rawData = jedis!![uuid]
        return Json.decodeFromString(rawData)
    }

    private fun pullMatrixAndNum(uuid: String): MN {
        val rawData = jedis!![uuid]
        return Json.decodeFromString(rawData)
    }

    private fun pushMatrix(uuid: String, matrix: Matrix) {
        val res = MatrixData(matrix.matrix, matrix.rows, matrix.cols, matrix.longest)
        jedis!!.del(uuid)
        jedis!![uuid] = Json.encodeToString(res)
    }

    private fun handleMatricesMultiplication(uuid: String) {
        val data = pullMatrices(uuid)
        logger.info("Extract data from redis")
        val matrixA = Matrix(data.matrices[0].matrix)
        val matrixB = Matrix(data.matrices[1].matrix)
        matrixA.longest = data.matrices[0].longest
        matrixB.longest = data.matrices[1].longest
        logger.info("Multiplying...")
        val res = matrixA multiplyBy matrixB
        logger.info("Save result")
        pushMatrix(uuid, res)
    }

    private fun handleMatrixMultiplicationByNum(uuid: String) {
        val data = pullMatrixAndNum(uuid)
        logger.info("Extract data from redis")
        val matrixA = Matrix(data.matrix.matrix)
        val num = data.number
        matrixA.longest = data.matrix.longest
        logger.info("Multiplying...")
        val res = matrixA multiplyBy num
        logger.info("Save result")
        pushMatrix(uuid, res)
    }

    fun serve() {
        extractEnvVars()
        val consumer = generateConsumer()
        consumer.subscribe(setOf(rqTopic))
        //consumer.subscribe(setOf(topicMulti, topicNum))
        val producer = generateProducer()
        logger.info("Consumer and producer are generated")
        while (true) {
            logger.info("Selecting messages from kafka...")
            val records = consumer.poll(Duration.ofMillis(2000))
            if (records.count() == 0) {
                logger.info("Nothing is received: $rqTopic")
                continue
            }
            records.iterator().forEach {
                val uuid = it.value()
                logger.info(uuid)
                when (rqTopic) {
                    "matrix.worker.rq_matrix" -> {
                        logger.info("Matrices multiplication $uuid...")
                        handleMatricesMultiplication(uuid)
                    }
                    "matrix.worker.rq_number" -> {
                        logger.info("Matrix by number multiplication $uuid...")
                        handleMatrixMultiplicationByNum(uuid)
                    }
                }
                try {
                    producer.send(ProducerRecord(rsTopic, uuid)).get()
                } catch (ex: Exception) {
                    logger.info("Can't post message to kafka")
                }
            }
        }
    }

    init {
        logger.info("Starting")
        setUpRedis()
    }
}