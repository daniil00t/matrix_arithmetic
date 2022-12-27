import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import mu.KotlinLogging

val logger = KotlinLogging.logger { }

suspend fun main() {
    val logger = KotlinLogging.logger {}
    var input: Int
    while(true) {
        do {
            logger.info("What do you want to do?\n\t1: Multiply 2 matrices\n\t2: Multiply a matrix by a number\n Action: ")
            try {
                input = readLine()!!.toInt()
            } catch (e: Exception) {
                logger.error("Input should be an integer")
                continue
            }
            if (input != 1 && input != 2) {
                logger.error("Incorrect input. It should be 1 or 2.")
            } else
                break
        } while (true)
        val client = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 300000
            }
        }
        logger.info("We are here")
        if (input == 1)
            Requests.matricesMultiplication(client)
        else
            Requests.matrixByNumMultiplication(client)
    }
}