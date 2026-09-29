package com.evecus.gitssh

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.evecus.gitssh.ui.AppViewModel
import com.evecus.gitssh.ui.screens.RepoScreen
import com.evecus.gitssh.ui.screens.SettingsScreen
import com.evecus.gitssh.ui.screens.TerminalScreen
import com.evecus.gitssh.ui.screens.UploadScreen
import com.evecus.gitssh.ui.theme.GitSshTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    private val treePicker = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri ?: return@registerForActivityResult
        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
        val path = uriToPath(uri)
        if (path != null) vm.setRepoPath(path)
    }

    private val storagePerm = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestStorage()
        setContent {
            GitSshTheme {
                val state by vm.state.collectAsStateWithLifecycle()
                var tab by rememberSaveable { mutableIntStateOf(0) }
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = tab == 0,
                                onClick = { tab = 0 },
                                icon = { Icon(Icons.Outlined.Settings, null) },
                                label = { Text("配置") },
                            )
                            NavigationBarItem(
                                selected = tab == 1,
                                onClick = { tab = 1 },
                                icon = { Icon(Icons.Outlined.Folder, null) },
                                label = { Text("仓库") },
                            )
                            NavigationBarItem(
                                selected = tab == 2,
                                onClick = { tab = 2 },
                                icon = { Icon(Icons.Outlined.Upload, null) },
                                label = { Text("推送") },
                            )
                            NavigationBarItem(
                                selected = tab == 3,
                                onClick = { tab = 3 },
                                icon = { Icon(Icons.Outlined.Terminal, null) },
                                label = { Text("终端") },
                            )
                        }
                    },
                ) { pad ->
                    val mod = Modifier.padding(pad)
                    when (tab) {
                        0 -> SettingsScreen(mod, state, vm)
                        1 -> RepoScreen(mod, state, vm, onPickFolder = { treePicker.launch(null) })
                        2 -> UploadScreen(mod, state, vm)
                        else -> TerminalScreen(mod, state, vm)
                    }
                }
            }
        }
    }

    private fun requestStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    },
                )
            }
        } else {
            storagePerm.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ),
            )
        }
    }

    private fun uriToPath(uri: Uri): String? {
        val docId = android.provider.DocumentsContract.getTreeDocumentId(uri)
        val split = docId.split(":")
        if (split.size < 2) return null
        val type = split[0]
        val rel = split[1]
        return if (type.equals("primary", true)) {
            "${Environment.getExternalStorageDirectory()}/$rel"
        } else {
            "/storage/$type/$rel"
        }
    }
}
