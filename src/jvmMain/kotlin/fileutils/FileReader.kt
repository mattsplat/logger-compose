package fileutils

import java.io.FileInputStream
import java.io.RandomAccessFile

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
            indexLogs()
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

    fun indexLogs() {
        val file = java.io.File(path)
        val inputStream = FileInputStream(file)
        val buffer = ByteArray(1024)
        var bytesRead: Int
        var carryOverText = ""
        var offset = 0
        do {
            buffer.fill(0)
            var bufferString: String
            bytesRead = inputStream.read(buffer)
            if (bytesRead != -1) {
                val splitRegex = Regex(this.splitPattern, RegexOption.MULTILINE)
                val carryOverTextLength = carryOverText.toByteArray().size
                // refactor
                bufferString = carryOverText + String(buffer.asList().toList().slice(0 until bytesRead).toByteArray())
                val splits = splitRegex.findAll(bufferString)
                var lastEnd = 0
                splits.forEach {
                    val date = it.groups["date"]?.value
                    val env = it.groups["env"]?.value
                    val type = it.groups["type"]?.value
                    val start = (offset - carryOverTextLength) + it.groups.first()!!.range.first
                    val end = it.groups.first()?.range?.last
                    if (date != null && env != null && type != null) {
                        lastEnd = end!!
                        logs.add(0, Log(date = date, env = env, type = type, message = "", start = start))
                    }
                }
                // carry remainder of last buffer incase it gets vut  off in the middle of a pattern
                carryOverText = if (lastEnd > 0) bufferString.substring(lastEnd) else String(buffer)
                lastEnd = lastEnd.plus(offset)
            }
            offset += buffer.size
        } while (bytesRead == buffer.size)
        inputStream.close()
        // set length of each log
        logs.forEachIndexed { index, log ->
            if (index < logs.size - 1) {
                logs[index + 1].length = log.start?.minus(logs[index + 1].start!!)
            }
        }


        if(logs.size > 0 && logs.first().start != null) {
            logs.first().length = file.length().toInt() - logs.first().start!!
        }

    }




    val logCount: Int = logs.size

    fun get(numberOfLogs: Int = logCount, offset: Int = 0): List<Log> {
        val to: Int = if (offset + numberOfLogs > logCount) logCount else offset + numberOfLogs
        if(offset > logCount) return listOf()

        val getList = logs.subList(offset, to).toList()

        getList.forEachIndexed { index, log ->
            if (log.start == null || log.length == null || log.length!! < 0) return@forEachIndexed
            val message = readBytesAt(log.start!!, log.length!!)
            getList[index].message = message.replace(this.splitPattern.toRegex(), "")
        }

        return getList
    }

    fun hasBeenUpdated(): Boolean {
        val file = java.io.File(path)
        return file.length() != size
    }

    fun readBytesAt(offset: Int, length: Int): String {
        val file = java.io.File(path)
        if (offset + length >= file.length() || length < 0 || offset < 0) return ""
        val inputStream = RandomAccessFile(file, "r")
        val buffer = ByteArray(length)
        inputStream.seek(offset.toLong())
        inputStream.read(buffer)
        inputStream.close()

        return String(buffer)
    }

}