import fileutils.FileReader
import org.junit.jupiter.api.Test

class TestParsingLog {
    val filePath = System.getProperty("user.dir").toString() + "/src/jvmTest/kotlin/laravel.log"

    @Test
    fun parseAllLines() {
        val fileReader = FileReader(filePath)
        assert(fileReader.logs.isNotEmpty())
    }
}
