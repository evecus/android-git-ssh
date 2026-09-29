package com.evecus.gitssh.git

import android.content.Context
import com.evecus.gitssh.data.SettingsStore
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.sshd.SshdSessionFactory
import java.io.File

data class RepoStatus(
    val path: String,
    val branch: String,
    val dirty: Boolean,
    val added: Int,
    val modified: Int,
    val untracked: Int,
    val lastCommit: String,
)

class GitService(
    private val context: Context,
    private val settings: SettingsStore,
    private val keys: SshKeyManager,
) {
    fun repoDir(): File {
        val p = settings.repoPath
        return if (p.isNotBlank()) File(p) else File(context.filesDir, "repos/default")
    }

    fun ensureDir(): File = repoDir().apply { mkdirs() }

    fun isRepo(): Boolean = File(repoDir(), ".git").exists()

    fun status(): RepoStatus {
        val dir = repoDir()
        if (!isRepo()) {
            return RepoStatus(dir.absolutePath, "-", false, 0, 0, 0, "not a repo")
        }
        Git.open(dir).use { git ->
            val st = git.status().call()
            val branch = git.repository.branch ?: "-"
            val last = git.log().setMaxCount(1).call().firstOrNull()?.let {
                "${it.name.take(7)} ${it.shortMessage}"
            } ?: "no commits"
            return RepoStatus(
                path = dir.absolutePath,
                branch = branch,
                dirty = !st.isClean,
                added = st.added.size + st.changed.size,
                modified = st.modified.size + st.changed.size,
                untracked = st.untracked.size,
                lastCommit = last,
            )
        }
    }

    fun initLocal() {
        val dir = ensureDir()
        Git.init().setDirectory(dir).call().use { git ->
            applyIdentity(git)
            ensureRemote(git)
        }
    }

    fun cloneRemote() {
        val dir = ensureDir()
        if (dir.exists()) dir.deleteRecursively()
        dir.mkdirs()
        val url = settings.remoteUrl.ifBlank { error("remote url empty") }
        Git.cloneRepository()
            .setURI(url)
            .setDirectory(dir)
            .setTransportConfigCallback { t ->
                if (t is SshTransport) t.sshSessionFactory = sshFactory()
            }
            .call()
            .use { applyIdentity(it) }
    }

    fun addAllCommit(message: String) {
        Git.open(repoDir()).use { git ->
            applyIdentity(git)
            git.add().addFilepattern(".").call()
            git.add().setUpdate(true).addFilepattern(".").call()
            val ident = ident()
            git.commit().setMessage(message).setAuthor(ident).setCommitter(ident).call()
        }
    }

    fun pullRebase() {
        Git.open(repoDir()).use { git ->
            ensureRemote(git)
            git.pull()
                .setRebase(true)
                .setTransportConfigCallback { t ->
                    if (t is SshTransport) t.sshSessionFactory = sshFactory()
                }
                .call()
        }
    }

    fun push(force: Boolean) {
        Git.open(repoDir()).use { git ->
            ensureRemote(git)
            val branch = git.repository.branch ?: settings.defaultBranch
            val spec = RefSpec("refs/heads/$branch:refs/heads/$branch")
            val cmd = git.push()
                .setRemote("origin")
                .setRefSpecs(spec)
                .setForce(force)
                .setTransportConfigCallback { t ->
                    if (t is SshTransport) t.sshSessionFactory = sshFactory()
                }
            cmd.call()
        }
    }

    fun checkoutBranch(name: String, create: Boolean) {
        Git.open(repoDir()).use { git ->
            val cmd = git.checkout().setName(name)
            if (create) cmd.setCreateBranch(true)
            cmd.call()
        }
    }

    fun log(n: Int = 20): List<String> {
        if (!isRepo()) return emptyList()
        Git.open(repoDir()).use { git ->
            return git.log().setMaxCount(n).call().map {
                "${it.name.take(7)}  ${it.shortMessage}"
            }
        }
    }

    fun resetHard() {
        Git.open(repoDir()).use { git ->
            git.reset().setMode(ResetCommand.ResetType.HARD).call()
        }
    }

    private fun ident(): PersonIdent =
        PersonIdent(
            settings.gitUserName.ifBlank { "android" },
            settings.gitEmail.ifBlank { "android@local" },
        )

    private fun applyIdentity(git: Git) {
        val cfg = git.repository.config
        if (settings.gitUserName.isNotBlank()) {
            cfg.setString("user", null, "name", settings.gitUserName)
        }
        if (settings.gitEmail.isNotBlank()) {
            cfg.setString("user", null, "email", settings.gitEmail)
        }
        cfg.save()
        ensureRemote(git)
    }

    private fun ensureRemote(git: Git) {
        val url = settings.remoteUrl
        if (url.isBlank()) return
        val existing = git.remoteList().call().any { it.name == "origin" }
        if (!existing) {
            git.remoteAdd().setName("origin").setUri(URIish(url)).call()
        } else {
            git.remoteSetUrl().setRemoteName("origin").setRemoteUri(URIish(url)).call()
        }
    }

    private fun sshFactory(): SshdSessionFactory {
        return object : SshdSessionFactory() {
            override fun getSshDirectory(): File = keys.keyDir
            override fun getDefaultKeys(sshDir: File?): List<File> =
                listOf(keys.privateKeyFile).filter { it.exists() }
        }.also {
            it.homeDirectory = context.filesDir
        }
    }
}
