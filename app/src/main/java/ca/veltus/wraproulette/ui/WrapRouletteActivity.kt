package ca.veltus.wraproulette.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
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
import ca.veltus.wraproulette.utils.FirebaseStorageUtil
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
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

        // Get Firebase current user data and display it in header.
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userAccount.collectLatest {
                    if (it != null) {
                        navView.getHeaderView(0)
                            .findViewById<TextView>(R.id.userNameHeaderTextView).text =
                            it.displayName
                        navView.getHeaderView(0)
                            .findViewById<TextView>(R.id.userEmailHeaderTextView).text = it.email

                        if (it.profilePicturePath != null) {
                            Glide.with(this@WrapRouletteActivity)
                                .load(FirebaseStorageUtil.pathToReference(it.profilePicturePath))
                                .placeholder(R.drawable.ic_baseline_account_circle_24).into(
                                    navView.getHeaderView(0)
                                        .findViewById<ImageView>(R.id.profileHeaderImageView)
                                )
                        }
                    }
                }
            }
        }


        navView.menu.findItem(R.id.logoutMenu).setOnMenuItemClickListener {
            viewModel.logout()
            startActivity(
                Intent(
                    this,
                    LoginSignupActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
            true
        }
    }
}