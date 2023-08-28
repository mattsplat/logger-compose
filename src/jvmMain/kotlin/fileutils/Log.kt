package fileutils

data class Log(
    val date: String,
    val env: String,
    val type: String,
    val message: String,
    var start: Int? = null,
    val length: Int? = null
)