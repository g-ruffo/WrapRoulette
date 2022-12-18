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
        viewModel.emailAddress.value = "  "

        viewModel.resetPassword()

        assertThat(viewModel.errorEmailText.value).isEqualTo(ErrorMessage.ErrorText("Please enter your email address."))
        assertThat(viewModel.showLoading.value).isEqualTo(false)
    }

    @Test
    fun `resetPassword function, returns error when email is not proper format`() = runBlocking {
        viewModel.emailAddress.value = "John.Doe@google"

        viewModel.resetPassword()

        assertThat(viewModel.errorEmailText.value).isEqualTo(ErrorMessage.ErrorText("Email address is not valid."))
        assertThat(viewModel.showLoading.value).isEqualTo(false)

    }


    @Test
    fun `validateEmailAndPassword function, returns error when email is blank`() {
        viewModel.emailAddress.value = "  "

        val result = viewModel.validateEmailAndPassword()

        assertThat(viewModel.errorEmailText.value).isEqualTo(ErrorMessage.ErrorText("Please enter your email address."))
        assertThat(result).isEqualTo(false)
        assertThat(viewModel.showLoading.value).isEqualTo(false)
    }

    @Test
    fun `validateEmailAndPassword function, returns error when email is not proper format`() {
        viewModel.emailAddress.value = "John.Doe@google     "

        val result = viewModel.validateEmailAndPassword()

        assertThat(viewModel.errorEmailText.value).isEqualTo(ErrorMessage.ErrorText("Email address is not valid."))
        assertThat(viewModel.emailAddress.value).isEqualTo("John.Doe@google")
        assertThat(result).isEqualTo(false)
        assertThat(viewModel.showLoading.value).isEqualTo(false)
    }

    @Test
    fun `validateEmailAndPassword function, returns error when password is blank`() {
        viewModel.password.value = "  "
        viewModel.emailAddress.value = "John.Doe@google.com"

        val result = viewModel.validateEmailAndPassword()

        assertThat(viewModel.errorPasswordText.value).isEqualTo(ErrorMessage.ErrorText("Please enter a password."))
        assertThat(result).isEqualTo(false)
        assertThat(viewModel.showLoading.value).isEqualTo(false)
    }

    @Test
    fun `validateEmailAndPassword function, returns error when username is blank`() {
        viewModel.username.value = "  "
        viewModel.password.value = "password"
        viewModel.emailAddress.value = "John.Doe@google.com"

        val result = viewModel.validateEmailAndPassword(true)

        assertThat(viewModel.errorNameText.value).isEqualTo(ErrorMessage.ErrorText("Please enter your name."))
        assertThat(result).isEqualTo(false)
        assertThat(viewModel.showLoading.value).isEqualTo(false)
    }

    @Test
    fun `validateEmailAndPassword function, returns error when department is blank`() {
        viewModel.username.value = "John"
        viewModel.password.value = "password"
        viewModel.emailAddress.value = "John.Doe@google.com"
        viewModel.department.value = "   "

        val result = viewModel.validateEmailAndPassword(true)

        assertThat(viewModel.errorDepartmentText.value).isEqualTo(ErrorMessage.ErrorText("Please enter your department."))
        assertThat(result).isEqualTo(false)
        assertThat(viewModel.showLoading.value).isEqualTo(false)
    }

    @Test
    fun `validateUpdateCurrentUser function, returns error when username is blank`() {
        viewModel.updateDepartment.value = ""

        viewModel.validateUpdateCurrentUser()

        assertThat(viewModel.errorNameText.value).isEqualTo(ErrorMessage.ErrorText("Please enter your name."))
        assertThat(viewModel.showLoading.value).isEqualTo(false)
    }

    @Test
    fun `validateUpdateCurrentUser function, returns error when department is blank`() {
        viewModel.updateUsername.value = "John"
        viewModel.updateDepartment.value = ""

        viewModel.validateUpdateCurrentUser()

        assertThat(viewModel.errorDepartmentText.value).isEqualTo(ErrorMessage.ErrorText("Please enter your department."))
        assertThat(viewModel.showLoading.value).isEqualTo(false)
    }

    @Test
    fun `sendFeedbackMessage function, returns false when message is blank`() {
        viewModel.feedbackMessage.value = "    "

        val result = viewModel.sendFeedbackMessage()

        assertThat(result).isEqualTo(false)
        assertThat(viewModel.showLoading.value).isEqualTo(false)
    }

}