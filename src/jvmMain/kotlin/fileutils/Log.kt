package fileutils

data class Log(
    val date: String,
    val env: String,
    val type: String,
    val message: String,
    var start: Int? = null,
    var length: Int? = null
)