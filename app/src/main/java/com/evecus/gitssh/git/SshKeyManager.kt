package com.evecus.gitssh.git

import android.content.Context
import com.evecus.gitssh.data.SettingsStore
import org.apache.sshd.common.config.keys.KeyUtils
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter
import org.apache.sshd.common.util.security.SecurityUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.KeyPair
import java.security.KeyPairGenerator

class SshKeyManager(
    private val context: Context,
    private val settings: SettingsStore,
) {
    val keyDir: File get() = File(context.filesDir, "ssh").apply { mkdirs() }
    val privateKeyFile: File get() = File(keyDir, "id_ed25519")
    val publicKeyFile: File get() = File(keyDir, "id_ed25519.pub")
    val knownHostsFile: File get() = File(keyDir, "known_hosts")

    fun hasKey(): Boolean = privateKeyFile.exists() && publicKeyFile.exists()

    fun publicKeyText(): String =
        if (publicKeyFile.exists()) publicKeyFile.readText() else ""

    fun generateEd25519() {
        val pair: KeyPair = try {
            KeyPairGenerator.getInstance("Ed25519").generateKeyPair()
        } catch (_: Exception) {
            KeyPairGenerator.getInstance("RSA").apply { initialize(4096) }.generateKeyPair()
        }
        writeOpenSsh(pair)
    }

    fun importPrivateKey(pem: String) {
        privateKeyFile.writeText(pem.trim() + "\n")
        if (!publicKeyFile.exists()) {
            publicKeyFile.writeText("# imported private key; add matching .pub if needed\n")
        }
    }

    fun importPublicKey(pub: String) {
        publicKeyFile.writeText(pub.trim() + "\n")
    }

    private fun writeOpenSsh(pair: KeyPair) {
        val comment = settings.githubUser.ifBlank { "android-git-ssh" }
        val privOut = ByteArrayOutputStream()
        val pubOut = ByteArrayOutputStream()
        val writer = OpenSSHKeyPairResourceWriter.INSTANCE
        writer.writePrivateKey(pair, comment, null, privOut)
        writer.writePublicKey(pair, comment, pubOut)
        privateKeyFile.writeBytes(privOut.toByteArray())
        publicKeyFile.writeBytes(pubOut.toByteArray())
        privateKeyFile.setReadable(false, false)
        privateKeyFile.setReadable(true, true)
        privateKeyFile.setWritable(true, true)
    }

    fun fingerprint(): String {
        if (!publicKeyFile.exists()) return ""
        return try {
            val loader = SecurityUtils.getKeyPairResourceParser()
            val ids = loader.loadKeyPairs(null, publicKeyFile.toPath(), null)
            val pub = ids.firstOrNull()?.public ?: return ""
            KeyUtils.getFingerPrint(pub)
        } catch (_: Exception) {
            "present"
        }
    }
}
