package screens.logs

import Screen
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fileutils.FileReader
import fileutils.FolderReader
import fileutils.Log
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



    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Project: ${project.name}")
            Button(
                onClick = {
                    onNavigate(Screen.HomeScreen)
                }) {
                Text("Go Home")
            }
            Spacer(modifier = Modifier.height(20.dp))
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
                setSelectedFile = { selectedFile = it }
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
                        text = fileSize + " MB",
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
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.width(24.dp))
                        }

                    }
                }


                LazyColumn {
                    if (fileReader != null && fileReader.logCount > 0) {
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
                .background(MaterialTheme.colors.onPrimary, RoundedCornerShape(20))
                .border(1.dp, MaterialTheme.colors.primary, RoundedCornerShape(20))
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