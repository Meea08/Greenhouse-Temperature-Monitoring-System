package meea.licence.greeny.client_ui.ui.change_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import meea.licence.greeny.SharedPreferencesRepository

class ChangePasswordViewModelFactory(
    private val repository: SharedPreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChangePasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChangePasswordViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
