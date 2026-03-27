package com.example.nexusforge.backend
import com.google.firebase.auth.FirebaseAuthException
fun Exception.toRusError(): String {
    if (this is FirebaseAuthException) {
        return when (errorCode) {
            "ERROR_INVALID_EMAIL" -> "Некорректный формат email-адреса."
            "ERROR_WRONG_PASSWORD",
            "ERROR_INVALID_CREDENTIAL" -> "Неверный пароль. Попробуйте ещё раз."
            "ERROR_WEAK_PASSWORD" -> "Пароль слишком простой. Используйте не менее 6 символов."
            "ERROR_USER_NOT_FOUND" -> "Пользователь с таким email не найден."
            "ERROR_USER_DISABLED" -> "Этот аккаунт заблокирован. Обратитесь в поддержку."
            "ERROR_USER_TOKEN_EXPIRED",
            "ERROR_INVALID_USER_TOKEN" -> "Сессия истекла. Войдите заново."
            "ERROR_REQUIRES_RECENT_LOGIN" -> "Для этого действия нужно войти заново."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Этот email уже зарегистрирован."
            "ERROR_OPERATION_NOT_ALLOWED" -> "Данный способ входа не разрешён. Обратитесь к администратору."
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                "Аккаунт уже существует, но был создан другим способом входа."
            "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "Этот аккаунт Google уже привязан."
            "ERROR_NETWORK_REQUEST_FAILED" ->
                "Нет соединения с интернетом. Проверьте сеть и попробуйте снова."
            "ERROR_TOO_MANY_REQUESTS" ->
                "Слишком много попыток. Подождите немного и попробуйте снова."
            else -> "Произошла ошибка авторизации. Попробуйте позже."
        }
    }
    return "Произошла ошибка. Попробуйте позже."
}
