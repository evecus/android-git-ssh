package com.evecus.gitssh.ui

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.evecus.gitssh.GitSshApp
import com.evecus.gitssh.git.RepoStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiState(
    val gitUserName: String = "",
    val gitEmail: String = "",
    val githubUser: String = "",
    val remoteUrl: String = "",
    val repoPath: String = "",
    val defaultBranch: String = "main",
    val sshPassphrase: String = "",
    val publicKey: String = "",
    val fingerprint: String = "",
    val hasKey: Boolean = false,
    val commitMessage: String = "手机端更新",
    val status: RepoStatus? = null,
    val log: List<String> = emptyList(),
    val terminalLines: List<String> = listOf("Git 终端：git 走 JGit，其它命令走 /system/bin/sh"),
    val busy: Boolean = false,
    val message: String = "",
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val c = (app as GitSshApp).container
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init { reload() }

    fun reload() {
        val s = c.settings
        _state.update {
            it.copy(
                gitUserName = s.gitUserName,
                gitEmail = s.gitEmail,
                githubUser = s.githubUser,
                remoteUrl = s.remoteUrl,
                repoPath = s.repoPath.ifBlank { c.git.repoDir().absolutePath },
                defaultBranch = s.defaultBranch,
                sshPassphrase = s.sshPassphrase,
                publicKey = c.sshKeys.publicKeyText(),
                fingerprint = c.sshKeys.fingerprint(),
                hasKey = c.sshKeys.hasKey(),
                commitMessage = s.lastCommitMessage,
            )
        }
        refreshStatus()
    }

    fun setName(v: String) { c.settings.gitUserName = v; _state.update { it.copy(gitUserName = v) } }
    fun setEmail(v: String) { c.settings.gitEmail = v; _state.update { it.copy(gitEmail = v) } }
    fun setGhUser(v: String) { c.settings.githubUser = v; _state.update { it.copy(githubUser = v) } }
    fun setRemote(v: String) { c.settings.remoteUrl = v; _state.update { it.copy(remoteUrl = v) } }
    fun setRepoPath(v: String) { c.settings.repoPath = v; _state.update { it.copy(repoPath = v) } }
    fun setBranch(v: String) { c.settings.defaultBranch = v; _state.update { it.copy(defaultBranch = v) } }
    fun setPass(v: String) { c.settings.sshPassphrase = v; _state.update { it.copy(sshPassphrase = v) } }
    fun setMsg(v: String) { c.settings.lastCommitMessage = v; _state.update { it.copy(commitMessage = v) } }

    fun useAppRepo() {
        val p = getApplication<GitSshApp>().getExternalFilesDir("repos")?.resolve("default")
            ?: getApplication<GitSshApp>().filesDir.resolve("repos/default")
        p.mkdirs()
        setRepoPath(p.absolutePath)
    }

    fun useDownloads() {
        val p = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .resolve("git-ssh")
        p.mkdirs()
        setRepoPath(p.absolutePath)
    }

    fun generateKey() = work("正在生成密钥") {
        c.sshKeys.generateEd25519()
        "密钥已生成"
    }

    fun importPrivate(pem: String) = work("导入私钥") {
        c.sshKeys.importPrivateKey(pem)
        "私钥已导入"
    }

    fun importPublic(pub: String) = work("导入公钥") {
        c.sshKeys.importPublicKey(pub)
        "公钥已导入"
    }

    fun initRepo() = work("初始化") { c.git.initLocal(); "仓库已初始化" }
    fun cloneRepo() = work("克隆") { c.git.cloneRemote(); "克隆完成" }
    fun commit() = work("提交") {
        c.git.addAllCommit(_state.value.commitMessage)
        "已提交"
    }
    fun pushSafe() = work("推送") { c.git.push(false); "推送完成" }
    fun forcePush() = work("强制推送") { c.git.push(true); "已强制推送" }
    fun pull() = work("拉取") { c.git.pullRebase(); "拉取完成" }
    fun resetHard() = work("重置") { c.git.resetHard(); "本地已硬重置" }

    fun runShell(line: String) {
        _state.update { it.copy(terminalLines = it.terminalLines + (c.shell.prompt() + line), busy = true) }
        viewModelScope.launch {
            val out = withContext(Dispatchers.IO) { c.shell.run(line) }
            _state.update {
                val extra = if (out.isBlank()) emptyList() else out.lines()
                it.copy(terminalLines = it.terminalLines + extra, busy = false)
            }
            refreshStatus()
        }
    }

    fun refreshStatus() {
        viewModelScope.launch {
            val st = withContext(Dispatchers.IO) {
                runCatching { c.git.status() }.getOrNull()
            }
            val log = withContext(Dispatchers.IO) {
                runCatching { c.git.log() }.getOrDefault(emptyList())
            }
            _state.update {
                it.copy(
                    status = st,
                    log = log,
                    publicKey = c.sshKeys.publicKeyText(),
                    fingerprint = c.sshKeys.fingerprint(),
                    hasKey = c.sshKeys.hasKey(),
                    repoPath = c.settings.repoPath.ifBlank { c.git.repoDir().absolutePath },
                )
            }
        }
    }

    private fun work(label: String, block: () -> String) {
        _state.update { it.copy(busy = true, message = label) }
        viewModelScope.launch {
            val msg = withContext(Dispatchers.IO) {
                runCatching { block() }.fold({ it }, { it.message ?: it.toString() })
            }
            _state.update { it.copy(busy = false, message = msg) }
            reload()
        }
    }
}
