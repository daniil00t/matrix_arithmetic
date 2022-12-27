import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.properties.Delegates
import kotlin.system.exitProcess

object Requests {
    suspend fun matricesMultiplication(client: HttpClient) {
        val matrixA = getMatrix()
        val matrixB = getMatrix()
        if (matrixA.cols != matrixB.rows) {
            logger.error("Columns quantity of 1st matrix should be equal to rows quantity of 2nd matrix")
            client.close()
            exitProcess(1)
        }
        lateinit var recieved: String
        val time = measureTimeMillis {
            val response: HttpResponse = client.post("http://0.0.0.0:8080/matrices") {
                contentType(ContentType.Application.Json)
                body = MDL(listOf(matrixA, matrixB))
            }
            recieved = response.receive()
        }
        val res = Json.decodeFromString<MatrixData>(recieved)
        println(res)
        logger.info("Time elapsed: $time ms")
        client.close()
    }

    suspend fun matrixByNumMultiplication(client: HttpClient) {
        val matrix = getMatrix()
        var num by Delegates.notNull<Double>()
        do {
            print("Enter a number to multiply matrix by: ")
            try {
                num = readLine()!!.toDouble()
                break
            } catch (e: Exception) {
                logger.error("Illegal argument")
                continue
            }
        } while (true)
        lateinit var recieved: String
        val time = measureTimeMillis {
            logger.info("create http_request")
            val response: HttpResponse = client.post("http://0.0.0.0:8080/matrix_by_num") {
                contentType(ContentType.Application.Json)
                body = MN(matrix, num)
            }
            logger.info("recieve http_request")
            recieved = response.receive()
        }
        val res = Json.decodeFromString<MatrixData>(recieved)
        println(res)
        logger.info("Time elapsed: $time ms")
        client.close()
    }

    private fun getMatrix(): MatrixData {
        var rows: Int
        var cols: Int
        do {
            print("Specify rows and columns: ")
            try {
                readLine()!!.split(' ').also {
                    rows = it[0].toInt()
                    cols = it[1].toInt()
                }
                break
            } catch (e: Exception) {
                logger.error("Input should be a number")
                continue
            }
        } while (true)
        var longest = 1
        println("Enter matrix row by row")
        lateinit var matrix: Array<DoubleArray>
        do {
            try {
                matrix = Array(rows) {
                    val input = readLine()!!.split(' ').map {
                        if (longest < it.length) {
                            longest = it.length
                        }
                        it.toDouble()
                    }
                    if (input.size != cols) {
                        throw IllegalArgumentException()
                    }
                    input.toDoubleArray()
                }
                break
            } catch (e: NumberFormatException){
                logger.error("Input should be a number")
                println("Enter matrix row by row again")
                continue
            } catch (e: IllegalArgumentException){
                logger.error("Not enough numbers for this row")
                println("Enter matrix row by row again")
                continue
            }
        } while (true)
        println("ok")
        return MatrixData(matrix, rows, cols, longest)
    }

    private inline fun measureTimeMillis(block: () -> Unit): Long {
        val start = System.currentTimeMillis()
        block()
        return System.currentTimeMillis() - start
    }
}