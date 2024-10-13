package meea.licence.greeny.client_ui.ui.greenhouse_data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import meea.licence.greeny.MyApp
import meea.licence.greeny.R
import meea.licence.greeny.SharedPreferencesRepository
import meea.licence.greeny.model.ComponentModel
import meea.licence.greeny.model.GHControllerModel
import meea.licence.greeny.network.ComponentService
import meea.licence.greeny.network.GHControllerService
import meea.licence.greeny.network.RetrofitClient
import meea.licence.greeny.network.SensorLogService
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class GreenhouseDataViewModel(private val repository: SharedPreferencesRepository) : ViewModel() {

    private val _greenhouses = MutableLiveData<List<GHControllerModel>>()
    val greenhouses: LiveData<List<GHControllerModel>> get() = _greenhouses

    private val _sensors = MutableLiveData<List<ComponentModel>>()
    val sensors: LiveData<List<ComponentModel>> get() = _sensors

    private val _sensorLogs = MutableLiveData<List<SensorLogModel>>()
    val sensorLogs: LiveData<List<SensorLogModel>> get() = _sensorLogs

    private val _realTimeEntries = MutableLiveData<Entry>()
    val realTimeEntries: LiveData<Entry> get() = _realTimeEntries

    private val _selectedSensorId = MutableLiveData<Int?>()
    val selectedSensorId: LiveData<Int?> get() = _selectedSensorId

    private var baseTimestamp: Long = 0L
    private var webSocket: WebSocket? = null

    private lateinit var stompClient: StompClient
    private lateinit var lifecycleDisposable: Disposable
    private lateinit var topicDisposable: Disposable

    init {
        fetchGreenhouses()
    }

    fun setBaseTimestamp(baseTimestamp: Long) {
        this.baseTimestamp = baseTimestamp
    }

    fun fetchGreenhouses() {
        viewModelScope.launch {
            val userId = repository.getUserId()
            val token = repository.getToken()

            if (userId != -1) {
                token?.let {
                    val ghControllerService = RetrofitClient.getRetrofitClient(MyApp.getContext(), token).create(GHControllerService::class.java)
                    ghControllerService.getControllerByUserId(userId).enqueue(object : Callback<List<GHControllerModel>> {
                        override fun onResponse(call: Call<List<GHControllerModel>>, response: Response<List<GHControllerModel>>) {
                            if (response.isSuccessful) {
                                _greenhouses.value = response.body()
                            } else {
                                // Handle error
                            }
                        }

                        override fun onFailure(call: Call<List<GHControllerModel>>, t: Throwable) {
                            // Handle error
                        }
                    })
                }
            }
        }
    }

    fun fetchSensors(controllerId: Int) {
        viewModelScope.launch {
            val token = repository.getToken()
            token?.let {
                val componentService = RetrofitClient.getRetrofitClient(MyApp.getContext(), token).create(ComponentService::class.java)
                componentService.getActiveComponentsByControllerId(controllerId).enqueue(object : Callback<List<ComponentModel>> {
                    override fun onResponse(call: Call<List<ComponentModel>>, response: Response<List<ComponentModel>>) {
                        if (response.isSuccessful) {
                            _sensors.value = response.body()
                        } else {
                            // Handle error
                        }
                    }

                    override fun onFailure(call: Call<List<ComponentModel>>, t: Throwable) {
                        // Handle error
                    }
                })
            }
        }
    }

    fun fetchSensorLogs(sensorId: Int) {
        viewModelScope.launch {
            val token = repository.getToken()
            token?.let {
                val sensorLogService = RetrofitClient.getRetrofitClient(MyApp.getContext(), token).create(SensorLogService::class.java)
                sensorLogService.getSensorLogs(sensorId).enqueue(object : Callback<List<SensorLogModel>> {
                    override fun onResponse(call: Call<List<SensorLogModel>>, response: Response<List<SensorLogModel>>) {
                        if (response.isSuccessful) {
                            _sensorLogs.value = response.body()
                        } else {
                            // Handle error
                        }
                    }

                    override fun onFailure(call: Call<List<SensorLogModel>>, t: Throwable) {
                        // Handle error
                    }
                })
            }
        }
    }

    fun startRealTimeUpdates() {
        if (webSocket == null) {
            setupWebSocket()
        }
    }

    fun stopRealTimeUpdates() {
        webSocket?.close(1000, null)
        webSocket = null
        stompClient.disconnect()
    }

    private fun setupWebSocket() {
        // Initialize StompClient with OkHttpConnectionProvider
        val sslContext = SSLContext.getInstance("TLS")
        val trustManager = loadTrustManagerFromP12()

        sslContext.init(null, arrayOf(trustManager), SecureRandom())

        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .build()

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "wss://meea.duckdns.org:8443/greeny-websocket", null, okHttpClient)

        stompClient.withClientHeartbeat(1000).withServerHeartbeat(1000)

        lifecycleDisposable = stompClient.lifecycle()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> {
                        subscribeToTopic()
                    }
                    LifecycleEvent.Type.ERROR -> {
                        lifecycleEvent.exception?.printStackTrace()
                    }
                    LifecycleEvent.Type.CLOSED -> println("Stomp connection closed")
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> println("Server heartbeat failed")
                }
            }, { error ->
                error.printStackTrace()
            })

        stompClient.connect()
    }

    private fun loadTrustManagerFromP12(): X509TrustManager {
        val keyStore = KeyStore.getInstance("PKCS12")
        val inputStream = MyApp.getContext().resources.openRawResource(R.raw.keystore_greeny)
        keyStore.load(inputStream, "greeny".toCharArray())
        inputStream.close()

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        return trustManagerFactory.trustManagers.first { it is X509TrustManager } as X509TrustManager
    }

    private fun subscribeToTopic() {
        topicDisposable = stompClient.topic("/topic/sensor-logs")
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ topicMessage: StompMessage ->
                handleIncomingData(topicMessage.payload)
            }, { error ->
                error.printStackTrace()
            })

    }

    private fun handleIncomingData(message: String) {
        viewModelScope.launch {
            try {
                // Parse the JSON message as an array
                val jsonArray = JSONArray(message)

                // Loop through the array
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)

                    // Extract the fields from each JSON object
                    val sensorId = jsonObject.getInt("sensorId")
                    val timestamp = jsonObject.getLong("timestamp")
                    val value = jsonObject.getDouble("value").toFloat()

                    // Check if the sensor ID matches the selected sensor ID
                    if (sensorId == _selectedSensorId.value) {
                        // Create the SensorLogModel and update LiveData
                        val sensorLog = SensorLogModel(sensorId, timestamp, value)
                        _realTimeEntries.postValue(Entry((timestamp - baseTimestamp) / 1000.0f, value))
                        fetchSensorLogs(sensorLog.sensorId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocket?.close(1000, null)
        stompClient.disconnect()
        lifecycleDisposable.dispose()
        topicDisposable.dispose()
    }

    fun setSelectedSensorId(sensorId: Int?) {
        _selectedSensorId.value = sensorId
    }
}
