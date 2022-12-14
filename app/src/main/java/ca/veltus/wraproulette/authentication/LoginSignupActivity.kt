package ca.veltus.wraproulette.authentication

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import ca.veltus.wraproulette.databinding.ActivityLoginSignupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginSignupActivity : AppCompatActivity() {

    private val viewModel: LoginSignupViewModel by viewModels()

    private lateinit var binding: ActivityLoginSignupBinding

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Wait until the loginFlow value has been returned before removing the SplashScreen
        splashScreen.setKeepOnScreenCondition { viewModel.loginFlow.value != null }

        // Hide the status bar, toolbar and bottom navigation bar for all fragments in the activity.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /**
     * This public function allows any fragment located in the activity to close the keyboard when
     * finished typing and the submit button is clicked.
     */
    fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}