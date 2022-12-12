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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WrapRouletteActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "WrapRouletteActivity"
    }

    private val viewModel: LoginSignupViewModel by viewModels()

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityWrapRouletteBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWrapRouletteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigationAndToolbar()

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupNavigationAndToolbar() {
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

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

        navView.menu.apply {
            findItem(R.id.nav_feedback).setOnMenuItemClickListener {
                launchFeedbackDialog()
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawers()
                }
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
                Toast.makeText(this@WrapRouletteActivity, "Invite", Toast.LENGTH_SHORT).show()
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
            startActivity(Intent.createChooser(intent, "Share Via"))
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
            setPositiveButton("Send") { _, _ -> viewModel.sendFeedbackMessage() }
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