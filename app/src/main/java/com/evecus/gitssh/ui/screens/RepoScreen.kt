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
        Text("代码根目录", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(state.repoPath, vm::setRepoPath, label = { Text("仓库路径") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPickFolder) { Text("选择文件夹") }
            OutlinedButton(onClick = vm::useAppRepo) { Text("应用目录") }
            OutlinedButton(onClick = vm::useDownloads) { Text("下载目录") }
        }
        Text("私用建议开启「所有文件访问」，否则 JGit 可能写不了任意路径。", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::initRepo, enabled = !state.busy) { Text("初始化仓库") }
            Button(onClick = vm::cloneRepo, enabled = !state.busy) { Text("克隆远程") }
            OutlinedButton(onClick = vm::refreshStatus) { Text("刷新") }
        }
        state.status?.let { s ->
            Text("当前分支：${s.branch}")
            Text(s.path, style = MaterialTheme.typography.bodySmall)
            Text("有改动=${s.dirty}  已修改=${s.modified}  未跟踪=${s.untracked}")
            Text("最新提交：${s.lastCommit}")
        }
        Text("最近提交", style = MaterialTheme.typography.titleMedium)
        state.log.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
        if (state.message.isNotBlank()) Text(state.message)
    }
}
