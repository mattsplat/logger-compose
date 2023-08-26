package screens.home

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import data.Project
import javax.swing.JFileChooser

@Composable
@Preview
fun AddProjectDialog(visible: Boolean, onDismiss: () -> Unit, onConfirm: (project: Project) -> Unit) {
    var text by remember { mutableStateOf("") }
    var path by remember { mutableStateOf("") }

    Window(
        visible = visible,
        onCloseRequest = onDismiss,
        title = "Add Project",
        resizable = false,
        undecorated = true,
    ) {
        Surface(
            modifier = Modifier.background(color = MaterialTheme.colors.background)
        ) {

            Column(
                modifier = Modifier.fillMaxSize().padding(start = 10.dp, top = 50.dp) ,
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {



                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp, top = 10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {

                    Text(
                        modifier = Modifier.padding(bottom = 10.dp, top = 10.dp, end = 10.dp),
                        style = TextStyle(textAlign = TextAlign.Center),
                        text = "Project Name"
                    )

                    TextField(
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .weight(0.75f)
                            .height(50.dp)
                            .background(MaterialTheme.colors.onPrimary, RoundedCornerShape(20)),
                        value = text,
                        onValueChange = { text = it },
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp, bottom = 10.dp, top = 10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier
                            .padding(end = 0.dp, bottom = 10.dp)
                            .weight(0.3f)
                            .height(50.dp)
                            .background(MaterialTheme.colors.primary, RoundedCornerShape(20)),
                        onClick = {
                            val chooser = JFileChooser()
                            chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                            val result = chooser.showOpenDialog(null)
                            if (result == JFileChooser.APPROVE_OPTION) {
                                println(chooser.selectedFile.absolutePath)
                                path = chooser.selectedFile.absolutePath
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Text("Choose Folder")
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(start = 0.dp, bottom = 10.dp)
                            .height(50.dp)
                            .weight(0.7f)
                            .background(Color.LightGray, RoundedCornerShape(20)),
                    ) {

                        Text(
                            text = path,
                            style = TextStyle(textAlign = TextAlign.Start),
                        )
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp, bottom = 10.dp, top = 10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier
                            .padding(end = 10.dp, bottom = 10.dp)
                            .weight(0.5f)
                            .height(50.dp)
                            .background(MaterialTheme.colors.primary, RoundedCornerShape(20)),
                        onClick = {
                            onDismiss()
                            text = ""
                            path = ""
                        }) {
                        Text("Cancel")
                    }

                    if (text.isNotEmpty() && path.isNotEmpty()) {
                        Button(
                            modifier = Modifier
                                .padding(end = 10.dp, bottom = 10.dp)
                                .weight(0.5f)
                                .height(50.dp)
                                .background(Color.White, RoundedCornerShape(20)),
                            onClick = {
                                onConfirm(Project(text, path))
                                text = ""
                                path = ""
                            }) {
                            Text("Add Project")
                        }
                    }

                }


            }
        }
    }
}