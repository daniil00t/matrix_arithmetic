package webServer

import adapters.Adapters
import adapters.RedisAdapter
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.json.Json
import redis.clients.jedis.Jedis
import java.util.*
import java.util.concurrent.CountDownLatch

@DelicateCoroutinesApi
fun main() {
    val adapter = Adapters()
    adapter.logger.info("Logger, kafka and redis init, starting server...")
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting(adapter)
    }.start(wait = true)
}


@DelicateCoroutinesApi
private fun Application.configureRouting(adapter: Adapters) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    routing {
        post("/matrices") {
            handleRequest(adapter, adapter.kafka.topicMulti!!, call)
        }
        post("/matrix_by_num") {
            handleRequest(adapter, adapter.kafka.topicNum!!, call)
        }
    }
}

private fun pushData(rAdapter: RedisAdapter, uuid: String, data: String) {
    val jedis = Jedis(rAdapter.host, rAdapter.getPort())
    jedis.set(uuid, data)
}

private fun pullData(rAdapter: RedisAdapter, uuid: String): String {
    val jedis = Jedis(rAdapter.host, rAdapter.getPort())
    val res: String = jedis.get(uuid)
    println(res)
    jedis.del(uuid)
    return res
}

@DelicateCoroutinesApi
suspend fun handleRequest(adapter: Adapters, topic: String, call: ApplicationCall) {
    val uuid = UUID.randomUUID().toString()
    adapter.logger.info("Generating uuid: $uuid")
    pushData(adapter.redis, uuid, call.receiveText())
    adapter.logger.info("Pushing data to redis")
    val cdl = CountDownLatch(1)
    adapter.kafka.putMessage(uuid, topic, cdl)
    adapter.logger.info("Putting message into kafka, waiting result...")
    cdl.await()
    adapter.logger.info("Sending response...")
    call.response.status(HttpStatusCode.fromValue(201))
    call.respond(pullData(adapter.redis, uuid))
}

/*
*     val jobsToAwait = mutableListOf<Job>()
    try {
        lateinit var result: String
        jobsToAwait += GlobalScope.launch(Dispatchers.IO) {
            KafkaAdapter().consume(uuid).onEach {
                log.info("Receiving result")
                result = it
            }
        }
    } catch (e: InterruptedException) {
        adapter.logger.warn(e.message)
    }

    jobsToAwait.forEach { it.join() }
* */