package customThreadPool

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean


class CustomThreadPool(nThreads: Int) {
    private inner class WorkerThread(name: String, queue: BlockingQueue<Runnable>) : Thread(name) {
        private val workerQueue: BlockingQueue<Runnable>
        private val isAlive: AtomicBoolean
        fun shutdown() {
            isAlive.set(false)
            this.interrupt()
        }

        override fun run() {
            while (isAlive.get()) {
                val r: Runnable? = try {
                    workerQueue.take()
                } catch (e: InterruptedException) {
                    continue
                }
                r?.run()
            }
        }

        init {
            workerQueue = queue
            isAlive = AtomicBoolean(true)
        }
    }

    private val workerQueue: BlockingQueue<Runnable>
    private val threads: ArrayList<WorkerThread>

    @Throws(InterruptedException::class)
    fun execute(r: Runnable?) {
        workerQueue.put(r!!)
    }

    fun shutdown() {
        for (t in threads) {
            t.shutdown()
        }
    }

    init {
        workerQueue = LinkedBlockingQueue()
        threads = ArrayList()
        for (i in 0 until nThreads) {
            val t = WorkerThread("Worker_$i", workerQueue)
            t.start()
            threads.add(t)
        }
    }
}