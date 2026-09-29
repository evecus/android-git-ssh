package com.evecus.gitssh.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SettingsStore(context: Context) {
    private val master = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "git_ssh_secure",
        master,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    var gitUserName: String
        get() = prefs.getString("git_name", "") ?: ""
        set(v) { prefs.edit().putString("git_name", v).apply() }

    var gitEmail: String
        get() = prefs.getString("git_email", "") ?: ""
        set(v) { prefs.edit().putString("git_email", v).apply() }

    var githubUser: String
        get() = prefs.getString("gh_user", "") ?: ""
        set(v) { prefs.edit().putString("gh_user", v).apply() }

    var remoteUrl: String
        get() = prefs.getString("remote", "") ?: ""
        set(v) { prefs.edit().putString("remote", v).apply() }

    var repoPath: String
        get() = prefs.getString("repo_path", "") ?: ""
        set(v) { prefs.edit().putString("repo_path", v).apply() }

    var defaultBranch: String
        get() = prefs.getString("branch", "main") ?: "main"
        set(v) { prefs.edit().putString("branch", v).apply() }

    var sshPassphrase: String
        get() = prefs.getString("ssh_pass", "") ?: ""
        set(v) { prefs.edit().putString("ssh_pass", v).apply() }

    var lastCommitMessage: String
        get() = prefs.getString("last_msg", "update from android") ?: "update from android"
        set(v) { prefs.edit().putString("last_msg", v).apply() }
}
