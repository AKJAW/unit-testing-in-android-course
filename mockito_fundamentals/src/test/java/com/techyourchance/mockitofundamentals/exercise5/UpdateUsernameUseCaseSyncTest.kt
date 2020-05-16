package com.techyourchance.mockitofundamentals.exercise5

import com.techyourchance.mockitofundamentals.exercise5.eventbus.EventBusPoster
import com.techyourchance.mockitofundamentals.exercise5.eventbus.UserDetailsChangedEvent
import com.techyourchance.mockitofundamentals.exercise5.networking.NetworkErrorException
import com.techyourchance.mockitofundamentals.exercise5.networking.UpdateUsernameHttpEndpointSync
import com.techyourchance.mockitofundamentals.exercise5.users.User
import com.techyourchance.mockitofundamentals.exercise5.users.UsersCache
import org.hamcrest.CoreMatchers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

class UpdateUsernameUseCaseSyncTest {

    companion object {
        private const val USERID = "123"
        private const val USERNAME = "aaa"
        val USER = User(USERID, USERNAME)
    }

    private val updateUsernameHttpEndpointSync: UpdateUsernameHttpEndpointSync = Mockito.mock(UpdateUsernameHttpEndpointSync::class.java)
    private val userCache: UsersCache = Mockito.mock(UsersCache::class.java)
    private val eventBusPoster: EventBusPoster = Mockito.mock(EventBusPoster::class.java)
    private lateinit var SUT: UpdateUsernameUseCaseSync

    @Before
    fun setUp(){
        Mockito.reset(updateUsernameHttpEndpointSync, userCache, eventBusPoster)
        SUT = UpdateUsernameUseCaseSync(updateUsernameHttpEndpointSync, userCache, eventBusPoster)
    }

    @Test
    fun `updateUsernameSync passed the userId and userName to the endpoint`(){
        success()
        val argumentCaptor = ArgumentCaptor.forClass(String::class.java)
        SUT.updateUsernameSync(USERID, USERNAME)
        Mockito.verify(updateUsernameHttpEndpointSync).updateUsername(argumentCaptor.capture(), argumentCaptor.capture())
        assertThat(argumentCaptor.allValues[0], CoreMatchers.`is`(USERID))
        assertThat(argumentCaptor.allValues[1], CoreMatchers.`is`(USERNAME))
    }

    @Test
    fun `updateUsernameSync endpoint SUCCESS then return SUCCESS`(){
        success()
        val result = SUT.updateUsernameSync(USERID, USERNAME)
        assertThat(result, CoreMatchers.`is`(UpdateUsernameUseCaseSync.UseCaseResult.SUCCESS))
    }

    //When AUTH_ERROR then return FAILURE
    @Test
    fun `updateUsernameSync endpoint AUTH_ERROR then return FAILURE`(){
        authError()
        val result = SUT.updateUsernameSync(USERID, USERNAME)
        assertThat(result, CoreMatchers.`is`(UpdateUsernameUseCaseSync.UseCaseResult.FAILURE))
    }

    //When SERVER_ERROR then return FAILURE
    @Test
    fun `updateUsernameSync endpoint SERVER_ERROR then return FAILURE`(){
        serverError()
        val result = SUT.updateUsernameSync(USERID, USERNAME)
        assertThat(result, CoreMatchers.`is`(UpdateUsernameUseCaseSync.UseCaseResult.FAILURE))
    }

    //When GENERAL_ERROR then return FAILURE
    @Test
    fun `updateUsernameSync endpoint GENERAL_ERROR then return FAILURE`(){
        generalError()
        val result = SUT.updateUsernameSync(USERID, USERNAME)
        assertThat(result, CoreMatchers.`is`(UpdateUsernameUseCaseSync.UseCaseResult.FAILURE))
    }

    //When NetworkErrorException then return NETWORK_ERROR
    @Test
    fun `updateUsernameSync endpoint NetworkErrorException then return NETWORK_ERROR`(){
        networkErrorException()
        val result = SUT.updateUsernameSync(USERID, USERNAME)
        assertThat(result, CoreMatchers.`is`(UpdateUsernameUseCaseSync.UseCaseResult.NETWORK_ERROR))
    }

    //When SUCCESS cache the user
    @Test
    fun `updateUsernameSync endpoint SUCCESS then cache the user`(){
        success()
        SUT.updateUsernameSync(USERID, USERNAME)
        Mockito.verify(userCache).cacheUser(USER)
    }

    //When AUTH_ERROR then dont cache user
    @Test
    fun `updateUsernameSync endpoint AUTH_ERROR then don't cache the user`(){
        authError()
        SUT.updateUsernameSync(USERID, USERNAME)
        Mockito.verifyZeroInteractions(userCache)
    }

    //When SERVER_ERROR then dont cache user
    @Test
    fun `updateUsernameSync endpoint SERVER_ERROR then don't cache the user`(){
        serverError()
        SUT.updateUsernameSync(USERID, USERNAME)
        Mockito.verifyZeroInteractions(userCache)
    }

    //When GENERAL_ERROR then dont cache user
    @Test
    fun `updateUsernameSync endpoint GENERAL_ERROR then don't cache the user`(){
        generalError()
        SUT.updateUsernameSync(USERID, USERNAME)
        Mockito.verifyZeroInteractions(userCache)
    }

    //When NetworkErrorException then dont cache user
    @Test
    fun `updateUsernameSync endpoint NetworkErrorException then don't cache the user`(){
        networkErrorException()
        SUT.updateUsernameSync(USERID, USERNAME)
        Mockito.verifyZeroInteractions(userCache)
    }

    //When SUCCESS post event
    @Test
    fun `updateUsernameSync endpoint SUCCESS then the correct event is posted`(){
        success()
        val argumentCaptor = ArgumentCaptor.forClass(UserDetailsChangedEvent::class.java)
        SUT.updateUsernameSync(USERID, USERNAME)
        Mockito.verify(eventBusPoster).postEvent(argumentCaptor.capture())
        assertThat(argumentCaptor.value, CoreMatchers.instanceOf(UserDetailsChangedEvent::class.java))
        assertThat(argumentCaptor.value.user, CoreMatchers.`is`(USER))
    }

    private fun success() {
        mockEndpoint(UpdateUsernameHttpEndpointSync.EndpointResultStatus.SUCCESS)
    }

    private fun authError() {
        mockEndpoint(UpdateUsernameHttpEndpointSync.EndpointResultStatus.AUTH_ERROR)
    }

    private fun serverError() {
        mockEndpoint(UpdateUsernameHttpEndpointSync.EndpointResultStatus.SERVER_ERROR)
    }

    private fun generalError() {
        mockEndpoint(UpdateUsernameHttpEndpointSync.EndpointResultStatus.GENERAL_ERROR)
    }

    private fun mockEndpoint(result: UpdateUsernameHttpEndpointSync.EndpointResultStatus) {
        Mockito.`when`(updateUsernameHttpEndpointSync.updateUsername(USERID, USERNAME))
                .thenReturn(
                        UpdateUsernameHttpEndpointSync.EndpointResult(
                                result,
                                USERID,
                                USERNAME
                        )
                )
    }

    private fun networkErrorException() {
        Mockito.`when`(updateUsernameHttpEndpointSync.updateUsername(USERID, USERNAME))
                .thenThrow(NetworkErrorException())
    }
}