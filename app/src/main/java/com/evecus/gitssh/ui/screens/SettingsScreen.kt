package com.evecus.gitssh.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.evecus.gitssh.ui.AppViewModel
import com.evecus.gitssh.ui.UiState

@Composable
fun SettingsScreen(modifier: Modifier, state: UiState, vm: AppViewModel) {
    val ctx = LocalContext.current
    var importPriv by remember { mutableStateOf("") }
    var importPub by remember { mutableStateOf("") }
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("GitHub / Git", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(state.gitUserName, vm::setName, label = { Text("user.name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.gitEmail, vm::setEmail, label = { Text("user.email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.githubUser, vm::setGhUser, label = { Text("GitHub user") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.remoteUrl, vm::setRemote, label = { Text("SSH remote") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.defaultBranch, vm::setBranch, label = { Text("default branch") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.sshPassphrase, vm::setPass, label = { Text("key passphrase") }, modifier = Modifier.fillMaxWidth())
        Text(if (state.hasKey) "key ok  ${state.fingerprint}" else "no key")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::generateKey, enabled = !state.busy) { Text("Generate Ed25519") }
            OutlinedButton(onClick = {
                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("ssh pub", state.publicKey))
            }, enabled = state.publicKey.isNotBlank()) { Text("Copy pub") }
        }
        if (state.publicKey.isNotBlank()) Text(state.publicKey, style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(importPriv, { importPriv = it }, label = { Text("paste private PEM") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        OutlinedButton(onClick = { vm.importPrivate(importPriv) }, enabled = importPriv.isNotBlank()) { Text("Import private") }
        OutlinedTextField(importPub, { importPub = it }, label = { Text("paste public key") }, modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = { vm.importPublic(importPub) }, enabled = importPub.isNotBlank()) { Text("Import public") }
        if (state.message.isNotBlank()) Text(state.message)
    }
}
