package meea.licence.greeny.client_ui.ui.my_greenhouses

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import meea.licence.greeny.R

class DeviceAdapter(
    private val devices: List<Device>,
    private val onDeviceSelected: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.textViewDeviceName)
        val deviceIP: TextView = view.findViewById(R.id.textViewDeviceIP)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_list_item, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = device.SSID
        holder.deviceIP.text = device.BSSID

        Log.d("DeviceAdapter", "Binding device at position $position: ${device.SSID}, ${device.BSSID}")

        holder.itemView.setOnClickListener {
            onDeviceSelected(device)
        }
    }


    override fun getItemCount(): Int {
        return devices.size
    }
}
