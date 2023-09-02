package screens.logs

import Screen
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import components.AnimatedIcon
import fileutils.FileReader
import fileutils.FolderReader
import fileutils.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import navcontroller.NavController
import java.math.RoundingMode
import kotlin.math.ceil


@Composable
@Preview
fun LogViewScreen(
    navController: NavController
) {
    val project = navController.currentProject!!
    val folderReader = remember { FolderReader(project.path) }
    val logFiles = folderReader.logFiles
    var selectedFile by remember { mutableStateOf<String?>(null) }
    val fileReader = if (selectedFile != null) FileReader((project.path) + "/" + selectedFile) else null
    val fileSize =
        if (fileReader != null && fileReader.size > 0) ((fileReader.size.toDouble() / 1024 / 1024).toBigDecimal()
            .setScale(2, RoundingMode.DOWN)).toString() else 0.toString()

    val watchFolders = remember { mutableStateOf(false) }
    val loading = remember { mutableStateOf(false) }

    val perPage = 25
    var page by remember { mutableStateOf(1) }

    val setWatched = { active: Boolean ->
        watchFolders.value = active
        if (active) folderReader.watch() else folderReader.stopWatching()
    }

    val onNavigate = { name: Screen ->
        setWatched(false)
        navController.navigate(name.name)
    }

    val coroutineScope = rememberCoroutineScope()


    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 25.dp, start = 25.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedIcon(Icons.Filled.Home, "Home", onClick = {
                onNavigate(Screen.HomeScreen)
            })

            Spacer(modifier = Modifier.width(20.dp))

            Text("Project: ${project.name}")
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            FilesSection(
                watchFolders = watchFolders.value,
                logFiles = logFiles,
                selectedFile = selectedFile,
                setWatch = {
                    setWatched(it)
                },
                setSelectedFile = {
                    selectedFile = it
                    page = 1
                    loading.value = true
                    coroutineScope.launch {
                        delay(250)
                        loading.value = false
                    }
                }
            )


            Column(
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, top = 25.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colors.primary, RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colors.primary)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = fileReader?.filename ?: "No File Selected",
                        style = TextStyle(color = MaterialTheme.colors.onPrimary)
                    )
                    Text(
                        text = "Logs: " + fileReader?.logCount,
                        style = TextStyle(color = MaterialTheme.colors.onPrimary)
                    )
                    Text(
                        text = "$fileSize MB",
                        style = TextStyle(color = MaterialTheme.colors.onPrimary),
                        textAlign = TextAlign.End
                    )
                    if (fileReader != null && fileReader.logCount > perPage) {
                        if (page > 1) {
                            Icon(
                                imageVector = Icons.Filled.ArrowLeft,
                                contentDescription = "Previous Page",
                                modifier = Modifier.clickable {
                                    if (page > 1) {
                                        page--
                                    }
                                    loading.value = true
                                    coroutineScope.launch {
                                        delay(50)
                                        loading.value = false
                                    }
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.width(24.dp))
                        }

                        Text(
                            text = page.toString() + " / " +
                                    ceil(fileReader.logCount / perPage.toFloat()).toInt(),
                            style = TextStyle(color = MaterialTheme.colors.onPrimary)
                        )

                        if (fileReader!!.logCount > (page * perPage)) {
                            Icon(
                                imageVector = Icons.Filled.ArrowRight,
                                contentDescription = "Next Page",
                                modifier = Modifier.clickable {
                                    if (page <= (fileReader.logCount / perPage)) {
                                        page++
                                    }
                                    loading.value = true
                                    coroutineScope.launch {
                                        delay(50)
                                        loading.value = false
                                    }
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.width(24.dp))
                        }

                    }
                }


                LazyColumn {
                    if (fileReader != null && fileReader.logCount > 0 && !loading.value) {
                        fileReader.get(25, ((page - 1) * perPage)).forEach { log ->
                            item {
                                LogLine(log)
                            }
                        }
                    }
                }


            }
        }
    }
}


fun limitChars(text: String, limit: Int): String {
    return if (text.length > limit) {
        text.substring(0, limit) + "..."
    } else {
        text
    }
}

@Composable
fun LogLine(
    log: Log,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val displayText = if (isExpanded) {
        log.date + " ${log.type} : \n" + log.message
    } else {
        log.date + " ${log.type} : " + limitChars(log.message, 100)
    }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp, top = 10.dp, end = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isExpanded) {
            AnimatedIcon(Icons.Filled.CopyAll, "Copy All") {
                clipboardManager.setText(AnnotatedString(displayText))
            }
        }

        Text(
            modifier = Modifier
                .weight(0.9f)
                .background(MaterialTheme.colors.onPrimary, RoundedCornerShape(12.dp, 12.dp, 12.dp, 12.dp))
                .border(1.dp, MaterialTheme.colors.primary, RoundedCornerShape(12.dp, 12.dp, 12.dp, 12.dp))
                .padding(10.dp).clickable { isExpanded = !isExpanded },
            style = TextStyle(textAlign = TextAlign.Center),
            text = displayText,
        )
    }
}

@Composable
fun FilesSection(
    watchFolders: Boolean,
    logFiles: List<String>,
    selectedFile: String?,
    setWatch: (Boolean) -> Unit,
    setSelectedFile: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.25f).padding(start = 25.dp, top = 25.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colors.primary, RoundedCornerShape(4.dp))
                .background(MaterialTheme.colors.primary)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Files",
                style = TextStyle(color = MaterialTheme.colors.onPrimary)
            )
            Icon(
                imageVector = if (watchFolders) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                contentDescription = "Watch Logs",
                modifier = Modifier.clickable {
                    setWatch(!watchFolders)
                }
            )

        }
        LazyColumn {
            if (logFiles.isNotEmpty()) {
                logFiles.forEach { file ->
                    item {
                        val isSelected = selectedFile == file
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 2.dp, top = 10.dp, end = 10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier
                                    .weight(0.9f)
                                    .background(
                                        if (isSelected) MaterialTheme.colors.onSecondary else MaterialTheme.colors.onPrimary,
                                        RoundedCornerShape(20)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colors.primaryVariant else MaterialTheme.colors.primary,
                                        RoundedCornerShape(20)
                                    )
                                    .padding(10.dp).clickable { setSelectedFile(file) },
                                style = TextStyle(textAlign = TextAlign.Center),
                                text = file,
                            )
                        }
                    }
                }
            }
        }
    }
}