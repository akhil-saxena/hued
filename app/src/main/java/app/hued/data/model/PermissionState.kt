package app.hued.data.model

sealed interface PermissionState {
    data object Full : PermissionState
    data object Partial : PermissionState
    data object Denied : PermissionState
    data object Revoked : PermissionState
    data object NotRequested : PermissionState
}
