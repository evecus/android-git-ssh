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
        Text("上传 / 推送", style = MaterialTheme.typography.titleLarge)
        Text("远程：${state.remoteUrl.ifBlank { "（未配置）" }}")
        Text("本地：${state.repoPath}")
        OutlinedTextField(state.commitMessage, vm::setMsg, label = { Text("提交说明") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = vm::commit, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
            Text("暂存全部并提交")
        }
        Button(onClick = vm::pushSafe, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
            Text("增量推送")
        }
        Button(onClick = vm::pull, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
            Text("先拉取再推（rebase）")
        }
        FilledTonalButton(
            onClick = {
                if (confirmForce) {
                    vm.forcePush()
                    confirmForce = false
                } else confirmForce = true
            },
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (confirmForce) "再点一次确认覆盖远程" else "覆盖远程（force push）")
        }
        FilledTonalButton(onClick = vm::resetHard, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
            Text("本地硬重置 reset --hard")
        }
        if (state.busy) Text("进行中… ${state.message}")
        else if (state.message.isNotBlank()) Text(state.message)
        Text(
            "force push 会覆盖远程当前分支，只用于你自己的小仓库。",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
