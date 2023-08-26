package fileutils

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class FolderReader(private val path: String) {
    private val files = mutableListOf<String>()
    private val folders = mutableListOf<String>()
    var logFiles by mutableStateOf(files.filter { it.endsWith(".log") })

    private var coroutineScope = CoroutineScope(Dispatchers.IO)
    private var shouldWatch = false
    init {
        readFiles()
    }

    fun watch() {
        shouldWatch = true
        coroutineScope.launch {
            while (shouldWatch) {
                readFiles()
                delay(1000)
            }
            this.cancel()
        }
    }

    fun stopWatching() {
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(Dispatchers.IO)
        shouldWatch = false
    }

    fun readFiles() {
        println("Reading files")
        folders.clear()
        this.files.clear()
        val file = java.io.File(path)
        val files = file.listFiles()
        if (files != null) {
            for (f in files) {
                if (f.isDirectory) {
                    if(!folders.contains(f.name)) folders.add(f.name)
                } else {
                    if(!this.files.contains(f.name)) this.files.add(f.name)
                }
            }
        }
        this.logFiles = this.files.filter { it.endsWith(".log") }
    }

}