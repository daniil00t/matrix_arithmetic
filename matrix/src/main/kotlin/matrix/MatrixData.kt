package matrix

import kotlinx.serialization.Serializable

@Serializable
data class MatrixData(val matrix: Array<DoubleArray>, val rows: Int,val cols: Int, val longest: Int)

@Serializable
data class MDL(val matrices: List<MatrixData>)

@Serializable
data class MN(val matrix: MatrixData, val number: Double)