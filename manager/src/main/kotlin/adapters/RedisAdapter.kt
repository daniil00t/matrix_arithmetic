package adapters

class RedisAdapter {
    var host: String?
    private var port: String?
    fun getPort(): Int {
        return port!!.toInt()
    }

    init {
        host = System.getenv("REDIS_HOST")
        if (host == null) {
            host = "192.168.1.106"
        }
        port = System.getenv("REDIS_PORT")
        if (port == null) {
            port = "6379"
        }
    }
}
