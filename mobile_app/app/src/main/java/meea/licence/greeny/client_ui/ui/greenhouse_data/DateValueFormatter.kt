package meea.licence.greeny.client_ui.ui.greenhouse_data

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DateValueFormatter(
    private val baseTimestamp: Long,
    private val timeUnit: TimeUnit = TimeUnit.SECONDS
) : ValueFormatter() {

    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        val offset: Long = when (timeUnit) {
            TimeUnit.MILLISECONDS -> value.toLong()
            TimeUnit.SECONDS -> (value * 1000).toLong()
            TimeUnit.MINUTES -> (value * 60 * 1000).toLong()
            else -> value.toLong()
        }
        val actualTimestamp = baseTimestamp + offset
        return dateFormat.format(Date(actualTimestamp))
    }
}
