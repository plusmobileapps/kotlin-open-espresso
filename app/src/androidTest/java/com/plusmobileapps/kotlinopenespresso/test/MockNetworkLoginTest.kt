package com.plusmobileapps.kotlinopenespresso.test

import androidx.test.espresso.IdlingRegistry
import com.plusmobileapps.kotlinopenespresso.data.LoginDataSource
import com.plusmobileapps.kotlinopenespresso.data.model.LoginResponse
import com.plusmobileapps.kotlinopenespresso.di.EspressoModule
import com.plusmobileapps.kotlinopenespresso.di.NetworkModule
import com.plusmobileapps.kotlinopenespresso.extension.launchActivity
import com.plusmobileapps.kotlinopenespresso.extension.verifyText
import com.plusmobileapps.kotlinopenespresso.pageobjects.LoginUI
import com.plusmobileapps.kotlinopenespresso.ui.login.LoginActivity
import com.plusmobileapps.kotlinopenespresso.util.CountingIdlingResource
import com.plusmobileapps.kotlinopenespresso.util.TestCountingIdlingResource
import com.plusmobileapps.kotlinopenespresso.util.fullUrl
import com.plusmobileapps.kotlinopenespresso.util.mockHttpClient
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@UninstallModules(NetworkModule::class, EspressoModule::class)
@HiltAndroidTest
class MockNetworkLoginTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private var getLoginResponse: (MockRequestHandler)? = null

    val _idlingResource = TestCountingIdlingResource()

    @BindValue
    @JvmField
    val idlingResource: CountingIdlingResource = _idlingResource

    @BindValue
    @JvmField
    val mockClient: HttpClient = mockHttpClient { request: HttpRequestData ->
        when (request.url.fullUrl) {
            LoginDataSource.LOGIN_URL -> getLoginResponse?.invoke(this, request)
                ?: throw NotImplementedError("${request.url.fullUrl} was called but the response handler wasn't implemented")
            else -> error("Unhandled ${request.url.fullUrl}")
        }
    }

    private val username = "some-awesome-user-name"
    private val password = "password123"

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(_idlingResource.idlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(_idlingResource.idlingResource)
        getLoginResponse = null
    }

    @Test
    fun successfulLogin() {
        everyLoginReturns {
            respond(
                Json.encodeToString(LoginResponse("first-id"),),
                HttpStatusCode.OK,
                headersOf("Content-Type", ContentType.Application.Json.toString())
            )
        }

        launchActivity<LoginActivity>()

        LoginUI().apply {
            enterInfo(username, password)
            submitAndGoToResultUI {
                onBodyText().verifyText("Welcome $username")
            }
        }
    }

    private fun everyLoginReturns(response: MockRequestHandler) {
        getLoginResponse = response
    }
}