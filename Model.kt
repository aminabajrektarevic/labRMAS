package ba.untz.attendance1

data class Student(val id: String, val name: String)

enum class Window(val label: String){
    START("start"),
    MIDDLE("middle"),
    END("end")
}

sealed interface CheckInStatus{
    data object OnTime: CheckInStatus
    data class Late(val minutes: Int): CheckInStatus
    data object Absent: CheckInStatus
}

data class CheckIn(
    val student: Student,
    val window: Window,
    val minutes: Int,
    val status: CheckInStatus
)

fun Student.initials() : String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .map { it.first().uppercaseChar() }
        .joinToString("")

fun CheckInStatus.label() : String = when(this) {
    CheckInStatus.OnTime -> "On time"
    is CheckInStatus.Late-> "$minutes min late"
    CheckInStatus.Absent -> "Absent"
}

val sampleCheckIns = listOf(
    CheckIn(Student("IB101", "Amina Bajrektarević"), Window.START, 481, CheckInStatus.OnTime),
    CheckIn(Student("IB102", "Amir Imširović"), Window.START, 492, CheckInStatus.Late(12)),
    CheckIn(Student("IB103", "Faris Ćosić"), Window.START, 480, CheckInStatus.OnTime),
    CheckIn(Student("IB104", "Hedija Šišić"), Window.START, 505, CheckInStatus.Late(25)),
    CheckIn(Student("IB105", "Selma Lugavić"), Window.START, 0, CheckInStatus.Absent),
    CheckIn(Student("IB106", "Elma Đonlić"), Window.START, 483, CheckInStatus.OnTime)

)