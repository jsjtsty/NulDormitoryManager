package com.nulstudio.dormitorymanager.sys

import android.app.Application
import android.os.Looper
import androidx.core.os.HandlerCompat
import java.util.concurrent.*

class NulApplication : Application() {
    private val workQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()

    var threadPoolExecutor = ThreadPoolExecutor(
        NUMBER_OF_CORES,  // Initial pool size
        NUMBER_OF_CORES,  // Max pool size
        KEEP_ALIVE_TIME.toLong(),
        KEEP_ALIVE_TIME_UNIT,
        workQueue
    )

    companion object {
        var executorService = Executors.newFixedThreadPool(8)
        var mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper())
        private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()
        private const val KEEP_ALIVE_TIME = 1
        private val KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS
    }
}