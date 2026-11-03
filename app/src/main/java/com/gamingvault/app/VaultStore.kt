package com.gamingvault.app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class VaultAccount(
    val id: String = java.util.UUID.randomUUID().toString(),
    val game: String = "",
    val label: String = "",
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val emailPassword: String = "",
    val gamigoUsername: String = "",
    val gamigoPassword: String = "",
    val notes: String = ""
)

class VaultStore(private val context: Context) {
    private val file: File get() = File(context.filesDir, "vault.enc")
    private val alias = "gaming_vault_aes_v1"

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256).build())
        return generator.generateKey()
    }

    fun load(): List<VaultAccount> {
        if (!file.exists()) return emptyList()
        val bytes = file.readBytes()
        require(bytes.size > 12) { "Ungültiger Tresor" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        val array = JSONArray(String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8))
        return (0 until array.length()).map { index ->
            val o = array.getJSONObject(index)
            VaultAccount(
                id = o.optString("id"), game = o.optString("game"), label = o.optString("label"),
                username = o.optString("username"), password = o.optString("password"),
                email = o.optString("email"), emailPassword = o.optString("emailPassword"),
                gamigoUsername = o.optString("gamigoUsername"), gamigoPassword = o.optString("gamigoPassword"),
                notes = o.optString("notes")
            )
        }
    }

    fun save(accounts: List<VaultAccount>) {
        val array = JSONArray()
        accounts.forEach { a -> array.put(JSONObject().apply {
            put("id", a.id); put("game", a.game); put("label", a.label)
            put("username", a.username); put("password", a.password)
            put("email", a.email); put("emailPassword", a.emailPassword)
            put("gamigoUsername", a.gamigoUsername); put("gamigoPassword", a.gamigoPassword)
            put("notes", a.notes)
        }) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.iv + cipher.doFinal(array.toString().toByteArray(Charsets.UTF_8))
        val temp = File(context.filesDir, "vault.tmp")
        temp.writeBytes(encrypted)
        check(temp.renameTo(file)) { "Speichern fehlgeschlagen" }
    }
}
