package ca.veltus.wraproulette.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import ca.veltus.wraproulette.R
import ca.veltus.wraproulette.authentication.LoginSignupActivity
import ca.veltus.wraproulette.authentication.LoginSignupViewModel
import ca.veltus.wraproulette.databinding.ActivityWrapRouletteBinding
import ca.veltus.wraproulette.databinding.FeedbackDialogBinding
import ca.veltus.wraproulette.utils.FirebaseStorageUtil
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.RuntimeExecutionException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WrapRouletteActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "WrapRouletteActivity"
    }

    private val viewModel: LoginSignupViewModel by viewModels()
    private val manager by lazy { ReviewManagerFactory.create(this) }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityWrapRouletteBinding
    private lateinit var drawerLayout: DrawerLayout

    private val navController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navHostFragment.navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWrapRouletteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationAndToolbar()

        requestReviewInfo()

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            @Suppress("DEPRECATION") super.onBackPressed()
        }
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun requestReviewInfo() {
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                startReviewFlow(reviewInfo)
            } else {
                when (val exception = task.exception) {
                    is ReviewException -> {
//                        Log.e(TAG, "requestReviewInfo: ${exception.errorCode}")
                        Firebase.crashlytics.recordException(exception)
                    }
                    is RuntimeExecutionException -> {
//                        Log.e(TAG, "requestReviewInfo: ${exception.message}")
                        Firebase.crashlytics.recordException(exception)
                    }
                    else -> {
//                        Log.e(TAG, "requestReviewInfo: ${-9999}")
                        Firebase.crashlytics.log(exception?.message.toString())
                    }
                }
            }
        }
    }

    private fun startReviewFlow(reviewInfo: ReviewInfo?) {
        if (reviewInfo != null) {
            manager.launchReviewFlow(this, reviewInfo)
        }
    }

    private fun setupNavigationAndToolbar() {
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navHostFragmentMain

        setSupportActionBar(binding.appBarMain.toolbar)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_pools, R.id.nav_account
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.addPoolFragment || destination.id == R.id.joinPoolFragment) {
                drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
            } else {
                drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED)
            }
        }

        navView.menu.apply {
            findItem(R.id.nav_feedback).setOnMenuItemClickListener {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawers()
                }
                launchFeedbackDialog()
                true
            }
            findItem(R.id.nav_logout).setOnMenuItemClickListener {
                viewModel.logout()
                startActivity(
                    Intent(
                        this@WrapRouletteActivity, LoginSignupActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
                true
            }
            findItem(R.id.nav_invite).setOnMenuItemClickListener {
                sendInviteIntent()
                true
            }
        }


        // Get Firebase current user data and display it in header.
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userAccount.collectLatest {
                    if (it != null) {
                        navView.getHeaderView(0).apply {
                            findViewById<TextView>(R.id.userNameHeaderTextView).text =
                                it.displayName
                            findViewById<TextView>(R.id.userDepartmentHeaderTextView).text =
                                it.department
                        }

                        if (it.profilePicturePath != null) {
                            Glide.with(this@WrapRouletteActivity).asBitmap()
                                .load(FirebaseStorageUtil.pathToReference(it.profilePicturePath))
                                .placeholder(R.drawable.ic_baseline_account_circle_24).into(
                                    navView.getHeaderView(0)
                                        .findViewById(R.id.profileHeaderImageView)
                                )
                        }
                    }
                }
            }
        }
    }

    private fun sendInviteIntent() {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.inviteEmailIntentSubject))
            var shareMessage = getString(R.string.inviteEmailIntentMessage)
            shareMessage =
                shareMessage + "https://play.google.com/store/apps/details?id=" + packageName
            intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(intent, getString(R.string.inviteIntentTitle)))
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun launchFeedbackDialog() {
        viewModel.showToast.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        val builder = MaterialAlertDialogBuilder(
            this@WrapRouletteActivity,
            R.style.NumberPickerDialog_MaterialComponents_MaterialAlertDialog
        )
        val view = FeedbackDialogBinding.inflate(LayoutInflater.from(this@WrapRouletteActivity))
        view.viewModel = viewModel

        builder.apply {
            setView(view.root)
            setPositiveButton(getString(R.string.send)) { _, _ -> viewModel.sendFeedbackMessage() }
        }
        val dialog = builder.show()
        dialog.apply {
            getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (!viewModel.sendFeedbackMessage()) Toast.makeText(
                    this@WrapRouletteActivity, getString(
                        R.string.feedbackDialogEmptyErrorToast
                    ), Toast.LENGTH_SHORT
                ).show()
                else {
                    Toast.makeText(
                        this@WrapRouletteActivity,
                        getString(R.string.feedbackDialogSuccessToast),
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
            }
            dialog.dismiss()
        }
        dialog.show()
    }
}