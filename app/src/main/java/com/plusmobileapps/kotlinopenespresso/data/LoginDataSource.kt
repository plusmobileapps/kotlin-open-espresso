package com.plusmobileapps.kotlinopenespresso.data

import com.plusmobileapps.kotlinopenespresso.OpenForTest
import com.plusmobileapps.kotlinopenespresso.data.model.LoggedInUser
import com.plusmobileapps.model.LoginRequest
import com.plusmobileapps.model.LoginResponse
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
@OpenForTest
class LoginDataSource @Inject constructor(private val httpClient: HttpClient) {

    companion object {
//        const val LOGIN_URL = "https://plusmobileapps.com/login"
        const val LOGIN_URL = "http://10.0.2.2:8080/login"
    }

    suspend fun login(email: String, password: String): Result<LoggedInUser> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post<LoginResponse>(LOGIN_URL) {
                contentType(ContentType.Application.Json)
                body = LoginRequest(email, password)
            }
            val user = LoggedInUser(response.id, response.displayName)
            Result.Success(user)
        } catch (e: Throwable) {
            val errorMessage = if (e is ClientRequestException) {
                e.response.readText()
            } else {
                "Don't know the error"
            }
            Result.Error(IOException(errorMessage, e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}