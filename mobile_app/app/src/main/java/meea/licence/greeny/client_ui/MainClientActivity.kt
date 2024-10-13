package meea.licence.greeny.client_ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import meea.licence.greeny.R
import meea.licence.greeny.SharedPreferencesRepository
import meea.licence.greeny.databinding.ActivityMainClientBinding
import meea.licence.greeny.model.ComponentModel
import meea.licence.greeny.model.GHControllerModel
import meea.licence.greeny.network.GHControllerService
import meea.licence.greeny.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainClientActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainClientBinding
    private lateinit var sharedPreferencesRepository: SharedPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMainClient.toolbar)

        binding.appBarMainClient.fab.setOnClickListener {
            showCreateGreenhouseDialog()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main_client)
        sharedPreferencesRepository = SharedPreferencesRepository(this)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_my_account,
                R.id.nav_my_greenhouses,
                R.id.nav_greenhouse_data,
                R.id.nav_logout
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Set user info in nav header
        setUserInfoInNavHeader(navView)
    }


    companion object {
        private const val REQUEST_CODE_LOCATION_PERMISSION = 1
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_client, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main_client)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setUserInfoInNavHeader(navView: NavigationView) {
        val headerView: View = navView.getHeaderView(0)
        val nameTextView: TextView = headerView.findViewById(R.id.textViewName)
        val emailTextView: TextView = headerView.findViewById(R.id.textViewEmail)

        // Retrieve data from SharedPreferences
        val firstName = sharedPreferencesRepository.getFirstName()
        val lastName = sharedPreferencesRepository.getLastName()
        val email = sharedPreferencesRepository.getEmail()

        // Set the text
        nameTextView.text = buildString {
        append(firstName)
        append(" ")
        append(lastName)
    }
        emailTextView.text = email
    }

    private fun showCreateGreenhouseDialog() {
        // Create the dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_greenhouse, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(true).create()

        // Get references to the EditTexts and Buttons in the dialog
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextName)
        val buttonSave = dialogView.findViewById<Button>(R.id.buttonSave)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)

        val textViewTemperatureSensorsHeader =
            dialogView.findViewById<TextView>(R.id.textViewTemperatureSensorsHeader)
        val scrollViewTemperatureSensorsCheckboxes =
            dialogView.findViewById<View>(R.id.scrollViewTemperatureSensorsCheckboxes)

        val textViewThresholdsHeader =
            dialogView.findViewById<TextView>(R.id.textViewThresholdsHeader)
        val linearLayoutThresholdInputs =
            dialogView.findViewById<LinearLayout>(R.id.linearLayoutThresholdInputs)

        val editTextMinThreshold = dialogView.findViewById<EditText>(R.id.editTextMinThreshold)
        val editTextMaxThreshold = dialogView.findViewById<EditText>(R.id.editTextMaxThreshold)

        val checkBoxTemperatures = mutableListOf<CheckBox>()

        for (i in 1..16) {
            val checkBoxId = resources.getIdentifier("checkBoxTemperature$i", "id", packageName)
            val checkBox = dialogView.findViewById<CheckBox>(checkBoxId)
            checkBox?.let {
                checkBoxTemperatures.add(it)
            }
        }

        // Set click listener for the Save button
        buttonSave.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val minThreshold = editTextMinThreshold.text.toString().trim()
            val maxThreshold = editTextMaxThreshold.text.toString().trim()

            if (name.isNotEmpty()) {
                val selectedComponents = mutableListOf<String>()

                for (i in 1..16) {
                    val checkBoxId =
                        resources.getIdentifier("checkBoxTemperature$i", "id", packageName)
                    val checkBox = dialogView.findViewById<CheckBox>(checkBoxId)
                    checkBox?.let {
                        if (it.isChecked) selectedComponents.add(it.text.toString())
                    }
                }

                createGreenhouse(
                    name, minThreshold.toDouble(), maxThreshold.toDouble(), selectedComponents
                )

                dialog.dismiss()
            } else {
//                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                Snackbar.make(this, binding.root, "Please fill in all fields", Snackbar.LENGTH_LONG).show()
            }
        }

        // Set click listener for the Cancel button
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        textViewTemperatureSensorsHeader.setOnClickListener {
            if (scrollViewTemperatureSensorsCheckboxes.visibility == View.GONE) {
                scrollViewTemperatureSensorsCheckboxes.visibility = View.VISIBLE
            } else {
                scrollViewTemperatureSensorsCheckboxes.visibility = View.GONE
            }
        }

        textViewThresholdsHeader.setOnClickListener {
            if (linearLayoutThresholdInputs.visibility == View.GONE) {
                linearLayoutThresholdInputs.visibility = View.VISIBLE
            } else {
                linearLayoutThresholdInputs.visibility = View.GONE
            }
        }

        // Show the dialog
        dialog.show()
    }

    private fun createGreenhouse(
        name: String, minThreshold: Double, maxThreshold: Double, components: List<String>
    ) {

        val userId = sharedPreferencesRepository.getUserId()
        val token = sharedPreferencesRepository.getToken()

        if (userId != -1) {
            val newGreenhouse = GHControllerModel(
                name = name,
                userId = userId,
                minThreshold = minThreshold,
                maxThreshold = maxThreshold
            )
            token?.let {
            val ghControllerService = RetrofitClient.getRetrofitClient(this, token).create(GHControllerService::class.java)
                ghControllerService.createController(newGreenhouse)
                    .enqueue(object : Callback<GHControllerModel> {
                        override fun onResponse(
                            call: Call<GHControllerModel>, response: Response<GHControllerModel>
                        ) {
                            if (response.isSuccessful) {
                                val createdGreenhouse = response.body()
                                if (createdGreenhouse != null) {
                                    createdGreenhouse.id?.let {
                                        addComponentsToGreenhouse(
                                            it, components
                                        )
                                    }
                                }

                                Snackbar.make(
                                    binding.root,
                                    "Greenhouse created successfully!",
                                    Snackbar.LENGTH_LONG
                                ).show()

                            } else {
                                Snackbar.make(
                                    binding.root, "Failed to create greenhouse", Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<GHControllerModel>, t: Throwable) {
                            Snackbar.make(binding.root, "Error: ${t.message}", Snackbar.LENGTH_LONG)
                                .show()
                        }
                    })


            }

        } else {
            Snackbar.make(binding.root, "User ID not found", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun addComponentsToGreenhouse(
        controllerId: Int, selectedComponentStrings: List<String>
    ) {
        val components = mutableListOf<ComponentModel>()

        // Loop through all possible components (Temperature Sensors 1 to 16)
        for (i in 1..16) {
            val componentName = "Temperature Sensor $i"
            val isActive = selectedComponentStrings.contains(componentName)

            val component = ComponentModel(
                name = componentName,
                controllerId = controllerId,
                type = ComponentModel.ComponentType.TEMPERATURE_SENSOR,
                active = isActive // Set active based on selection
            )
            components.add(component)
        }

        RetrofitClient.componentService.createComponents(components)
            .enqueue(object : Callback<List<ComponentModel>> {
                override fun onResponse(
                    call: Call<List<ComponentModel>>, response: Response<List<ComponentModel>>
                ) {
                    if (response.isSuccessful) {
                        Snackbar.make(
                            binding.root, "Components added successfully!", Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        Snackbar.make(
                            binding.root, "Failed to add components", Snackbar.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<ComponentModel>>, t: Throwable) {
                    Snackbar.make(binding.root, "Error: ${t.message}", Snackbar.LENGTH_LONG).show()
                }
            })
    }
}
