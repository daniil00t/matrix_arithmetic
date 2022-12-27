import kotlinx.serialization.Serializable

@Serializable
data class MatrixData(val matrix: Array<DoubleArray>, val rows: Int,val cols: Int, val longest: Int){
    override fun toString(): String {
        var string = ""
        for (i in 0 until rows) {
            string += "["
            for (j in 0 until cols) {
                string += String.format("%${this.longest+6}.2f",this.matrix[i][j])
            }
            string += "  ]\n"
        }
        return string
    }
}

@Serializable
data class MDL(val matrices: List<MatrixData>)

@Serializable
data class MN(val matrix: MatrixData, val number: Double)
