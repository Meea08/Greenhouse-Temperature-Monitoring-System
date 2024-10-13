package meea.licence.greeny.client_ui.ui.my_greenhouses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import meea.licence.greeny.SharedPreferencesRepository

class MyGreenhousesViewModelFactory(
    private val repository: SharedPreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyGreenhousesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyGreenhousesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
