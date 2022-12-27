package me.zukoap.performance.benchmarks

import matrix.Matrix
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@State(Scope.Thread)
open class Matrices {
    @Param("3000")
    var matSize = ""
    @Param("5","6","7","8")
    var nThreads = ""
}

@Timeout(time = 120)
@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@Fork(3)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class MatrixMultiplicationBenchmark : Matrices() {
    lateinit var  matrixA: Matrix
    lateinit var matrixB: Matrix
    var num = 0.0

    @Setup(Level.Iteration) fun setUp() {
        matrixA = Matrix(matSize.toInt(), matSize.toInt())
        matrixA.nThreads = 8
        matrixB = Matrix(matSize.toInt(), matSize.toInt())
        num = Random.nextDouble(2.0,100.0)
    }

    @Benchmark
    fun naive(blackhole: Blackhole){
        val res = matrixA*matrixB
        blackhole.consume(res)
    }
    @Benchmark
    fun bestThread(blackhole: Blackhole) {
        val res = matrixA multiplyBy matrixB
        blackhole.consume(res)
    }

    @Benchmark
    fun bestThreadNum(blackhole: Blackhole) {
        val res = matrixA multiplyBy num
        blackhole.consume(res)
    }
    @Benchmark
    fun oneThreadNum(blackhole: Blackhole) {
        val res = matrixA*num
        blackhole.consume(res)
    }

}

