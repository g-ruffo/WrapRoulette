package ca.veltus.wraproulette.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
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
import ca.veltus.wraproulette.utils.FirestoreUtil
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WrapRouletteActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "WrapRouletteActivity"
    }

    private val viewModel: LoginSignupViewModel by viewModels()

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityWrapRouletteBinding

    val db = Firebase.firestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWrapRouletteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)


        setupNavigationAndToolbar()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupNavigationAndToolbar() {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_pools, R.id.nav_statistics, R.id.nav_account
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Get Firebase current user data and display it in header.
        FirestoreUtil.getCurrentUser { user ->
            navView.getHeaderView(0).findViewById<TextView>(R.id.userNameHeaderTextView).text = user.displayName
            navView.getHeaderView(0).findViewById<TextView>(R.id.userEmailHeaderTextView).text = user.email

                if (user.profilePicturePath != null) {
                    Glide.with(this).load(FirebaseStorageUtil.pathToReference(user.profilePicturePath))
                        .placeholder(R.drawable.ic_baseline_account_circle_24)
                        .into(navView.getHeaderView(0).findViewById<ImageView>(R.id.profileHeaderImageView))
                }
            }


        navView.menu.findItem(R.id.logoutMenu).setOnMenuItemClickListener {
            viewModel.logout()
            startActivity(
                Intent(this, LoginSignupActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
            true
        }
    }
}