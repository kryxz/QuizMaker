package com.lemonlab.quizmaker

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val vm: QuizzesVM by viewModels()
    val questionsVM: QuestionsVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        chooseTheme()
        setContentView(R.layout.activity_main)
        setUpNavigation()
        MobileAds.initialize(this)
        setUpFireBase()


        checkIfURL()
    }

    private fun checkIfURL() {
        val data: Uri? = intent?.data
        if (data != null) {
            val url = data.toString()
            val code = url.substring(url.indexOf("com/") + 4, url.length)
            vm.joinClassWithCode(this, code)
            questionsVM.setClassJoinCode(code)
        }
    }

    private fun chooseTheme() {

        if (getSharedPreferences("userPrefs", 0).getBoolean("lightMode", false))
            setThisTheme(
                R.style.LightTheme,
                ContextCompat.getColor(this, R.color.lightColorPrimaryDark)
            )
        else
            setThisTheme(R.style.AppTheme, ContextCompat.getColor(this, R.color.colorPrimaryDark))

    }

    private fun setThisTheme(theme: Int, statusBarColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.statusBarColor = statusBarColor

        setTheme(theme)
    }

    private fun setUpFireBase() {
        FirebaseApp.initializeApp(this)
        FirebaseFirestore.getInstance().firestoreSettings =
            with(FirebaseFirestoreSettings.Builder()) {
                isPersistenceEnabled = true
                cacheSizeBytes = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
                build()
            }
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        if (FirebaseAuth.getInstance().currentUser != null)
            FirebaseMessaging.getInstance()
                .subscribeToTopic(FirebaseAuth.getInstance().currentUser!!.displayName!!)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(
            item,
            Navigation.findNavController(this, R.id.navigationHost)
        )
                || super.onOptionsItemSelected(item)
    }

    private fun setUpNavigation() {
        val navController = Navigation.findNavController(this, R.id.navigationHost)
        val drawerLayout = drawer_layout

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(navView, navController)

        val fragmentsWithNoBackButton = setOf(
            R.id.mainFragment,
            R.id.loginFragment
        )

        NavigationUI.setupActionBarWithNavController(
            this, navController,
            AppBarConfiguration.Builder(fragmentsWithNoBackButton).build()
        )

        NavigationUI.setupWithNavController(navView, navController)

        // Users only use the drawer in main fragment.
        navController.addOnDestinationChangedListener { controller, destination, _ ->
            if (destination.id == controller.graph.startDestination) {
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                openDrawerFAB.visibility = View.VISIBLE
            } else {
                openDrawerFAB.visibility = View.GONE
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            }

        }


        openDrawerFAB.setOnClickListener {
            if (drawerLayout.isDrawerOpen(Gravity.START))
                drawerLayout.closeDrawer(Gravity.START)
            else
                drawerLayout.openDrawer(Gravity.START)
        }
    }


    override fun onSupportNavigateUp() =
        NavigationUI.navigateUp(
            Navigation.findNavController(this, R.id.navigationHost),
            drawer_layout
        )

}
