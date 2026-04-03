package app.hued.processing

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import app.hued.data.model.PermissionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionStateManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val _state = MutableStateFlow<PermissionState>(checkCurrentState())
    val state: StateFlow<PermissionState> = _state.asStateFlow()

    fun refresh() {
        _state.value = checkCurrentState()
    }

    fun updateState(state: PermissionState) {
        _state.value = state
    }

    private fun checkCurrentState(): PermissionState {
        return when {
            Build.VERSION.SDK_INT >= 34 -> {
                when {
                    hasPermission(Manifest.permission.READ_MEDIA_IMAGES) -> PermissionState.Full
                    hasPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> PermissionState.Partial
                    else -> PermissionState.NotRequested
                }
            }
            Build.VERSION.SDK_INT >= 33 -> {
                if (hasPermission(Manifest.permission.READ_MEDIA_IMAGES)) PermissionState.Full
                else PermissionState.NotRequested
            }
            else -> {
                if (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) PermissionState.Full
                else PermissionState.NotRequested
            }
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
