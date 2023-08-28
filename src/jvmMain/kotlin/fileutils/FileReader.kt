package fileutils

class FileReader(path: String) {
    val pattern = """^\[(?<date>.*)]\s(?<env>\w+)\.(?<type>\w+):(?<message>.*)"""
    val splitPattern = """\[(?<date>.*)]\s(?<env>\w+)\.(?<type>\w+):"""
    val logs = mutableListOf<Log>()
    val path: String
    val filename: String
    var size: Long = 0
    private val file: java.io.File

    init {
        this.path = path
        filename = path.split("/").last()
        file = java.io.File(path)
        size = file.length()
        logs.clear()
        if (file.exists()) {
            parseFile()
        }
    }

    private fun parseFile() {
        val splitRegex = Regex(splitPattern, RegexOption.MULTILINE)
        val text = file.readText()
        val splits = splitRegex.findAll(text).toList()
        splits.forEachIndexed { index, it ->
            val date = it.groups["date"]?.value
            val env = it.groups["env"]?.value
            val type = it.groups["type"]?.value
            val start = it.groups.first()?.range?.first
            if (date != null && env != null && type != null && start != null) {
                val end =
                    if (index < splits.count() - 1) splits[index + 1].groups.first()?.range?.first?.minus(1) else text.length - 1
                val endType = it.groups.first()?.range?.last?.plus(1) ?: 0
                val message = text.slice(endType..end!!)
                logs.add(
                    0,
                    Log(
                        date = date,
                        env = env,
                        type = type,
                        message = message,
                        length = end - start,
                        start = start
                    )
                )
            }
        }


    }

    val logCount: Int = logs.size

    fun get(numberOfLogs: Int = logCount, offset: Int = 0): List<Log> {
        val to: Int = if (offset + numberOfLogs > logCount) logCount else offset + numberOfLogs
        return logs.subList(offset, to).toList()
    }

    fun hasBeenUpdated(): Boolean {
        val file = java.io.File(path)
        return file.length() != size
    }

}