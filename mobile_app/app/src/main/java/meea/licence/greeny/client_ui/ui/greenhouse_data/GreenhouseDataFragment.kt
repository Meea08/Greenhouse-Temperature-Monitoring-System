package meea.licence.greeny.client_ui.ui.greenhouse_data

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import meea.licence.greeny.R
import meea.licence.greeny.SharedPreferencesRepository
import java.util.concurrent.TimeUnit

class GreenhouseDataFragment : Fragment(R.layout.fragment_greenhouse_data) {

    private lateinit var viewModel: GreenhouseDataViewModel
    private lateinit var lineChart: LineChart
    private lateinit var greenhouseSpinner: Spinner
    private lateinit var sensorSpinner: Spinner
    private var baseTimestamp: Long = 0L

    private lateinit var realTimeTemperatureLabel: TextView
    private lateinit var realTimeTemperatureValue: TextView
    private var minThreshold: Double = 0.0
    private var maxThreshold: Double = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = SharedPreferencesRepository(requireContext())
        val factory = GreenhouseDataViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[GreenhouseDataViewModel::class.java]

        lineChart = view.findViewById(R.id.sensor_chart)
        greenhouseSpinner = view.findViewById(R.id.spinner_greenhouse)
        sensorSpinner = view.findViewById(R.id.spinner_vis_data)

        // Initialize the TextViews
        realTimeTemperatureLabel = view.findViewById(R.id.text_real_time_temperature_label)
        realTimeTemperatureValue = view.findViewById(R.id.text_real_time_temperature_value)

        setupChart()
        setupGreenhouseSpinner()
        setupSensorSpinner()

        observeViewModelData()

