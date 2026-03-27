package com.example.nexusforge.backend

import com.example.nexusforge.BuildConfig

// Ключ теперь хранится в BuildConfig и читается из local.properties
val WEB_CLIENT_ID: String
    get() = BuildConfig.WEB_CLIENT_ID
