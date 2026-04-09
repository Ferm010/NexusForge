package com.ferm.nexusforge.backend

import com.ferm.nexusforge.BuildConfig

// Ключ теперь хранится в BuildConfig и читается из local.properties
val WEB_CLIENT_ID: String
    get() = BuildConfig.WEB_CLIENT_ID
