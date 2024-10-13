package meea.licence.greeny.client_ui.ui.my_greenhouses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import meea.licence.greeny.MyApp
import meea.licence.greeny.SharedPreferencesRepository
import meea.licence.greeny.model.ComponentModel
import meea.licence.greeny.model.GHControllerModel
import meea.licence.greeny.network.ComponentService
import meea.licence.greeny.network.GHControllerService
import meea.licence.greeny.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyGreenhousesViewModel(private val repository: SharedPreferencesRepository) : ViewModel() {

    private val _greenhouseList = MutableLiveData<List<GHControllerModel>>()
    val greenhouseList: LiveData<List<GHControllerModel>> get() = _greenhouseList

    private val _selectedGreenhouseId = MutableLiveData<Int>()
    val selectedGreenhouseId: LiveData<Int> get() = _selectedGreenhouseId

    fun fetchGreenhouses(userId: Int) {
        viewModelScope.launch {
            val token = repository.getToken()
            token?.let {
                val ghControllerService =
                    RetrofitClient.getRetrofitClient(MyApp.getContext(), token).create(GHControllerService::class.java)
                ghControllerService.getControllerByUserId(userId)
                    .enqueue(object : Callback<List<GHControllerModel>> {
                        override fun onResponse(
                            call: Call<List<GHControllerModel>>,
                            response: Response<List<GHControllerModel>>
                        ) {
                            if (response.isSuccessful) {
                                _greenhouseList.value = response.body() ?: emptyList()
                            }
                        }

                        override fun onFailure(call: Call<List<GHControllerModel>>, t: Throwable) {
                            // Handle failure
                        }
                    })
            }
        }
    }

    fun updateGreenhouse(
        greenhouse: GHControllerModel,
        name: String,
        components: List<ComponentModel>,
        minThresholdNew: Double,
        maxThresholdNew: Double,
    ) {
        val updatedGreenhouse = GHControllerModel(
            id = greenhouse.id,
            name = name,
            userId = greenhouse.userId,
            minThreshold = minThresholdNew,
            maxThreshold = maxThresholdNew
        )

        viewModelScope.launch {
            val token = repository.getToken()
            token?.let {
                greenhouse.id?.let {
                    val ghControllerService = RetrofitClient.getRetrofitClient(MyApp.getContext(), token)
                        .create(GHControllerService::class.java)
                    ghControllerService.updateController(it, updatedGreenhouse)
                        .enqueue(object : Callback<GHControllerModel> {
                            override fun onResponse(
                                call: Call<GHControllerModel>, response: Response<GHControllerModel>
                            ) {
                                if (response.isSuccessful) {
                                    response.body()?.id?.let { controllerId ->
                                        updateComponentsForGreenhouse(controllerId, components)
                                    }
                                }
                            }

                            override fun onFailure(call: Call<GHControllerModel>, t: Throwable) {
                                // Handle failure
                            }
                        })
                }
            }
        }
    }

    private fun updateComponentsForGreenhouse(
        controllerId: Int,
        selectedComponents: List<ComponentModel>
    ) {
        viewModelScope.launch {
            val token = repository.getToken()
            token?.let {
                val componentService =
                    RetrofitClient.getRetrofitClient(MyApp.getContext(), token).create(ComponentService::class.java)
                componentService.updateComponentsByControllerId(controllerId, selectedComponents)
                    .enqueue(object : Callback<List<ComponentModel>> {
                        override fun onResponse(
                            call: Call<List<ComponentModel>>,
                            response: Response<List<ComponentModel>>
                        ) {
                            if (response.isSuccessful) {
                                val existingComponents = response.body() ?: emptyList()
                                val updatedComponents = existingComponents.map { component ->
                                    component.copy(active = selectedComponents.any { it.name == component.name && it.active })
                                }
                            }
                        }

                        override fun onFailure(call: Call<List<ComponentModel>>, t: Throwable) {
                            // Handle failure
                        }
                    })
            }
        }
    }

    fun deleteGreenhouse(greenhouse: GHControllerModel) {
        viewModelScope.launch {
            val token = repository.getToken()
            token?.let {
                greenhouse.id?.let {
                    val ghControllerService = RetrofitClient.getRetrofitClient(MyApp.getContext(),token)
                        .create(GHControllerService::class.java)
                    ghControllerService.deleteController(it)
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    fetchGreenhouses(greenhouse.userId)
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                // Handle failure
                            }
                        })
                }
            }
        }
    }

    fun setSelectedGreenhouseId(id: Int) {
        _selectedGreenhouseId.value = id
    }
}



