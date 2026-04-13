package com.ferm.nexusforge.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun <T> debounce(
    delayMillis: Long = 300,
    coroutineScope: CoroutineScope,
    action: suspend (T) -> Unit
): (T) -> Unit {
    var debounceJob: Job? = null
    return { value: T ->
        debounceJob?.cancel()
        debounceJob = coroutineScope.launch {
            delay(delayMillis)
            action(value)
        }
    }
}

fun <T> throttle(
    delayMillis: Long = 300,
    coroutineScope: CoroutineScope,
    action: suspend (T) -> Unit
): (T) -> Unit {
    var lastExecutionTime = 0L
    var throttleJob: Job? = null
    return { value: T ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastExecutionTime >= delayMillis) {
            lastExecutionTime = currentTime
            throttleJob?.cancel()
            throttleJob = coroutineScope.launch {
                action(value)
            }
        }
    }
}
