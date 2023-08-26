import data.Project
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.File


object Store {
    val UserPreferencesFile: () -> File
        get() = {
            val file = File(File(System.getProperty("user.home")), "logger.preferences")
            if (!file.exists()) {
                file.createNewFile()
                file.writeText(Json.encodeToString(JsonObject(mapOf())))
            }
            file
        }

    fun write(key: String, value: String) {
        val json = readAsJson()
        val mutable = json.toMutableMap()
        mutable[key] = Json.parseToJsonElement(value)
        val writeJson = JsonObject(mutable)
        UserPreferencesFile().writer().write(Json.encodeToString(writeJson))
    }

    private fun readAsJson(): JsonObject {
        return try {
            Json.decodeFromString(UserPreferencesFile().reader().readText())
        } catch (e: Exception) {
            JsonObject(mapOf())
        }
    }

    fun read(key: String): String? {
        val json = readAsJson()
        return json[key]?.let { Json.encodeToString(it) }
    }

    fun saveProjects(projects: List<Project>) {
        val json = Json.encodeToString(projects)
        write("projects", json)
    }

    fun getProjects(): List<Project> {
        val json = read("projects")
        if (json != null) {
            return try {
                Json.decodeFromString(json)
            } catch (e: Exception) {
                listOf()
            }
        }
        return listOf()
    }
}