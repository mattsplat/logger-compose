package fileutils

class FileReader(path: String) {
    val pattern =  """^\[(?<date>.*)\]\s(?<env>\w+)\.(?<type>\w+):(?<message>.*)"""
    val logs = mutableListOf<Log>()
    val path: String
    val filename: String
    var size: Long = 0

    init {
        this.path = path
        filename = path.split("/").last()
        val file = java.io.File(path)
        size = file.length()
//        val buf = CharArray(1024)
//        var n: Int
//        while (file.reader().read(buf).also { n = it } != -1) {
//
////            val string = buf.map { char -> char.toChar() }.joinToString("")
//
//        }
        val regex = Regex(pattern, RegexOption.MULTILINE)
        regex.findAll(file.reader().readText()).map {
            val date = it.groups["date"]?.value
            val env = it.groups["env"]?.value
            val type = it.groups["type"]?.value
            val message = it.groups["message"]?.value
            if (date != null && env != null && type != null && message != null) {
                val length = it.groups[0].toString().length
                logs.add(Log(date = date, env = env, type = type, message = message, length = length))
            }
            it.value
        }
            .toList().reversed()
        file.reader().close()

    }

    val logCount: Int = logs.size

    fun get(numberOfLogs: Int = logCount, offset: Int = 0): List<Log> {
        val to: Int = if (offset + numberOfLogs > logCount) logCount else offset + numberOfLogs
        return logs.subList(offset, to).toList()
    }

}