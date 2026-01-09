package pt.ipt.dam2025.nocrastination.ui.focuslocations.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.ipt.dam2025.nocrastination.databinding.ItemFocusLocationBinding
import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation

class FocusLocationAdapter(
    private val onEditClick: (FocusLocation) -> Unit,
    private val onDeleteClick: (FocusLocation) -> Unit,
    private val onToggleClick: (FocusLocation, Boolean) -> Unit
) : ListAdapter<FocusLocation, FocusLocationAdapter.FocusLocationViewHolder>(
    FocusLocationDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FocusLocationViewHolder {
        val binding = ItemFocusLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FocusLocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FocusLocationViewHolder, position: Int) {
        val location = getItem(position)
        holder.bind(location)
    }

    inner class FocusLocationViewHolder(
        private val binding: ItemFocusLocationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(location: FocusLocation) {
            binding.textLocationName.text = location.name
            binding.textLocationAddress.text = location.address
            binding.textLocationRadius.text = "Raio: ${location.radius}m"

            binding.switchEnabled.isChecked = location.enabled

            binding.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                onToggleClick(location, isChecked)
            }

            binding.buttonEdit.setOnClickListener {
                onEditClick(location)
            }

            binding.buttonDelete.setOnClickListener {
                onDeleteClick(location)
            }
        }
    }

    class FocusLocationDiffCallback : DiffUtil.ItemCallback<FocusLocation>() {
        override fun areItemsTheSame(oldItem: FocusLocation, newItem: FocusLocation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FocusLocation, newItem: FocusLocation): Boolean {
            return oldItem == newItem
        }
    }
}