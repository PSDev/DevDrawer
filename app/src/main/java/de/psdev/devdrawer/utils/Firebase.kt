package de.psdev.devdrawer.utils

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.perf.trace

inline fun <T> FirebasePerformance.trace(traceName: String, block: Trace.() -> T): T {
    return newTrace(traceName).trace(block)
}