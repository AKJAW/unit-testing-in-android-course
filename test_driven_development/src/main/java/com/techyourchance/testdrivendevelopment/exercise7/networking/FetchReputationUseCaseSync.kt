package com.techyourchance.testdrivendevelopment.exercise7.networking

import com.techyourchance.testdrivendevelopment.exercise7.networking.GetReputationHttpEndpointSync.*
import java.lang.RuntimeException

class FetchReputationUseCaseSync(
        private val reputationHttpEndpointSync: GetReputationHttpEndpointSync
) {

    enum class Status {
        SUCCESS,
        FAILURE;
    }

    data class Result(val status: Status, val reputation: Int)

    fun fetchReputation(): Result {
        val endpointResult = reputationHttpEndpointSync.reputationSync

        return when(endpointResult.status){
            EndpointStatus.SUCCESS -> Result(Status.SUCCESS, endpointResult.reputation)
            EndpointStatus.GENERAL_ERROR,
            EndpointStatus.NETWORK_ERROR -> Result(Status.FAILURE, 0)
            else -> throw RuntimeException("Invalid endpoint status ${endpointResult.status}")
        }
    }
}