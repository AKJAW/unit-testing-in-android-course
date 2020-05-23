package com.techyourchance.testdrivendevelopment.exercise7.networking

import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class FetchReputationUseCaseSyncTest {

    companion object {
        private const val SUCCESS_REPUTATION = 5
        private const val FAILURE_REPUTATION = 0
    }

    private val getReputationHttpEndpointSync: GetReputationHttpEndpointSync = Mockito.mock(GetReputationHttpEndpointSync::class.java)
    private lateinit var fetchReputationUseCaseSync: FetchReputationUseCaseSync

    @Before
    fun setUp(){
        fetchReputationUseCaseSync = FetchReputationUseCaseSync(getReputationHttpEndpointSync)
    }

    @Test
    fun `fetchReputation endpoint returns success then return success`(){
        success()
        val result = fetchReputationUseCaseSync.fetchReputation()
        Assert.assertThat(result.status, CoreMatchers.`is`(FetchReputationUseCaseSync.Status.SUCCESS))
    }

    @Test
    fun `fetchReputation endpoint returns success then return the reputation`(){
        success()
        val result = fetchReputationUseCaseSync.fetchReputation()
        Assert.assertThat(result.reputation, CoreMatchers.`is`(SUCCESS_REPUTATION))
    }

    @Test
    fun `fetchReputation endpoint returns general error then return failure`(){
        generalError()
        val result = fetchReputationUseCaseSync.fetchReputation()
        Assert.assertThat(result.status, CoreMatchers.`is`(FetchReputationUseCaseSync.Status.FAILURE))
    }

    @Test
    fun `fetchReputation endpoint returns general error then reputation is equal to zero`(){
        generalError()
        val result = fetchReputationUseCaseSync.fetchReputation()
        Assert.assertThat(result.reputation, CoreMatchers.`is`(FAILURE_REPUTATION))
    }

    @Test
    fun `fetchReputation endpoint returns network error then return failure`(){
        networkError()
        val result = fetchReputationUseCaseSync.fetchReputation()
        Assert.assertThat(result.status, CoreMatchers.`is`(FetchReputationUseCaseSync.Status.FAILURE))
    }

    @Test
    fun `fetchReputation endpoint returns network error then reputation is equal to zero`(){
        networkError()
        val result = fetchReputationUseCaseSync.fetchReputation()
        Assert.assertThat(result.reputation, CoreMatchers.`is`(FAILURE_REPUTATION))
    }

    private fun success() {
        prepareEndpoint(
                GetReputationHttpEndpointSync.EndpointStatus.SUCCESS,
                SUCCESS_REPUTATION
        )
    }

    private fun generalError() {
        prepareEndpoint(
                GetReputationHttpEndpointSync.EndpointStatus.GENERAL_ERROR
        )
    }

    private fun networkError() {
        prepareEndpoint(
                GetReputationHttpEndpointSync.EndpointStatus.NETWORK_ERROR
        )
    }

    private fun prepareEndpoint(status: GetReputationHttpEndpointSync.EndpointStatus, reputation: Int = -5){
        Mockito.`when`(getReputationHttpEndpointSync.reputationSync)
                .thenReturn(
                        GetReputationHttpEndpointSync.EndpointResult(
                                status,
                                reputation
                        )
                )
    }
}