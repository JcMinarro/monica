package com.teobaranga.monica.setup

import androidx.compose.ui.text.input.TextFieldValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import com.teobaranga.monica.auth.AuthorizationRepository
import com.teobaranga.monica.data.PARAM_CLIENT_ID
import com.teobaranga.monica.data.PARAM_REDIRECT_URI
import com.teobaranga.monica.data.PARAM_RESPONSE_TYPE
import com.teobaranga.monica.data.REDIRECT_URI
import com.teobaranga.monica.settings.getOAuthSettings
import com.teobaranga.monica.settings.oAuthSettings
import com.teobaranga.monica.util.coroutines.Dispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import timber.log.Timber
import javax.inject.Inject

@OptIn(SavedStateHandleSaveableApi::class)
@HiltViewModel
class SetupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcher: Dispatcher,
    private val dataStore: DataStore<Preferences>,
    private val authorizationRepository: AuthorizationRepository,
) : ViewModel() {

    val isLoggedIn = authorizationRepository.isLoggedIn

    val uiState by savedStateHandle.saveable(saver = UiState.Saver) {
        UiState()
    }

    private val _setupUri = MutableSharedFlow<String>()
    val setupUri: SharedFlow<String> = _setupUri

    init {
        viewModelScope.launch(dispatcher.io) {
            val preferences = dataStore.data.first()
            val oAuthSettings = preferences.getOAuthSettings()
            withContext(dispatcher.main) {
                oAuthSettings.serverAddress?.let {
                    uiState.onServerAddressChanged(TextFieldValue(it))
                }
                oAuthSettings.clientId?.let {
                    uiState.onClientIdChanged(TextFieldValue(it))
                }
                oAuthSettings.clientSecret?.let {
                    uiState.onClientSecretChanged(TextFieldValue(it))
                }
            }
        }
    }

    fun onSignIn() {
        viewModelScope.launch(dispatcher.io) {
            val baseUrl = "${uiState.serverAddress.text}/oauth/authorize".toHttpUrlOrNull() ?: return@launch

            dataStore.edit { preferences ->
                preferences.oAuthSettings {
                    setServerAddress(uiState.serverAddress.text)
                    setClientId(uiState.clientId.text)
                    setClientSecret(uiState.clientSecret.text)
                }
            }

            val url = baseUrl
                .newBuilder()
                .addQueryParameter(PARAM_CLIENT_ID, uiState.clientId.text)
                .addQueryParameter(PARAM_RESPONSE_TYPE, "code")
                .addQueryParameter(PARAM_REDIRECT_URI, REDIRECT_URI)
                .build()
                .toString()
            _setupUri.emit(url)
        }
    }

    fun onAuthorizationCode(code: String?) {
        if (code == null) {
            Timber.w("Received null authorization code")
            return
        }
        Timber.d("Authorization code: $code")
        viewModelScope.launch(dispatcher.io) {
            val oAuthSettings = dataStore.data.first().getOAuthSettings()
            val clientId = requireNotNull(oAuthSettings.clientId)
            val clientSecret = requireNotNull(oAuthSettings.clientSecret)
            val result = authorizationRepository.signIn(clientId, clientSecret, code)
            if (!result) {
                uiState.error = UiState.Error.ConfigurationError
            }
        }
    }
}
