package com.lemonlab.quizmaker

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.firebase.FirebaseApp
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        setTheme(R.style.TextAppearance)
        setContentView(R.layout.activity_main)
        setUpNavigation()
        FirebaseApp.initializeApp(this)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(this, R.id.navigationHost))
                || super.onOptionsItemSelected(item)
    }

    private fun setUpNavigation() {
        drawerLayout = drawer_layout
        val navController = Navigation.findNavController(this, R.id.navigationHost)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(navView, navController)
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            //main fragment, login fragment will have no back/up button.
            AppBarConfiguration.Builder(setOf(R.id.mainFragment, R.id.loginFragment)).build()
        )

        navController.addOnDestinationChangedListener { controller, destination, _ ->
            if (destination.id == controller.graph.startDestination) //Users only use the drawer in main fragment.
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            else
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        }
    }

    override fun onSupportNavigateUp() =
        NavigationUI.navigateUp(Navigation.findNavController(this, R.id.navigationHost), drawerLayout)

}
