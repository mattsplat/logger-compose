import fileutils.FileReader
import fileutils.Log
import org.junit.jupiter.api.Test
import java.io.FileInputStream

class TestParsingLog {
    val filePath = System.getProperty("user.dir").toString() + "/src/jvmTest/kotlin/laravel.log"

    @Test
    fun parseAllLines() {
        val fileReader = FileReader(filePath)
        assert(fileReader.logs.isNotEmpty())
    }

//    @Test
//    fun parseInChunks() {
//        val file = java.io.File(filePath)
//        val charArray = CharArray(1024)
//        val logs = mutableListOf<Log>()
//        val file_length = file.length()
//        var offset = 0
//
//        while (offset < file_length) {
//            val length = if (offset + 1024 >= file_length) file_length - offset - 1 else 1024
//            val bytesRead = file.bufferedReader(Charset.defaultCharset(),1024).read(charArray, offset, length.toInt())
//            val splitPattern = """\[(?<date>.*)]\s(?<env>\w+)\.(?<type>\w+):"""   //"""\[.*]\s\w+\.\w+:"""
//            val splitRegex = Regex(splitPattern, RegexOption.MULTILINE)
//            val splits = splitRegex.findAll(charArray.joinToString(""))
//            splits.forEach {
//                val date = it.groups["date"]?.value
//                val env = it.groups["env"]?.value
//                val type = it.groups["type"]?.value
//                val start = it.groups.first()?.range?.first
//                if (date != null && env != null && type != null && start != null) {
//                    val end = it.groups.first()?.range?.last
//                    val message = charArray.sliceArray(start..end!!).joinToString("")
//                    logs.add(0, Log(date = date, env = env, type = type, message = message))
//                }
//            }
//            offset += bytesRead
//        }
//
//
//    }

    @Test
    fun parseInChunks() {

        val logs = mutableListOf<Log>()

        val file = java.io.File(filePath)
        val inputStream = FileInputStream(file)
        val buffer = ByteArray(1024)
        var bytesRead: Int
        var carryOverText = ""
        var offset = 0
        do {
            bytesRead = inputStream.read(buffer)
            if (bytesRead != -1) {
                val splitPattern = """\[(?<date>.*)]\s(?<env>\w+)\.(?<type>\w+):"""   //"""\[.*]\s\w+\.\w+:"""
                val splitRegex = Regex(splitPattern, RegexOption.MULTILINE)
                val bufferString = carryOverText + String(buffer)
                val splits = splitRegex.findAll(bufferString)
                var lastEnd = 0
                splits.forEach {
                    val date = it.groups["date"]?.value
                    val env = it.groups["env"]?.value
                    val type = it.groups["type"]?.value
                    val start = it.groups.first()?.range?.first
                    if (date != null && env != null && type != null && start != null) {
                        val end = it.groups.first()?.range?.last
                        lastEnd = end!!
                        val message = bufferString.substring(start..end!!)
                        logs.add(0, Log(date = date, env = env, type = type, message = message, start = start.plus(offset)))
                    }
                }
                carryOverText = if (lastEnd > 0) bufferString.substring(lastEnd) else ""
                lastEnd = lastEnd.plus(offset)
            }
            offset += bytesRead - carryOverText.length
        } while (bytesRead != -1)
        inputStream.close()
        logs.forEachIndexed { index, log ->
            if (index < logs.size - 1) {
               logs[index + 1].length = log.start?.minus(logs[index + 1].start!!)
            }
        }

        logs.first().length = file.length().toInt() - logs.first().start!!
        assert(logs.isNotEmpty())
    }

}
