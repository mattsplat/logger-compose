package screens.home

import Screen
import Store
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.Project
import navcontroller.NavController
import java.util.prefs.Preferences


@Composable
@Preview
fun HomeScreen(
    navController: NavController
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val getSavedProjects = Store.getProjects()
    val projectList by remember {
        mutableStateOf(
            mutableListOf<Project>(
                Project("HBNation Local", "/Users/matt/code/Projects/hbnation/hbnation-site/storage/logs"),
                Project("NTDF", "/Users/matt/code/Projects/barnhouse/NTDF/storage/logs"),
            )
        )
    }

    Preferences.userRoot().get("projects", null)

    Column(
        modifier = Modifier.fillMaxSize().padding(start = 25.dp, top = 25.dp, end = 25.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .padding(bottom = 10.dp, top = 10.dp),
            style = TextStyle(textAlign = TextAlign.Center),
            text = navController.currentScreen.value
        )

        Button(
            modifier = Modifier
                .padding(end = 32.dp, bottom = 10.dp, top = 10.dp)
                .width(200.dp)
                .height(50.dp).align(Alignment.CenterHorizontally),
            onClick = {
                showAddDialog = true
            }
        ) {
            Text(
                modifier = Modifier.padding(bottom = 10.dp, top = 10.dp, end = 10.dp),
                style = TextStyle(textAlign = TextAlign.Center),
                text = "Add Project"
            )
        }


        if (projectList.size > 0) {
            projectList.forEach { project ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp, top = 10.dp, end = 10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier
                            .weight(0.3f)
                            .background(MaterialTheme.colors.onPrimary, RoundedCornerShape(20))
                            .border(1.dp, MaterialTheme.colors.primary, RoundedCornerShape(20))
                            .padding(10.dp),
                        style = TextStyle(textAlign = TextAlign.Center),
                        text = project.name
                    )
                    Text(
                        modifier = Modifier
                            .weight(0.7f)
                            .background(MaterialTheme.colors.onPrimary, RoundedCornerShape(20))
                            .border(1.dp, MaterialTheme.colors.primary, RoundedCornerShape(20))
                            .padding(10.dp),
                        style = TextStyle(textAlign = TextAlign.Center),
                        text = project.path
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp, top = 0.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.End
                ) {

                    Box(modifier = Modifier.clickable {
                        println("Clicked")
                        navController.currentProject = project
                        navController.navigate(Screen.LogViewScreen.name)
                    }.padding(end = 10.dp)) {
                        Icon(imageVector = Icons.Filled.Visibility, contentDescription = "View")
                    }
                    Box(modifier = Modifier.clickable {
                        println("Clicked")
                    }.padding(end = 10.dp)) {
                        Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = "Remove")
                    }
                }
            }
        }


    }


    if (showAddDialog) {
        AddProjectDialog(
            visible = showAddDialog,
            onDismiss = { showAddDialog = false },
            onConfirm = { project: Project ->
                showAddDialog = false
                projectList.add(project)
                Store.saveProjects(projectList.toList())
            }
        )
    }

}