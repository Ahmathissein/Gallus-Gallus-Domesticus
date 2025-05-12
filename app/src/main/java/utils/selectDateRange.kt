import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
fun selectDateRange(
    context: Context,
    initialStart: LocalDate,
    initialEnd: LocalDate,
    onRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    DatePickerDialog(
        context,
        { _, yearStart, monthStart, dayStart ->
            val start = LocalDate.of(yearStart, monthStart + 1, dayStart)

            DatePickerDialog(
                context,
                { _, yearEnd, monthEnd, dayEnd ->
                    val end = LocalDate.of(yearEnd, monthEnd + 1, dayEnd)
                    onRangeSelected(start, end)
                },
                initialEnd.year, initialEnd.monthValue - 1, initialEnd.dayOfMonth
            ).show()

        },
        initialStart.year, initialStart.monthValue - 1, initialStart.dayOfMonth
    ).show()
}
