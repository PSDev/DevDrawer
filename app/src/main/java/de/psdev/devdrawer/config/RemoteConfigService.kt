package de.psdev.devdrawer.config

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import de.psdev.devdrawer.BuildConfig
import de.psdev.devdrawer.R
import de.psdev.devdrawer.utils.await
import mu.KLogging
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigService @Inject constructor() {
    companion object : KLogging()

    private val remoteConfig by lazy { Firebase.remoteConfig }

    init {
        val settings = remoteConfigSettings {
            if (BuildConfig.DEBUG) {
                minimumFetchIntervalInSeconds = 5
            }
        }
        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    suspend fun getBoolean(key: String): Boolean {
        updateConfig()
        return remoteConfig[key].asBoolean()
    }

    suspend fun getInteger(key: String): Int {
        updateConfig()
        return remoteConfig[key].asLong().toInt()
    }

    suspend fun getLong(key: String): Long {
        updateConfig()
        return remoteConfig[key].asLong()
    }

    private suspend fun updateConfig() {
        try {
            remoteConfig.fetchAndActivate().await()
        } catch (e: Exception) {
            logger.warn(e) { "updateConfig failed: ${e.message}" }
        }
    }

}