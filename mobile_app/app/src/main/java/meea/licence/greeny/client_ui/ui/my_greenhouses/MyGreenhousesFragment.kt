package meea.licence.greeny.client_ui.ui.my_greenhouses

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import meea.licence.greeny.R
import meea.licence.greeny.SharedPreferencesRepository
import meea.licence.greeny.model.ComponentModel
import meea.licence.greeny.model.GHControllerModel
import meea.licence.greeny.network.RetrofitClient
import meea.licence.greeny.network.RetrofitESPClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyGreenhousesFragment : Fragment(), OnItemClickListener {

    private lateinit var greenhouseRecyclerViewRef: RecyclerView
    private lateinit var adapterGreenhouseList: AdapterGreenhouseList
    private var greenhouseList: ArrayList<GHControllerModel> = arrayListOf()
    private var selectedId: Int = 0

    private val sharedPreferencesRepository: SharedPreferencesRepository by lazy {
        SharedPreferencesRepository(requireContext())
    }
    private val viewModel: MyGreenhousesViewModel by viewModels {
        MyGreenhousesViewModelFactory(sharedPreferencesRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_greenhouses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        greenhouseRecyclerViewRef = view.findViewById(R.id.greenhouse_recycler_view)
        greenhouseRecyclerViewRef.layoutManager = layoutManager
        greenhouseRecyclerViewRef.setHasFixedSize(false)

        adapterGreenhouseList = AdapterGreenhouseList(greenhouseList, this)
        greenhouseRecyclerViewRef.adapter = adapterGreenhouseList

        // Observe the LiveData from ViewModel
        viewModel.greenhouseList.observe(viewLifecycleOwner) { greenhouses ->
            greenhouseList.clear()
            greenhouseList.addAll(greenhouses)
            adapterGreenhouseList.notifyDataSetChanged()
        }

        dataInit()
    }

    private fun dataInit() {
        val userId = sharedPreferencesRepository.getUserId()

        if (userId != -1) {
            viewModel.fetchGreenhouses(userId)
        } else {
            Snackbar.make(requireView(), "User ID not found", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onItemClick(greenhouse: GHControllerModel) {
        showEditGreenhouseDialog(greenhouse)
    }

    private fun showEditGreenhouseDialog(greenhouse: GHControllerModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_greenhouse, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val editTextName = dialogView.findViewById<EditText>(R.id.editTextName)
        editTextName.setText(greenhouse.name)

        val editTextMinThreshold = dialogView.findViewById<EditText>(R.id.editTextMinThreshold)
        editTextMinThreshold.setText(greenhouse.minThreshold.toString())

        val editTextMaxThreshold = dialogView.findViewById<EditText>(R.id.editTextMaxThreshold)
        editTextMaxThreshold.setText(greenhouse.maxThreshold.toString())

        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val buttonDelete = dialogView.findViewById<Button>(R.id.buttonDelete)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonWiFi = dialogView.findViewById<Button>(R.id.buttonWiFi)

        val headerTextView = dialogView.findViewById<TextView>(R.id.textViewTemperatureSensorsHeader)
        val scrollViewCheckboxes = dialogView.findViewById<ScrollView>(R.id.scrollViewTemperatureSensorsCheckboxes)
        val textViewThresholdsHeader = dialogView.findViewById<TextView>(R.id.textViewThresholdsHeader)
        val linearLayoutThresholdInputs = dialogView.findViewById<LinearLayout>(R.id.linearLayoutThresholdInputs)

        val checkBoxTemperatures = mutableListOf<CheckBox>()

        for (i in 1..16) {
            val checkBoxId = resources.getIdentifier("checkBoxTemperature$i", "id", requireContext().packageName)
            val checkBox = dialogView.findViewById<CheckBox>(checkBoxId)
            checkBox?.let {
                checkBoxTemperatures.add(it)
            }
        }

        // Load the existing components and set the checkboxes accordingly
        RetrofitClient.componentService.getComponentsByControllerId(greenhouse.id!!)
            .enqueue(object : Callback<List<ComponentModel>> {
                override fun onResponse(
                    call: Call<List<ComponentModel>>, response: Response<List<ComponentModel>>
                ) {
                    if (response.isSuccessful) {
                        val components = response.body() ?: emptyList()
                        components.forEachIndexed { index, component ->
                            if (index < checkBoxTemperatures.size) {
                                checkBoxTemperatures[index].isChecked = component.active
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<ComponentModel>>, t: Throwable) {
                    // Handle failure
                }
            })

        buttonSave.setOnClickListener {
            val name = editTextName.text.toString()
            val minThreshold = editTextMinThreshold.text.toString().toDoubleOrNull()
            val maxThreshold = editTextMaxThreshold.text.toString().toDoubleOrNull()

            if (name.isNotEmpty()) {
                val components = checkBoxTemperatures.mapIndexed { _, checkBox ->
                    val componentName = checkBox.text.toString()
                    ComponentModel(
                        name = componentName,
                        controllerId = greenhouse.id,
                        type = ComponentModel.ComponentType.TEMPERATURE_SENSOR,
                        active = checkBox.isChecked
                    )
                }

                if (minThreshold != null && maxThreshold != null) {
                    viewModel.updateGreenhouse(
                        greenhouse, name, components, minThreshold, maxThreshold
                    )
                    dialog.dismiss()
                } else {
                    Snackbar.make(
                        requireView(), "Please enter valid thresholds", Snackbar.LENGTH_LONG
                    ).show()
                }
            } else {
                Snackbar.make(requireView(), "Please enter a name", Snackbar.LENGTH_LONG).show()
            }
        }

        buttonDelete.setOnClickListener {
            viewModel.deleteGreenhouse(greenhouse)
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        buttonWiFi.setOnClickListener {
            dialog.dismiss()
            selectedId = greenhouse.id
            showAvailableDevicesDialog()
        }

        headerTextView.setOnClickListener {
            scrollViewCheckboxes.visibility = if (scrollViewCheckboxes.visibility == View.GONE) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        textViewThresholdsHeader.setOnClickListener {
            linearLayoutThresholdInputs.visibility = if (linearLayoutThresholdInputs.visibility == View.GONE) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        dialog.show()
    }

    @SuppressLint("MissingPermission")
    private fun showAvailableDevicesDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_available_devices, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val recyclerViewDevices = dialogView.findViewById<RecyclerView>(R.id.recyclerViewDevices)
        recyclerViewDevices.layoutManager = LinearLayoutManager(requireContext())

        // Get Wi-Fi Manager
        val wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            Snackbar.make(requireView(), "Wi-Fi is disabled. Please enable it to scan networks.", Snackbar.LENGTH_LONG).show()
            return
        }

        // Start a scan for Wi-Fi networks
        wifiManager.startScan()
        val scanResults = wifiManager.scanResults

        // Filter networks starting with "ESP"
        val espNetworks = scanResults.filter { it.SSID.startsWith("ESP") }

        // Convert scan results to a list of Device objects
        val devices = espNetworks.map { Device(SSID = it.SSID, BSSID = it.BSSID) }

        // Set up the adapter with the list of devices
        val deviceAdapter = DeviceAdapter(devices) { selectedDevice ->
            Snackbar.make(requireView(), "Selected Network: ${selectedDevice.SSID}", Snackbar.LENGTH_LONG).show()
            // Implement your logic here for what happens when a network is selected
            connectToSelectedNetwork(selectedDevice.SSID)
            dialog.dismiss()
        }
        recyclerViewDevices.adapter = deviceAdapter

        dialog.show()
    }

    private fun connectToSelectedNetwork(ssid: String) {
        val wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .build()

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()

        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // Bind the process to the network (useful for network requests)
                connectivityManager.bindProcessToNetwork(network)
                Snackbar.make(requireView(), "Connected to $ssid", Snackbar.LENGTH_LONG).show()
                showSSIDPasswordDialog()
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Snackbar.make(requireView(), "Failed to connect to $ssid", Snackbar.LENGTH_LONG).show()
            }
        }
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    private fun showSSIDPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_ssid_password, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val editTextSSID = dialogView.findViewById<EditText>(R.id.editTextSSID)
        val editTextPassword = dialogView.findViewById<EditText>(R.id.editTextPassword)
        val buttonConnect = dialogView.findViewById<Button>(R.id.buttonConnect)

        editTextSSID.setText("")

        buttonConnect.setOnClickListener {
            val ssid = editTextSSID.text.toString()
            val password = editTextPassword.text.toString()

            if (ssid.isNotEmpty() && password.isNotEmpty()) {
                sendWiFiCredentialsToESP(ssid, password)
                dialog.dismiss()
            } else {
                Snackbar.make(requireView(), "Please enter both SSID and Password", Snackbar.LENGTH_LONG).show()
            }
        }

        dialog.show()
    }

    private fun sendWiFiCredentialsToESP(ssid: String, password: String) {
        RetrofitESPClient.espService.sendId(selectedId.toString()).enqueue(object :Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {

            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
            }

        })
        RetrofitESPClient.espService.sendCredentials(ssid, password).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Snackbar.make(requireView(), "Wi-Fi credentials sent successfully", Snackbar.LENGTH_LONG).show()
                } else {
                    Snackbar.make(requireView(), "Failed to send Wi-Fi credentials", Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Snackbar.make(requireView(), "Error: ${t.message}", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        dataInit()
    }
}


