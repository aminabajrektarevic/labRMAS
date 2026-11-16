package ba.untz.attendance1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ba.untz.attendance1.ui.theme.Attendance1Theme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.Color
import android.content.res.Configuration
import androidx.compose.material3.Surface
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Attendance1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ProfessorScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Composable
fun ProfessorScreen(modifier: Modifier=Modifier){
    var openWindow by rememberSaveable {mutableStateOf<Window?>(null)}
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WindowsControl(
            openWindow = openWindow,
            onOpen = { openWindow = it }
        )

        AttendanceSummary(checkIns = sampleCheckIns)

        sampleCheckIns.forEach { item ->
            CheckInCard(checkIn = item)
        }
    }
}
@Composable
fun StatusBadge(
    status: CheckInStatus,
    modifier: Modifier = Modifier
) {
    Text(
        text = status.label(),
        color = status.color(),
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
fun AttendanceSummary(checkIns: List<CheckIn>, modifier: Modifier=Modifier){
    var navrijeme = checkIns.count{it -> it.status == CheckInStatus.OnTime}
    var kasno = checkIns.count{it -> it.status is CheckInStatus.Late}
    var odsutni = checkIns.count{it -> it.status == CheckInStatus.Absent}
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "On time: $navrijeme Late: $kasno Absent: $odsutni")
    }
}

@Composable
fun Avatar(initials: String, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = initials,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun CheckInRow(
    checkIn: CheckIn,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box {
            Avatar(initials = checkIn.student.initials())

            if (checkIn.status is CheckInStatus.Late) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = checkIn.student.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            StatusBadge(status = checkIn.status)
        }

        Text(
            text = checkIn.timeText(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}



@Composable
fun CheckInStatus.color(): Color = when(this) {
        CheckInStatus.OnTime -> MaterialTheme.colorScheme.primary
        CheckInStatus.Absent -> MaterialTheme.colorScheme.outline
        is CheckInStatus.Late -> MaterialTheme.colorScheme.error
    }
//Ovdje se pojavi error i u CheckInCard funkciji jer tu koristimo when koji zahtijeva da se svi slucajevi obrade
@Composable
fun WindowsControl(openWindow: Window?, onOpen:(Window)->Unit, modifier: Modifier= Modifier){

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Open: ${openWindow?.label ?: "none"}",
            style =MaterialTheme.typography.titleMedium )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Window.entries.forEach { w ->
                Button(
                    onClick = {
                        onOpen(w)
                        Log.d("RMAS", "now: ${w.label}")
                    },
                    enabled = openWindow != w
                ) {
                    Text(text = w.label)
                }
            }
        }
    }
}
//U logcatu se promijeni stanje svaki put kad je dugme kliknuto ali se to ne prikazuje na screenu
//obicna varijabla openWindow ne moze uzrokovat rekompoziciju
//Ako se rotira screen bez da je openWindow rememberSAeveable nece se nista zapamtiti

@Composable
fun CheckInCard(checkIn: CheckIn, modifier: Modifier= Modifier){
    var expaneded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth().clickable{expaneded=!expaneded}
    ) {
        Column{
            CheckInRow(checkIn = checkIn)
            if (expaneded) {
                val studentCheckIns = sampleCheckIns.filter { it.student == checkIn.student }


                Column(
                    modifier = Modifier.padding(start = 64.dp, end = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    studentCheckIns.forEach { item ->
                        Text(
                            text = "${item.window.label}: ${item.status.label()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }


    }
}
// it.student == checkIn.student pronalazi sve prijave jer je Student data class,
//pa operator == uspoređuje vrijednosti atributa
// Da Student nije data class, uspoređivale bi se memorijske reference (kao ===),
// pa bi uslov bio tačan samo za taj jedan konkretan red, a ne za ostale prozore.
@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CheckInRowPreview() {
    Attendance1Theme {
        Surface {
            CheckInRow(checkIn = sampleCheckIns[0])
        }
    }
}
@Preview
@Composable
fun AttendanceSummaryPreview(){
    AttendanceSummary(checkIns = sampleCheckIns.filter{it.window == Window.MIDDLE})
}
//za window = middle, rezultat je  4 2 0
@Preview(name = "On time", showBackground = true)
@Composable
fun CheckInCardOnTimePreview() {
    CheckInCard(checkIn = sampleCheckIns[0])
}

@Preview(name = "Late", showBackground = true)
@Composable
fun CheckInCardLatePreview() {
    CheckInCard(checkIn = sampleCheckIns[1])
}

@Preview(name = "Absent", showBackground = true)
@Composable
fun CheckInCardAbsentPreview() {
    CheckInCard(checkIn = sampleCheckIns[4])
}

