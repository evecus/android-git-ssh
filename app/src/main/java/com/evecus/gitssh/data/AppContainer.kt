package com.evecus.gitssh.data

import android.content.Context
import com.evecus.gitssh.git.GitService
import com.evecus.gitssh.git.SshKeyManager
import com.evecus.gitssh.git.ShellEngine

class AppContainer(context: Context) {
    val settings = SettingsStore(context)
    val sshKeys = SshKeyManager(context, settings)
    val git = GitService(context, settings, sshKeys)
    val shell = ShellEngine(context, settings, git)
}
