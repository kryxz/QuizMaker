package com.lemonlab.quizmaker

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        setContentView(R.layout.activity_main)
        setUpNavigation()
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_drawer)
        setUpFireBase()
        getDataFromIntent()

    }

    private fun getDataFromIntent() {
        if (intent.extras != null) {
            val notificationType = (intent.extras!!.get("notificationType") as NotificationType)
            if (notificationType == NotificationType.MESSAGE)
                Navigation.findNavController(this, R.id.navigationHost).navigate(R.id.messagesFragment)
            else
                Navigation.findNavController(this, R.id.navigationHost).navigate(R.id.profileFragment)
        }

    }

    private fun setUpFireBase() {
        FirebaseApp.initializeApp(this)
        FirebaseFirestore.getInstance().firestoreSettings = with(FirebaseFirestoreSettings.Builder()) {
            setPersistenceEnabled(true)
            setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            build()
        }
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        if (FirebaseAuth.getInstance().currentUser != null)
            FirebaseMessaging.getInstance()
                .subscribeToTopic(FirebaseAuth.getInstance().currentUser!!.displayName)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(this, R.id.navigationHost))
                || super.onOptionsItemSelected(item)
    }

    private fun setUpNavigation() {
        val navController = Navigation.findNavController(this, R.id.navigationHost)
        NavigationUI.setupWithNavController(navView, navController)

        //setupActionBarWithNavController(navController, drawer_layout)
        setupActionBarWithNavController(navController, AppBarConfiguration.Builder(R.id.loginFragment).build())

        navController.addOnDestinationChangedListener { controller, destination, _ ->
            if (destination.id == controller.graph.startDestination) //Users only use the drawer in main fragment.
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            else
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        }
    }

    override fun onPause() {
        TempData.currentQuizzes = null
        super.onPause()
    }

    override fun onSupportNavigateUp() =
        NavigationUI.navigateUp(Navigation.findNavController(this, R.id.navigationHost), drawer_layout)

}
