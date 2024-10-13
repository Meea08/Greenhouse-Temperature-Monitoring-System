package meea.licence.greeny.client_ui.ui.my_greenhouses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import meea.licence.greeny.R
import meea.licence.greeny.model.GHControllerModel

class AdapterGreenhouseList(
    private val greenhouseList: List<GHControllerModel>,
    private val listener: OnItemClickListener):
        RecyclerView.Adapter<AdapterGreenhouseList.GreenhouseViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GreenhouseViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.list_element_greenhouse,
            parent, false
        )
        return GreenhouseViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return greenhouseList.size
    }

    override fun onBindViewHolder(holder: GreenhouseViewHolder, position: Int) {
        val currentGreenhouse = greenhouseList[position]
        holder.bind(currentGreenhouse)
    }

    inner class GreenhouseViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener{
        private val greenhouseNameTextView: TextView = itemView.findViewById(R.id.textViewGreenhouseName)
        private val greenhouseActiveTextView: TextView = itemView.findViewById(R.id.textViewActive)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(greenhouseList[position])
            }
        }

        fun bind(greenhouse: GHControllerModel) {
            greenhouseNameTextView.text = greenhouse.name
            greenhouseActiveTextView.text = buildString {
                append("min: ")
                append(greenhouse.minThreshold)
                append(" - max: ")
                append(greenhouse.maxThreshold)
            }
        }

    }
}