package com.techyourchance.testdrivendevelopment.exercise6

import com.techyourchance.testdrivendevelopment.exercise6.networking.FetchUserHttpEndpointSync
import com.techyourchance.testdrivendevelopment.exercise6.networking.NetworkErrorException
import com.techyourchance.testdrivendevelopment.exercise6.users.User
import com.techyourchance.testdrivendevelopment.exercise6.users.UsersCache

class FetchUserUseCaseSyncImpl(
        private val userCache: UsersCache,
        private val fetchUserHttpEndpointSync: FetchUserHttpEndpointSync
) : FetchUserUseCaseSync {
    override fun fetchUserSync(userId: String): FetchUserUseCaseSync.UseCaseResult {
        val cachedUser = userCache.getUser(userId)
        if (cachedUser != null) {
            return FetchUserUseCaseSync.UseCaseResult(
                    FetchUserUseCaseSync.Status.SUCCESS,
                    cachedUser
            )
        }

        try {
            val result = fetchUserHttpEndpointSync.fetchUserSync(userId)

            if (result.status == FetchUserHttpEndpointSync.EndpointStatus.GENERAL_ERROR ||
                    result.status == FetchUserHttpEndpointSync.EndpointStatus.AUTH_ERROR) {
                return FetchUserUseCaseSync.UseCaseResult(
                        FetchUserUseCaseSync.Status.FAILURE,
                        null
                )
            }

            if (result.userId == null || result.username == null) {
                return FetchUserUseCaseSync.UseCaseResult(
                        FetchUserUseCaseSync.Status.SUCCESS,
                        null
                )
            }

            val user = User(result.userId, result.username)
            userCache.cacheUser(user)

            return FetchUserUseCaseSync.UseCaseResult(
                    FetchUserUseCaseSync.Status.SUCCESS,
                    user

            )
        } catch (e: NetworkErrorException) {
            return FetchUserUseCaseSync.UseCaseResult(
                    FetchUserUseCaseSync.Status.NETWORK_ERROR,
                    null
            )
        }
    }
}