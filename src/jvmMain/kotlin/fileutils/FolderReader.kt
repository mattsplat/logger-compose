package fileutils

class FolderReader(path: String) {
    private val files = mutableListOf<String>()
    private val folders = mutableListOf<String>()

    init {
        val file = java.io.File(path)
        val files = file.listFiles()
        if (files != null) {
            for (f in files) {
                if (f.isDirectory) {
                    folders.add(f.name)
                } else {
                    this.files.add(f.name)
                }
            }
        }
    }

    fun getLogFiles(): List<String> {
        return files.filter { it.endsWith(".log") }
    }

    fun getFolders(): List<String> {
        return folders
    }

}