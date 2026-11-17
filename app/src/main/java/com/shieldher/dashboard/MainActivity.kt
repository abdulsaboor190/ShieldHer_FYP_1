package com.shieldher.dashboard

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.shieldher.R
import com.shieldher.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge: let the app draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system-bar insets: top inset → status_bar_spacer height, left/right → root padding
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Status bar spacer: exact height of top system bar
            binding.statusBarSpacer.layoutParams =
                binding.statusBarSpacer.layoutParams.also { lp ->
                    (lp as ViewGroup.LayoutParams).height = systemBars.top
                }
            // Left / right padding only on the root (bottom handled by BottomNavigationView)
            binding.root.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        // Connect bottom navigation to NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
    }
}
