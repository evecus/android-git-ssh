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
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("GitHub / Git 身份", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(state.gitUserName, vm::setName, label = { Text("姓名 user.name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.gitEmail, vm::setEmail, label = { Text("邮箱 user.email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.githubUser, vm::setGhUser, label = { Text("GitHub 用户名") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            state.remoteUrl,
            vm::setRemote,
            label = { Text("SSH 远程  git@github.com:用户/仓库.git") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(state.defaultBranch, vm::setBranch, label = { Text("默认分支") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.sshPassphrase, vm::setPass, label = { Text("私钥口令（可空）") }, modifier = Modifier.fillMaxWidth())
        Text("SSH 密钥", style = MaterialTheme.typography.titleMedium)
        Text(if (state.hasKey) "已有密钥  ${state.fingerprint}" else "尚未生成密钥")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::generateKey, enabled = !state.busy) { Text("生成 Ed25519") }
            OutlinedButton(
                onClick = {
                    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("ssh pub", state.publicKey))
                },
                enabled = state.publicKey.isNotBlank(),
            ) { Text("复制公钥") }
        }
        if (state.publicKey.isNotBlank()) {
            Text(state.publicKey, style = MaterialTheme.typography.bodySmall)
            Text("把公钥贴到 GitHub → Settings → SSH and GPG keys")
        }
        OutlinedTextField(importPriv, { importPriv = it }, label = { Text("粘贴私钥 PEM 导入") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        OutlinedButton(onClick = { vm.importPrivate(importPriv) }, enabled = importPriv.isNotBlank()) { Text("导入私钥") }
        OutlinedTextField(importPub, { importPub = it }, label = { Text("粘贴公钥导入") }, modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = { vm.importPublic(importPub) }, enabled = importPub.isNotBlank()) { Text("导入公钥") }
        if (state.message.isNotBlank()) Text(state.message)
    }
}
