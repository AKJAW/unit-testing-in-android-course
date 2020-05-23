package com.techyourchance.testdrivendevelopment.exercise6

import com.techyourchance.testdrivendevelopment.exercise6.networking.FetchUserHttpEndpointSync
import com.techyourchance.testdrivendevelopment.exercise6.networking.NetworkErrorException
import com.techyourchance.testdrivendevelopment.exercise6.users.User
import com.techyourchance.testdrivendevelopment.exercise6.users.UsersCache
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

class FetchUserUseCaseSyncImplTest {

    companion object {
        private const val USER_ID = "USER_ID"
        private const val USER_NAME = "USER_NAME"

        private val USER = User(USER_ID, USER_NAME)

        private val SUCCESS_ENDPOINT_RESULT = FetchUserHttpEndpointSync.EndpointResult(
                FetchUserHttpEndpointSync.EndpointStatus.SUCCESS,
                USER_ID,
                USER_NAME
        )

        private val AUTH_ERROR_ENDPOINT_RESULT = FetchUserHttpEndpointSync.EndpointResult(
                FetchUserHttpEndpointSync.EndpointStatus.AUTH_ERROR,
                null,
                null
        )

        private val GENERAL_ERROR_ENDPOINT_RESULT = FetchUserHttpEndpointSync.EndpointResult(
                FetchUserHttpEndpointSync.EndpointStatus.GENERAL_ERROR,
                null,
                null
        )
    }

    private val userCache: UsersCache = Mockito.mock(UsersCache::class.java)
    private val fetchUserHttpEndpointSync: FetchUserHttpEndpointSync = Mockito.mock(FetchUserHttpEndpointSync::class.java)
    lateinit var SUT: FetchUserUseCaseSync

    @Before
    fun setUp(){
        Mockito.reset(userCache, fetchUserHttpEndpointSync)
        SUT = FetchUserUseCaseSyncImpl(userCache, fetchUserHttpEndpointSync)
    }

    //the userid is passed to the endpoint
    @Test
    fun `fetchUserSync passes the credentials to the endpoint`(){
        prepareCacheReturn(null)
        prepareEndpointReturn(SUCCESS_ENDPOINT_RESULT)

        val ac = ArgumentCaptor.forClass(String::class.java)
        SUT.fetchUserSync(USER_ID)
        Mockito.verify(fetchUserHttpEndpointSync).fetchUserSync(ac.capture())

        Assert.assertThat(ac.value, CoreMatchers.`is`(USER_ID))
    }

    //if endpoint success then return sucess
    @Test
    fun `fetchUserSync endpoint returned success then usecase returns success`(){
        prepareCacheReturn(null)
        prepareEndpointReturn(SUCCESS_ENDPOINT_RESULT)

        val result = SUT.fetchUserSync(USER_ID)
        Assert.assertThat(result, CoreMatchers.`is`(FetchUserUseCaseSync.UseCaseResult(FetchUserUseCaseSync.Status.SUCCESS, USER)))
    }
    //if endpoint Auth_error then return failure
    @Test
    fun `fetchUserSync endpoint returned auth_error then usecase returns failure`(){
        prepareCacheReturn(null)
        prepareEndpointReturn(AUTH_ERROR_ENDPOINT_RESULT)

        val result = SUT.fetchUserSync(USER_ID)
        Assert.assertThat(result, CoreMatchers.`is`(FetchUserUseCaseSync.UseCaseResult(FetchUserUseCaseSync.Status.FAILURE, null)))
    }
    //if endpoint Gneelr erro thn return failur
    @Test
    fun `fetchUserSync endpoint returned general_error then usecase returns failure`(){
        prepareCacheReturn(null)
        prepareEndpointReturn(GENERAL_ERROR_ENDPOINT_RESULT)

        val result = SUT.fetchUserSync(USER_ID)
        Assert.assertThat(result, CoreMatchers.`is`(FetchUserUseCaseSync.UseCaseResult(FetchUserUseCaseSync.Status.FAILURE, null)))
    }
    //if endpoint throws error then NETWORK_ERROR
    @Test
    fun `fetchUserSync endpoint throws error then usecase returns network_error`(){
        prepareCacheReturn(null)
        Mockito.`when`(fetchUserHttpEndpointSync.fetchUserSync(USER_ID))
                .thenThrow(NetworkErrorException())

        val result = SUT.fetchUserSync(USER_ID)
        Assert.assertThat(result, CoreMatchers.`is`(FetchUserUseCaseSync.UseCaseResult(FetchUserUseCaseSync.Status.NETWORK_ERROR, null)))
    }

    //if user not in cache the he is fetched from the endpoint
    @Test
    fun `fetchUserSync cache empty then call the endpoint`(){
        prepareCacheReturn(null)
        prepareEndpointReturn(SUCCESS_ENDPOINT_RESULT)
        SUT.fetchUserSync(USER_ID)

        Mockito.verify(fetchUserHttpEndpointSync).fetchUserSync(USER_ID)
    }
    //if user fetched from endpoint then cache him
    @Test
    fun `fetchUserSync endpoint result user is cached`(){
        prepareCacheReturn(null)
        prepareEndpointReturn(SUCCESS_ENDPOINT_RESULT)
        SUT.fetchUserSync(USER_ID)

        Mockito.verify(userCache).cacheUser(USER)
    }
    //if user in cache then return success
    @Test
    fun `fetchUserSync cache is used to return user`(){
        prepareCacheReturn(USER)
        val result = SUT.fetchUserSync(USER_ID)
        Assert.assertThat(result, CoreMatchers.`is`(FetchUserUseCaseSync.UseCaseResult(FetchUserUseCaseSync.Status.SUCCESS, USER)))
    }

    //if the user in cache then it is returned from there without calling the endpoint
    @Test
    fun `fetchUserSync cache present then endpoint not called`(){
        prepareCacheReturn(USER)
        SUT.fetchUserSync(USER_ID)
        Mockito.verifyZeroInteractions(fetchUserHttpEndpointSync)
    }
    //if there is no user in the endpoint then null is returned
    @Test
    fun `fetchUserSync returns null if endpoint returns no user`(){
        prepareCacheReturn(null)
        Mockito.`when`(fetchUserHttpEndpointSync.fetchUserSync(USER_ID))
                .thenReturn(FetchUserHttpEndpointSync.EndpointResult(FetchUserHttpEndpointSync.EndpointStatus.SUCCESS, null, null))
        val result = SUT.fetchUserSync(USER_ID)
        Assert.assertThat(result, CoreMatchers.`is`(FetchUserUseCaseSync.UseCaseResult(FetchUserUseCaseSync.Status.SUCCESS, null)))
    }

    private fun prepareEndpointReturn(result: FetchUserHttpEndpointSync.EndpointResult){
        Mockito.`when`(fetchUserHttpEndpointSync.fetchUserSync(USER_ID))
                .thenReturn(result)
    }

    private fun prepareCacheReturn(user: User?) {
        Mockito.`when`(userCache.getUser(USER_ID))
                .thenReturn(user)
    }


}