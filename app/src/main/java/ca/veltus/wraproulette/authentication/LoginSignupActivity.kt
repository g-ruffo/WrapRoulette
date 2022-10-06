package ca.veltus.wraproulette.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import ca.veltus.wraproulette.data.Resource
import ca.veltus.wraproulette.databinding.ActivityLoginSignupBinding
import ca.veltus.wraproulette.ui.WrapRouletteActivity
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LoginSignupActivity : AppCompatActivity() {

    companion object {
        const val TAG = "LoginActivityLog"
    }

    private val viewModel: LoginSignupViewModel by viewModels()

    private lateinit var binding: ActivityLoginSignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set activity as fullscreen by hiding the status bar, bottom navigation bar and action bar
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        supportActionBar?.hide()

        observeAuthenticationState()


    }

    // Observe authentication state live data. Once authenticated, finish LoginActivity and navigate to the CrewCallerActivity.
    private fun observeAuthenticationState() {
        Log.i(TAG, "observeAuthenticationState: called")
        lifecycleScope.launchWhenStarted {
            viewModel.loginFlow.collectLatest {
                when (it) {
                    is Resource.Success -> {
                        Log.i(TAG, "observeAuthenticationState = Resource.Success")
                        Log.i(TAG, "AuthenticationState.AUTHENTICATED")
                        startActivity(
                            Intent(this@LoginSignupActivity, WrapRouletteActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                        finish()
                    }
                    is Resource.Loading -> {
                        Log.i(TAG, "observeAuthenticationState = Resource.Loading")
                        CircularProgressIndicator(this@LoginSignupActivity)
                    }
                    is Resource.Failure -> {
                        Log.i(TAG, "observeAuthenticationState = Resource.Failure")
                        viewModel.showToast.value = it.exception.message
                    }
                }
            }
        }

    }

}