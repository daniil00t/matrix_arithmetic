package matrix

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.provider.ValueSource
import kotlin.random.Random

internal class MatrixTest {
    @ParameterizedTest
    @ValueSource(ints = [250, 500, 750,1000])
    fun multiplyByMatrix(size: Int) {
        val matrixA = Matrix(size,size)
        val matrixB = Matrix(size,size)
        val res1 = matrixA*matrixB
        val res = matrixA multiplyBy matrixB
        for (i in res.matrix.indices)
            for (j in res.matrix[0].indices)
                assertEquals(res[i,j], res1[i,j])
    }

    @ParameterizedTest
    @ValueSource(ints = [1000,2000,3000])
    fun multiplyByNumber(size: Int) {
        val matrixA = Matrix(size,size)
        val num = Random.nextDouble(2.0,100.0)
        val res2 = matrixA*num
        val res = matrixA multiplyBy num
        for (i in res.matrix.indices)
            for (j in res.matrix[0].indices)
                assertEquals(res[i,j], res2[i,j])
    }
}