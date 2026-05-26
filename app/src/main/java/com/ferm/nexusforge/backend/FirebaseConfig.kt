package com.ferm.nexusforge.backend

import com.ferm.nexusforge.BuildConfig

// Ключ теперь хранится в BuildConfig
val WEB_CLIENT_ID: String
    get() = BuildConfig.WEB_CLIENT_ID
