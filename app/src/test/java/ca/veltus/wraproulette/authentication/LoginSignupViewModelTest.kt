package ca.veltus.wraproulette.authentication

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import ca.veltus.wraproulette.WrapRouletteApplication
import ca.veltus.wraproulette.data.ErrorMessage
import ca.veltus.wraproulette.data.repository.FakeAuthenticationRepository
import ca.veltus.wraproulette.utils.StringResourcesProvider
import ca.veltus.wraproulette.utils.network.NetworkConnectivityObserver
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(AndroidJUnit4::class)
class LoginSignupViewModelTest {

    private lateinit var viewModel: LoginSignupViewModel
    private lateinit var app: Application
    private lateinit var connectivityObserver: NetworkConnectivityObserver
    private lateinit var stringResourcesProvider: StringResourcesProvider
    private val repository = FakeAuthenticationRepository()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        app = getApplicationContext<WrapRouletteApplication>()
        connectivityObserver = NetworkConnectivityObserver(app)
        stringResourcesProvider = StringResourcesProvider(app)
        FirebaseApp.initializeApp(app)
        viewModel =
            LoginSignupViewModel(repository, connectivityObserver, stringResourcesProvider, app)
    }


    @Test
    fun `resetPassword function, returns error when email is blank`() = runBlocking {
        viewModel.emailAddress.postValue("  ")

        viewModel.resetPassword()

        assertThat(viewModel.errorEmailText.value).isEqualTo(ErrorMessage.ErrorText("Please enter your email address."))
    }


    @Test
    fun `validateEmailAndPassword function, returns error when email is blank`() {
        viewModel.emailAddress.postValue("  ")

        val result = viewModel.validateEmailAndPassword()

        assertThat(viewModel.errorEmailText.value).isEqualTo(ErrorMessage.ErrorText("Please enter your email address."))
        assertThat(result).isEqualTo(false)
    }
}