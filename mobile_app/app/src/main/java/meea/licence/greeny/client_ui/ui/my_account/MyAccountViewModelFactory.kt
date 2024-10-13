package meea.licence.greeny.client_ui.ui.my_account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import meea.licence.greeny.SharedPreferencesRepository

class MyAccountViewModelFactory(
    private val repository: SharedPreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyAccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyAccountViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
