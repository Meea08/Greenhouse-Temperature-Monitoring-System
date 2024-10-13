package meea.licence.greeny.client_ui.ui.greenhouse_data

import androidx.lifecycle.ViewModelProvider
import meea.licence.greeny.SharedPreferencesRepository

class GreenhouseDataViewModelFactory(
    private val repository: SharedPreferencesRepository
) : ViewModelProvider.Factory{

    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(GreenhouseDataViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return GreenhouseDataViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
