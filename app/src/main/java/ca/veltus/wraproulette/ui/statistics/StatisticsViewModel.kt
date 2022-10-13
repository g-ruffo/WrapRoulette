package ca.veltus.wraproulette.ui.statistics

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ca.veltus.wraproulette.base.BaseViewModel
import ca.veltus.wraproulette.data.repository.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(private val repository: AuthenticationRepository, app: Application) :
    BaseViewModel(app) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is slideshow Fragment"
    }
    val text: LiveData<String> = _text
}