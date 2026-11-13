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
import android.graphics.Color



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Attendance1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) {

                }
            }
        }
    }
}

@Composable
fun CheckInCard(checkIn: CheckIn) {
    val statusText = when (checkIn.status) {
        is CheckInStatus.OnTime -> "On time"
        is CheckInStatus.Late -> "Late ${checkIn.status.minutes} min"
        is CheckInStatus.Absent -> "Absent"
    }

    Row(modifier = Modifier.padding(all = 8.dp)) {

        Avatar(initials = checkIn.student.initials())
        Spacer(modifier = Modifier.width(8.dp))
        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(text = checkIn.student.name)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = statusText)
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
fun CheckInStatus.color(): Color{
    when{
        CheckInStatus.OnTime ->
    }
}
@Preview
@Composable
fun CheckInRowPreview(){
    CheckInRow(sampleCheckIns[1])
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

