package com.example.nexusforge.backend

import android.content.Context
import com.example.nexusforge.R

private val errorMessagesMap = mapOf(
    "ERROR_INVALID_EMAIL" to R.string.error_invalid_email,
    "ERROR_WRONG_PASSWORD" to R.string.error_wrong_password,
    "ERROR_INVALID_CREDENTIAL" to R.string.error_wrong_password,
    "ERROR_WEAK_PASSWORD" to R.string.error_weak_password,
    "ERROR_USER_NOT_FOUND" to R.string.error_user_not_found,
    "ERROR_USER_DISABLED" to R.string.error_user_disabled,
    "ERROR_USER_TOKEN_EXPIRED" to R.string.error_token_expired,
    "ERROR_INVALID_USER_TOKEN" to R.string.error_token_expired,
    "ERROR_REQUIRES_RECENT_LOGIN" to R.string.error_requires_recent_login,
    "ERROR_EMAIL_ALREADY_IN_USE" to R.string.error_email_already_in_use,
    "ERROR_OPERATION_NOT_ALLOWED" to R.string.error_operation_not_allowed,
    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" to R.string.error_account_exists_different_credential,
    "ERROR_CREDENTIAL_ALREADY_IN_USE" to R.string.error_credential_already_in_use,
    "ERROR_NETWORK_REQUEST_FAILED" to R.string.error_network_failed,
    "ERROR_TOO_MANY_REQUESTS" to R.string.error_too_many_requests,
    "ERROR_GENERIC" to R.string.error_generic
)

fun errorCodeToString(context: Context, errorCode: String): String {
    val resId = errorMessagesMap[errorCode]
    return if (resId != null) {
        context.getString(resId)
    } else {
        context.getString(R.string.error_authorization)
    }
}
