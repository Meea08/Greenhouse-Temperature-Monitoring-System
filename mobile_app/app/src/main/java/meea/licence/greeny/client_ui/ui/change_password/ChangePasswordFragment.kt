package meea.licence.greeny.client_ui.ui.change_password

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import meea.licence.greeny.SharedPreferencesRepository
import meea.licence.greeny.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var changePasswordViewModel: ChangePasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Create the SharedPreferencesRepository instance
        val sharedPreferencesRepository = SharedPreferencesRepository(requireContext())

        // Create the ViewModel using the ViewModelFactory
        changePasswordViewModel =
            ViewModelProvider(this, ChangePasswordViewModelFactory(sharedPreferencesRepository))
                .get(ChangePasswordViewModel::class.java)

        binding.buttonSubmitChangePassword.setOnClickListener {
            Log.d("ChangePasswordFragment", "Submit button clicked")

            val oldPassword = binding.editTextOldPassword.text.toString()
            val newPassword = binding.editTextNewPassword.text.toString()
            val confirmPassword = binding.editTextConfirmNewPassword.text.toString()

            if (newPassword != confirmPassword) {
                Snackbar.make(view, "Passwords do not match", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (newPassword.isEmpty() || oldPassword.isEmpty()) {
                Snackbar.make(view, "All fields are required", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val userId = sharedPreferencesRepository.getUserId()
            Log.d("ChangePasswordFragment", "User ID: $userId")

            if (userId != -1) {
                // Call ViewModel to change the password
                changePasswordViewModel.changePassword(
                    userId,
                    oldPassword,
                    newPassword
                ) { success ->
                    if (success) {
                        Snackbar.make(view, "Password changed successfully", Snackbar.LENGTH_LONG)
                            .show()
                    } else {
                        Snackbar.make(view, "Password change failed", Snackbar.LENGTH_LONG).show()
                    }
                }
            } else {
                Log.e("ChangePasswordFragment", "Invalid user ID")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


