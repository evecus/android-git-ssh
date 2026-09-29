package com.evecus.gitssh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.evecus.gitssh.ui.AppViewModel
import com.evecus.gitssh.ui.UiState

@Composable
fun RepoScreen(modifier: Modifier, state: UiState, vm: AppViewModel, onPickFolder: () -> Unit) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Repo root", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(state.repoPath, vm::setRepoPath, label = { Text("path") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPickFolder) { Text("Pick folder") }
            OutlinedButton(onClick = vm::useAppRepo) { Text("App dir") }
            OutlinedButton(onClick = vm::useDownloads) { Text("Downloads") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::initRepo, enabled = !state.busy) { Text("git init") }
            Button(onClick = vm::cloneRepo, enabled = !state.busy) { Text("clone") }
            OutlinedButton(onClick = vm::refreshStatus) { Text("refresh") }
        }
        state.status?.let { s ->
            Text("branch ${s.branch}")
            Text(s.path, style = MaterialTheme.typography.bodySmall)
            Text("dirty=${s.dirty} modified=${s.modified} untracked=${s.untracked}")
            Text(s.lastCommit)
        }
        Text("log", style = MaterialTheme.typography.titleMedium)
        state.log.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
        if (state.message.isNotBlank()) Text(state.message)
    }
}