        // Observe real-time temperature data
        viewModel.realTimeEntries.observe(viewLifecycleOwner) { entry ->
            realTimeTemperatureValue.text = entry.y.toString()

            // Check if the temperature exceeds the threshold
            if (entry.y > maxThreshold || entry.y < minThreshold) {
                realTimeTemperatureValue.setTextColor(Color.RED)
            } else {
                realTimeTemperatureValue.setTextColor(Color.BLACK)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopRealTimeDataUpdates()
    }

    private fun setupGreenhouseSpinner() {
        viewModel.greenhouses.observe(viewLifecycleOwner) { greenhouses ->
            val greenhouseNames = greenhouses.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, greenhouseNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            greenhouseSpinner.adapter = adapter

            greenhouseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedGreenhouse = greenhouses[position]
                    viewModel.fetchSensors(selectedGreenhouse.id!!)
                    minThreshold = selectedGreenhouse.minThreshold
                    maxThreshold = selectedGreenhouse.maxThreshold
                    setupChartWithThresholds(minThreshold, maxThreshold)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun setupSensorSpinner() {
        viewModel.sensors.observe(viewLifecycleOwner) { sensors ->
            val sensorNames = sensors.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sensorNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sensorSpinner.adapter = adapter

            sensorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedSensor = sensors[position]
                    viewModel.setSelectedSensorId(selectedSensor.id)  // Update the ViewModel with the selected sensor ID
                    clearChart()  // Clear the chart before updating with new logs
                    viewModel.fetchSensorLogs(selectedSensor.id!!)

                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun observeViewModelData() {
        viewModel.sensorLogs.observe(viewLifecycleOwner) { sensorLogs ->
            if (sensorLogs.isNotEmpty()) {
                updateChartWithLogs(sensorLogs)
            }
        }
    }

    private fun clearChart() {
        lineChart.clear()
        lineChart.invalidate()
        baseTimestamp = 0L // Reset the base timestamp
    }

    private fun stopRealTimeDataUpdates() {
        viewModel.stopRealTimeUpdates()
    }

    private fun updateChartWithLogs(sensorLogs: List<SensorLogModel>) {
        if (sensorLogs.isNotEmpty()) {
            baseTimestamp = sensorLogs.minOf { it.timestamp }
            viewModel.setBaseTimestamp(baseTimestamp)

            // Map sensor logs to chart entries
            val entries = sensorLogs.map { log ->
                val relativeTime = (log.timestamp - baseTimestamp) / 1000.0f
                Entry(relativeTime, log.value)
            }

            // Create the dataset
            val lineDataSet = LineDataSet(entries, "Temperature Over Time").apply {
                setDrawValues(false) // Don't draw values on the points
                setDrawCircles(false) // Do not draw circles on data points
                color = Color.GREEN // Line color
                lineWidth = 2f // Line width
                setDrawFilled(false) // Do not fill under the line
            }

            // Set data to the chart
            val lineData = LineData(lineDataSet)
            lineChart.data = lineData

            // Customize the X-axis
            val xAxis: XAxis = lineChart.xAxis
            xAxis.valueFormatter = DateValueFormatter(baseTimestamp, TimeUnit.SECONDS)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 3600f // Granularity set to 1 hour in seconds (3600s)
            xAxis.labelRotationAngle = -45f
            xAxis.setDrawGridLines(false)
            xAxis.setAvoidFirstLastClipping(true)

            // Enable horizontal scrolling and set visible range
            lineChart.setVisibleXRangeMaximum(200f) // Adjust this value to control the visible range

            // Move the view to the last data point
            val lastEntryX = entries.lastOrNull()?.x ?: 0f
            lineChart.moveViewToX(lastEntryX) // Start viewing from the last data point

            // Final chart settings
            lineChart.description.isEnabled = false
            lineChart.axisRight.isEnabled = false
            lineChart.invalidate() // Refresh the chart
        }
    }


    private fun setupChart() {
        lineChart.apply {
            description.isEnabled = false // Disable chart description
            setTouchEnabled(true) // Enable touch gestures
            isDragEnabled = true // Enable dragging
            setScaleEnabled(false) // Disable all scaling
            setPinchZoom(false) // Disable pinch zoom

            // Enable horizontal scrolling
            isDragXEnabled = true
            isScaleXEnabled = false // Disable X-axis scaling
            isScaleYEnabled = false // Disable Y-axis scaling

            // X-Axis customization for horizontal scrolling
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f // Ensures that each label represents one hour (or unit of time)
                setDrawGridLines(false)
                textColor = Color.BLACK
                textSize = 12f
                setAvoidFirstLastClipping(true)
            }

            // Y-Axis customization to fit the data vertically
            axisLeft.apply {
                textColor = Color.BLACK
                textSize = 12f
                setDrawGridLines(true)
                gridColor = Color.LTGRAY
                gridLineWidth = 1f
            }

            // Disable right Y-Axis
            axisRight.isEnabled = false

            // Customize the legend
            legend.apply {
                isEnabled = true
                form = Legend.LegendForm.LINE
                textColor = Color.BLACK
                textSize = 12f
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }

            // Disable background grid and borders
            setDrawGridBackground(false)
            setDrawBorders(false)

            // Allow horizontal scrolling with a fixed number of points visible at a time
            setVisibleXRangeMaximum(200f) // Adjust this value to control how many points are visible at once
            moveViewToX(0f) // Start viewing from the first data point
        }
    }

    private fun setupChartWithThresholds(minThreshold: Double, maxThreshold: Double) {
        val minLine = LimitLine(minThreshold.toFloat(), "Min Threshold").apply {
            lineWidth = 2f
            lineColor = Color.RED
            textColor = Color.RED
            textSize = 12f
            enableDashedLine(10f, 10f, 0f)
        }

        val maxLine = LimitLine(maxThreshold.toFloat(), "Max Threshold").apply {
            lineWidth = 2f
            lineColor = Color.RED
            textColor = Color.RED
            textSize = 12f
            enableDashedLine(10f, 10f, 0f)
        }

        lineChart.axisLeft.apply {
            removeAllLimitLines() // Remove previous limit lines
            addLimitLine(minLine)
            addLimitLine(maxLine)
            setDrawLimitLinesBehindData(true) // Ensure limit lines are drawn behind data
        }
    }


    override fun onResume() {
        super.onResume()
        viewModel.startRealTimeUpdates()
    }
}
