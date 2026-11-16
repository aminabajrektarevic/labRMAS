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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import kotlin.math.roundToInt

enum class StatusKind(val label: String) {
    ON_TIME("On time"),
    LATE("Late"),
    ABSENT("Absent")
}
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
    var checkIns by remember { mutableStateOf(sampleCheckIns) }
    var query by rememberSaveable {mutableStateOf("")}
    var onlyLate by rememberSaveable { mutableStateOf(false) }
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
        if (openWindow==null){
            Text(
                text = "open a window to see check-ins",
                modifier = Modifier.padding(16.dp)
            )
        }else {
            val windowCheckIns = checkIns.filter { it.window == openWindow }
            val filteredCheckIns =
                windowCheckIns.filter { checkIn ->
                    val matchesQuery = checkIn.student.name.contains(query, ignoreCase = true)
                    val matchesLate = !onlyLate || checkIn.status is CheckInStatus.Late
                    matchesQuery && matchesLate }

            AttendanceSummary(checkIns = checkIns.filter { it.window == openWindow })
            SearchField(
                query = query,
                onQueryChange = { query = it }
            )
            LateOnlySwitch(
                onlyLate = onlyLate,
                onOnlyLateChange = { onlyLate = it }
            )
            if (filteredCheckIns.isEmpty()){
                Text(
                    text = "No check-ins match the filter",
                    modifier = Modifier.padding(16.dp)
                )
            }else{
                filteredCheckIns.forEach { checkIn ->
                    CheckInCard(
                        checkIn = checkIn,
                        allCheckIns = checkIns
                    )
                }
            }
        }

    }
}

//S isključenim prekidačem ostaju samo studenti iz Window.MIDDLE koji imaju obično slovo 's' ili 'S' u imenu/prezimenu.
//Hedija Šišić se ne vidi jer njeno prezime sadrži 'Š' i 'š', a contains("s", ignoreCase = true) ne izjednačava 'š' i 's'.
//Kad se uključi Only late, ostaju samo oni od tih studenata koji uz slovo 's' imaju i status Late.


//kad bi svako radio dugme u forEach imalo svoj var selected by remember { mutableStateOf(false) }
//svaka opcija bi imala vlastito, nezavisno stanje i klik na jednu ne bi ponistio prethodno izabrane.
//bilo bi moguce oznaciti vise opcija istovremeno pa to vise ne bi bila radioo grupa
// vec bi se ponasalo kao lista checkbox-ova

@Composable
fun StatusPicker(
    selected: StatusKind,
    onSelect: (StatusKind) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        StatusKind.entries.forEach { kind ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(kind) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (kind == selected),
                    onClick = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = kind.label)
            }
        }
    }
}

//za korak od 5 minuta na rasponu 5f..60f postoji 12 vrijednosti (5, 10, ..., 60).
//Kako steps broji samo tacke izmedju krajeva  potrebno je steps = 10.
//sa 5f..60f i steps = 12 slider ima ukupno 14 razlicitih vrijednosti ,
//odnosno 13 intervala sirine 55 / 13 ≈ 4.23. to nsiu cijeli brojevi (osim krajeva 5 i 60).

@Composable
fun LateMinutesSlider(
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (enabled) "Late: $minutes min" else "Late: –",
            style = MaterialTheme.typography.bodyMedium
        )

        Slider(
            value = minutes.toFloat(),
            onValueChange = { onMinutesChange(it.roundToInt()) },
            valueRange = 5f..60f,
            steps = 10,
            enabled = enabled
        )
    }
}
@Composable
fun LateOnlySwitch(onlyLate: Boolean,onOnlyLateChange: (Boolean) -> Unit, modifier: Modifier = Modifier){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOnlyLateChange(!onlyLate) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Only late")
        Switch(
            checked = onlyLate,
            onCheckedChange = null
        )
    }
}
@Composable
fun SearchField(query: String, onQueryChange: (String)-> Unit, modifier: Modifier = Modifier){
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search student")},
        placeholder = {Text("e.g. Amir")},
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}
//ne paimti nista on iz preview funkcije dobije query ami i on samo to iscrta   , onquerychange je prazan pa se ne da nista napisati
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

@Composable
fun CheckInCard(checkIn: CheckIn, modifier: Modifier= Modifier, allCheckIns: List<CheckIn>){
    var expaneded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth().clickable{expaneded=!expaneded}
    ) {
        Column{
            CheckInRow(checkIn = checkIn)
            if (expaneded) {
                val studentCheckIns = allCheckIns.filter { it.student == checkIn.student }


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
@Preview(showBackground = true)
@Composable
fun SearchFieldPreview() {
    Attendance1Theme {
        SearchField(
            query = "ami",
            onQueryChange = {}
        )
    }
}

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
@Preview(name = "Only late - Off", showBackground = true)
@Composable
fun LateOnlySwitchOffPreview() {
    Attendance1Theme {
        LateOnlySwitch(
            onlyLate = false,
            onOnlyLateChange = {}
        )
    }
}

@Preview(name = "Only late - On", showBackground = true)
@Composable
fun LateOnlySwitchOnPreview() {
    Attendance1Theme {
        LateOnlySwitch(
            onlyLate = true,
            onOnlyLateChange = {}
        )
    }
}
@Preview(showBackground = true)
@Composable
fun StatusPickerPreview() {
    Attendance1Theme {
        StatusPicker(
            selected = StatusKind.LATE,
            onSelect = {}
        )
    }
}
@Preview(name = "Late Slider - Enabled", showBackground = true)
@Composable
fun LateMinutesSliderEnabledPreview() {
    Attendance1Theme {
        LateMinutesSlider(
            minutes = 15,
            onMinutesChange = {},
            enabled = true
        )
    }
}

@Preview(name = "Late Slider - Disabled", showBackground = true)
@Composable
fun LateMinutesSliderDisabledPreview() {
    Attendance1Theme {
        LateMinutesSlider(
            minutes = 15,
            onMinutesChange = {},
            enabled = false
        )
    }
}
//@Preview
//@Composable
//fun AttendanceSummaryPreview(){
//    AttendanceSummary(checkIns = sampleCheckIns.filter{it.window == Window.MIDDLE})
//}
////za window = middle, rezultat je  4 2 0
//@Preview(name = "On time", showBackground = true)
//@Composable
//fun CheckInCardOnTimePreview() {
//    CheckInCard(checkIn = sampleCheckIns[0])
//}
//
//@Preview(name = "Late", showBackground = true)
//@Composable
//fun CheckInCardLatePreview() {
//    CheckInCard(checkIn = sampleCheckIns[1])
//}
//
//@Preview(name = "Absent", showBackground = true)
//@Composable
//fun CheckInCardAbsentPreview() {
//    CheckInCard(checkIn = sampleCheckIns[4])
//}

