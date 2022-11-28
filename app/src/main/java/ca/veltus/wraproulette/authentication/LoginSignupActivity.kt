package ca.veltus.wraproulette.authentication

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import ca.veltus.wraproulette.databinding.ActivityLoginSignupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginSignupActivity : AppCompatActivity() {

    companion object {
        const val TAG = "LoginActivityLog"
    }

    private val viewModel: LoginSignupViewModel by viewModels()

    private lateinit var binding: ActivityLoginSignupBinding

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityLoginSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set activity as fullscreen by hiding the status bar, bottom navigation bar and action bar
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        supportActionBar?.hide()
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}