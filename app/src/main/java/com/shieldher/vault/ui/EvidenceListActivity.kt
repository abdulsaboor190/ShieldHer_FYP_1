package com.shieldher.vault.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.shieldher.R
import com.shieldher.vault.data.EvidenceEntity
import com.shieldher.vault.data.EvidenceRepository
import kotlinx.coroutines.launch

class EvidenceListActivity : AppCompatActivity() {

    private val viewModel: EvidenceListViewModel by viewModels()
    private val repository by lazy { EvidenceRepository(this) }
    private lateinit var adapter: EvidenceListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evidence_list)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.evidence_vault_title)

        val emptyStateTextView = findViewById<TextView>(R.id.emptyStateTextView)
        val recyclerView = findViewById<RecyclerView>(R.id.evidenceRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EvidenceListAdapter(
            onVerifyClick = { item ->
                lifecycleScope.launch {
                    val valid = repository.verifyIntegrity(item.id)
                    Toast.makeText(
                        this@EvidenceListActivity,
                        if (valid) "Integrity verified" else "Integrity check failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onDeleteClick = { item -> showDeleteConfirmation(item) }
        )
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            repository.sweepOrphans()
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.evidenceList.collect { evidence ->
                    adapter.submitList(evidence)
                    if (evidence.isEmpty()) {
                        emptyStateTextView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyStateTextView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmation(item: EvidenceEntity) {
        AlertDialog.Builder(this)
            .setMessage("Delete this evidence? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    repository.delete(item.id)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
