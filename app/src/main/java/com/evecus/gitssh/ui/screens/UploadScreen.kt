package com.evecus.gitssh.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.evecus.gitssh.ui.AppViewModel
import com.evecus.gitssh.ui.UiState

@Composable
fun UploadScreen(modifier: Modifier, state: UiState, vm: AppViewModel) {
    var confirmForce by remember { mutableStateOf(false) }
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Push", style = MaterialTheme.typography.titleLarge)
        Text("remote: ${state.remoteUrl.ifBlank { "(none)" }}")
        Text("local: ${state.repoPath}")
        OutlinedTextField(state.commitMessage, vm::setMsg, label = { Text("commit message") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = vm::commit, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("add + commit") }
        Button(onClick = vm::pushSafe, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("push") }
        Button(onClick = vm::pull, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("pull --rebase") }
        FilledTonalButton(
            onClick = {
                if (confirmForce) { vm.forcePush(); confirmForce = false } else confirmForce = true
            },
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (confirmForce) "tap again to force push" else "force push") }
        FilledTonalButton(onClick = vm::resetHard, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
            Text("reset --hard")
        }
        if (state.busy) Text("busy ${state.message}") else if (state.message.isNotBlank()) Text(state.message)
    }
}
