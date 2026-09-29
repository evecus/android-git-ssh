package com.evecus.gitssh.git

import android.content.Context
import com.evecus.gitssh.data.SettingsStore
import java.io.File
import java.util.concurrent.TimeUnit

class ShellEngine(
    private val context: Context,
    private val settings: SettingsStore,
    private val git: GitService,
) {
    var cwd: File = git.repoDir()

    fun prompt(): String = "${cwd.absolutePath}$ "

    fun run(line: String): String {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return ""
        val parts = tokenize(trimmed)
        val cmd = parts.first()
        if (cmd == "cd") {
            val target = parts.getOrNull(1) ?: return cwd.absolutePath
            val next = resolve(target)
            return if (next.isDirectory) {
                cwd = next
                ""
            } else {
                "cd: no such directory: $target"
            }
        }
        if (cmd == "pwd") return cwd.absolutePath
        if (cmd == "git" || trimmed.startsWith("git ")) {
            return runGit(parts.drop(1))
        }
        return runSh(trimmed)
    }

    private fun runGit(args: List<String>): String {
        if (args.isEmpty()) return "git: need a subcommand"
        return try {
            when (args[0]) {
                "status" -> {
                    val s = git.status()
                    buildString {
                        appendLine("On branch ${s.branch}")
                        appendLine("path: ${s.path}")
                        appendLine("dirty=${s.dirty} added=${s.added} modified=${s.modified} untracked=${s.untracked}")
                        appendLine(s.lastCommit)
                    }
                }
                "init" -> {
                    git.initLocal()
                    "initialized ${git.repoDir()}"
                }
                "add" -> {
                    git.addAllCommit(settings.lastCommitMessage)
                    "staged+committed with last message (use GUI commit for custom msg)"
                }
                "commit" -> {
                    val msgIdx = args.indexOf("-m")
                    val msg = if (msgIdx >= 0) args.getOrNull(msgIdx + 1) else settings.lastCommitMessage
                    git.addAllCommit(msg ?: "commit")
                    "committed"
                }
                "push" -> {
                    val force = "--force" in args || "-f" in args
                    git.push(force)
                    if (force) "force-pushed" else "pushed"
                }
                "pull" -> {
                    git.pullRebase()
                    "pulled (rebase)"
                }
                "log" -> git.log().joinToString("\n")
                "clone" -> {
                    git.cloneRemote()
                    "cloned ${settings.remoteUrl}"
                }
                "checkout", "switch" -> {
                    val name = args.last { !it.startsWith("-") && it != args[0] }
                    git.checkoutBranch(name, create = "-b" in args)
                    "now on $name"
                }
                "reset" -> {
                    if ("--hard" in args) {
                        git.resetHard()
                        "reset --hard"
                    } else "only git reset --hard is implemented"
                }
                else -> "git ${args[0]}: not implemented in JGit shell; use supported commands"
            }
        } catch (e: Exception) {
            e.message ?: e.toString()
        }
    }

    private fun runSh(line: String): String {
        val pb = ProcessBuilder("/system/bin/sh", "-c", line)
            .directory(cwd)
            .redirectErrorStream(true)
        val env = pb.environment()
        env["HOME"] = context.filesDir.absolutePath
        env["TMPDIR"] = context.cacheDir.absolutePath
        return try {
            val p = pb.start()
            val out = p.inputStream.bufferedReader().readText()
            p.waitFor(30, TimeUnit.SECONDS)
            out.ifBlank { "(exit ${p.exitValue()})" }
        } catch (e: Exception) {
            e.message ?: e.toString()
        }
    }

    private fun resolve(target: String): File =
        if (target.startsWith("/")) File(target) else File(cwd, target).canonicalFile

    private fun tokenize(s: String): List<String> {
        val out = mutableListOf<String>()
        val cur = StringBuilder()
        var q: Char? = null
        for (c in s) {
            when {
                q != null && c == q -> q = null
                q == null && (c == '"' || c == '\'') -> q = c
                q == null && c.isWhitespace() -> {
                    if (cur.isNotEmpty()) {
                        out += cur.toString()
                        cur.clear()
                    }
                }
                else -> cur.append(c)
            }
        }
        if (cur.isNotEmpty()) out += cur.toString()
        return out
    }
}
