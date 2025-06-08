package ru.glebik.updater.library.http

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object HttpUtils {

    suspend fun get(urlStr: String): String? = withContext(Dispatchers.IO) {
        getInternal(urlStr)
    }

    private fun getInternal(urlStr: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000 // Таймаут подключения (мс)
            connection.readTimeout = 5000    // Таймаут чтения (мс)

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                // Чтение ответа
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                response.toString() // Возвращаем строку
            } else {
                null // Ошибка соединения
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection?.disconnect() // Закрытие соединения
        }
    }
}