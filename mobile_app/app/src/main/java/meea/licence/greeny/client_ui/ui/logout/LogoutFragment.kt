package meea.licence.greeny.client_ui.ui.logout

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import meea.licence.greeny.SharedPreferencesRepository
import meea.licence.greeny.authentication_ui.LogInMainActivity
import meea.licence.greeny.databinding.FragmentLogoutBinding

class LogoutFragment : Fragment() {

    private var _binding: FragmentLogoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogoutBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Perform logout when the view is created
        val sharedPreferencesRepository = SharedPreferencesRepository(requireContext())
        performLogout(sharedPreferencesRepository)

        return root
    }

    private fun performLogout(sharedPreferencesRepository: SharedPreferencesRepository) {
        clearUserData(sharedPreferencesRepository)
        navigateToLoginScreen()
    }

    private fun clearUserData(sharedPreferencesRepository: SharedPreferencesRepository) {
        sharedPreferencesRepository.clearUserData()
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(requireContext(), LogInMainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish() // Finish the current activity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
