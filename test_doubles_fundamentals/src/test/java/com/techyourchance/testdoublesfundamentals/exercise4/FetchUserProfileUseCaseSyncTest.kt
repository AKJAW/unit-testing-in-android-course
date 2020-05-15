package com.techyourchance.testdoublesfundamentals.exercise4

import com.techyourchance.testdoublesfundamentals.example4.networking.NetworkErrorException
import com.techyourchance.testdoublesfundamentals.exercise4.networking.UserProfileHttpEndpointSync
import com.techyourchance.testdoublesfundamentals.exercise4.users.User
import com.techyourchance.testdoublesfundamentals.exercise4.users.UsersCache
import org.hamcrest.CoreMatchers
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class FetchUserProfileUseCaseSyncTest {

    companion object {
        private const val USER_ID = "idd"
        private const val USER_NAME = "NAME"
        private const val USER_URL = "URL"

        private val SUCCESS_USER = User(
                USER_ID,
                USER_NAME,
                USER_URL
        )

        private val SUCCESS_ENDPOINT_RESULT = UserProfileHttpEndpointSync.EndpointResult(
                UserProfileHttpEndpointSync.EndpointResultStatus.SUCCESS,
                USER_ID,
                USER_NAME,
                USER_URL
        )

        private val AUTH_ERROR_ENDPOINT_RESULT = UserProfileHttpEndpointSync.EndpointResult(
                UserProfileHttpEndpointSync.EndpointResultStatus.AUTH_ERROR, null, null, null
        )

        private val GENERAL_ERROR_ENDPOINT_RESULT = UserProfileHttpEndpointSync.EndpointResult(
                UserProfileHttpEndpointSync.EndpointResultStatus.GENERAL_ERROR, null, null, null
        )

        private val SERVER_ERROR_ENDPOINT_RESULT = UserProfileHttpEndpointSync.EndpointResult(
                UserProfileHttpEndpointSync.EndpointResultStatus.SERVER_ERROR, null, null, null
        )
    }

    private lateinit var userProfileHttpEndpointSync: UserProfileHttpEndpointSyncTd
    private lateinit var usersCache: UsersCacheTd
    private lateinit var SUT: FetchUserProfileUseCaseSync

    @Before
    fun setUp(){
        userProfileHttpEndpointSync = UserProfileHttpEndpointSyncTd()
        usersCache = UsersCacheTd()
        SUT = FetchUserProfileUseCaseSync(userProfileHttpEndpointSync, usersCache)
    }

    @Test
    fun `fetchUserProfileSync passes the userId to the endpoint`(){
        userProfileHttpEndpointSync.result = SUCCESS_ENDPOINT_RESULT
        SUT.fetchUserProfileSync(USER_ID)

        assertThat(userProfileHttpEndpointSync.userId, CoreMatchers.`is`(USER_ID))
    }

    @Test
    fun `fetchUserProfileSync succeeds then SUCCESS result is returned`(){
        userProfileHttpEndpointSync.result = SUCCESS_ENDPOINT_RESULT

        val result: FetchUserProfileUseCaseSync.UseCaseResult = SUT.fetchUserProfileSync(USER_ID)

        assertThat(result, CoreMatchers.`is`(FetchUserProfileUseCaseSync.UseCaseResult.SUCCESS))
    }

    @Test
    fun `fetchUserProfileSync auth error then FAILURE result is returned`(){
        userProfileHttpEndpointSync.result = AUTH_ERROR_ENDPOINT_RESULT

        val result: FetchUserProfileUseCaseSync.UseCaseResult = SUT.fetchUserProfileSync(USER_ID)

        assertThat(result, CoreMatchers.`is`(FetchUserProfileUseCaseSync.UseCaseResult.FAILURE))
    }

    @Test
    fun `fetchUserProfileSync general error then FAILURE result is returned`(){
        userProfileHttpEndpointSync.result = GENERAL_ERROR_ENDPOINT_RESULT

        val result: FetchUserProfileUseCaseSync.UseCaseResult = SUT.fetchUserProfileSync(USER_ID)

        assertThat(result, CoreMatchers.`is`(FetchUserProfileUseCaseSync.UseCaseResult.FAILURE))
    }

    @Test
    fun `fetchUserProfileSync server error then FAILURE result is returned`(){
        userProfileHttpEndpointSync.result = SERVER_ERROR_ENDPOINT_RESULT

        val result: FetchUserProfileUseCaseSync.UseCaseResult = SUT.fetchUserProfileSync(USER_ID)

        assertThat(result, CoreMatchers.`is`(FetchUserProfileUseCaseSync.UseCaseResult.FAILURE))
    }

    @Test
    fun `fetchUserProfileSync endpoint error then NETWORK_ERROR result is returned`(){
        userProfileHttpEndpointSync.throwsError = true

        val result: FetchUserProfileUseCaseSync.UseCaseResult = SUT.fetchUserProfileSync(USER_ID)

        assertThat(result, CoreMatchers.`is`(FetchUserProfileUseCaseSync.UseCaseResult.NETWORK_ERROR))
    }

    @Test
    fun `fetchUserProfileSync succeeds then the user profile is cached`(){
        userProfileHttpEndpointSync.result = SUCCESS_ENDPOINT_RESULT
        SUT.fetchUserProfileSync(USER_ID)

        assertThat(usersCache.user?.userId, CoreMatchers.`is`(SUCCESS_USER.userId))
        assertThat(usersCache.user?.fullName, CoreMatchers.`is`(SUCCESS_USER.fullName))
        assertThat(usersCache.user?.imageUrl, CoreMatchers.`is`(SUCCESS_USER.imageUrl))
    }

    @Test
    fun `fetchUserProfileSync general error then the user profile is not cached`(){
        userProfileHttpEndpointSync.result = GENERAL_ERROR_ENDPOINT_RESULT

        SUT.fetchUserProfileSync(USER_ID)

        assertNull(usersCache.user)
    }

    @Test
    fun `fetchUserProfileSync auth error then the user profile is not cached`(){
        userProfileHttpEndpointSync.result = AUTH_ERROR_ENDPOINT_RESULT

        SUT.fetchUserProfileSync(USER_ID)

        assertNull(usersCache.user)
    }

    @Test
    fun `fetchUserProfileSync server error then the user profile is not cached`(){
        userProfileHttpEndpointSync.result = SERVER_ERROR_ENDPOINT_RESULT

        SUT.fetchUserProfileSync(USER_ID)

        assertNull(usersCache.user)
    }

    @Test
    fun `fetchUserProfileSync endpoint error then the user profile is not cached`(){
        userProfileHttpEndpointSync.throwsError = true

        SUT.fetchUserProfileSync(USER_ID)

        assertNull(usersCache.user)
    }

    private class UserProfileHttpEndpointSyncTd: UserProfileHttpEndpointSync {
        var userId: String? = null
        var result: UserProfileHttpEndpointSync.EndpointResult? = null
        var throwsError = false

        override fun getUserProfile(userId: String?): UserProfileHttpEndpointSync.EndpointResult? {
            if(throwsError){
                throw NetworkErrorException()
            }
            this.userId = userId
            return result
        }

    }

    private class UsersCacheTd: UsersCache {
        var user: User? = null

        override fun getUser(userId: String?): User? {
            return if(userId == user?.userId) {
                user
            } else {
                null
            }
        }

        override fun cacheUser(user: User) {
            this.user = user
        }

    }
}