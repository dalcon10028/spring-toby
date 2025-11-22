package study

import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.SendChannel

private sealed class MapMessage
private data class Put(val key: Int, val value: String) : MapMessage()
private data class Update(val key: Int, val newValue: String) : MapMessage()
private data class Remove(val key: Int) : MapMessage()
private data class Get(val key: Int, val response: SendChannel<String?>) : MapMessage()

@OptIn(ObsoleteCoroutinesApi::class)
class ConcurrencyTest : FunSpec({
    context("ConcurrentHashMap") {
        test("should handle concurrent access correctly") {
            val concurrentMap = ConcurrentHashMap<Int, String>()

            // Create 100 coroutines to put values into the map
            val jobs = (0 until 100).map { i ->
                launch(Dispatchers.IO) { // Use Dispatchers.IO for multi-threaded execution
                    concurrentMap[i] = "Value $i"
                }
            }

            // Wait for all coroutines to finish
            jobs.joinAll()

            // Verify that all values are present in the map
            for (i in 0 until 100) {
                val value = concurrentMap[i]
                if (value != "Value $i") {
                    throw AssertionError("Expected Value $i but found $value")
                }
            }
        }
    }
})