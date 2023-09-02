import fileutils.FileReader
import org.junit.jupiter.api.Test


class TestParsingLog {
    val filePath = System.getProperty("user.dir").toString() + "/src/jvmTest/kotlin/laravel.log"

    @Test
    fun parseAllLines() {
        val fileReader = FileReader(filePath)
        assert(fileReader.logs.isNotEmpty())
    }

    @Test
    fun indexLogs() {
        val fileReader = FileReader(filePath)
        fileReader.indexLogs()
        assert(fileReader.logs.isNotEmpty())

        val page = fileReader.get(25, 20)
        assert(page.isNotEmpty())
        assert(page.first().message.isNotBlank())
    }


}
