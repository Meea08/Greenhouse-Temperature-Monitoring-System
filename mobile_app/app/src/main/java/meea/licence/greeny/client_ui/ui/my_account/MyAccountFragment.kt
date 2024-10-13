package meea.licence.greeny.client_ui.ui.my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import meea.licence.greeny.R
import meea.licence.greeny.SharedPreferencesRepository
import meea.licence.greeny.databinding.FragmentMyAccountBinding
import meea.licence.greeny.model.UserModel

class MyAccountFragment : Fragment() {

    private var _binding: FragmentMyAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var myAccountViewModel: MyAccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the SharedPreferencesRepository
        val sharedPreferencesRepository = SharedPreferencesRepository(requireContext())

        // Create the ViewModel using the ViewModelFactory
        myAccountViewModel = ViewModelProvider(this, MyAccountViewModelFactory(sharedPreferencesRepository))
            .get(MyAccountViewModel::class.java)

        // Set up click listeners for buttons
        binding.buttonChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_myAccountFragment_to_changePasswordFragment)
        }

        binding.buttonUpdateData.setOnClickListener {
            val firstName = binding.editTextFirstName.text.toString()
            val lastName = binding.editTextLastName.text.toString()
            val username = binding.editTextUsername.text.toString()
            val email = binding.editTextEmail.text.toString()

            val userId = sharedPreferencesRepository.getUserId()

            if (userId != -1) {
                val updatedUserData = UserModel(firstName, lastName, username, email)
                myAccountViewModel.updateUser(updatedUserData, userId) { updatedUser ->
                    if (updatedUser != null) {
                        // Update successful, handle UI changes
                        myAccountViewModel.setUserData(firstName, lastName, username, email)
                    } else {
                        // Update failed, handle error
                        myAccountViewModel.setUserData(firstName, lastName, "Update failed", email)
                    }
                }
            }
        }

        // Load user data from SharedPreferences using SharedPreferencesRepository
        val firstName = sharedPreferencesRepository.getFirstName()
        val lastName = sharedPreferencesRepository.getLastName()
        val username = sharedPreferencesRepository.getUsername()
        val email = sharedPreferencesRepository.getEmail()

        // Set in the ViewModel user data
        myAccountViewModel.setUserData(firstName ?: "", lastName ?: "", username ?: "", email ?: "")

        // Observe ViewModel LiveData
        myAccountViewModel.userData.observe(viewLifecycleOwner) { userData ->
            binding.editTextFirstName.setText(userData.firstname)
            binding.editTextLastName.setText(userData.lastname)
            binding.editTextUsername.setText(userData.username)
            binding.editTextEmail.setText(userData.email)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
