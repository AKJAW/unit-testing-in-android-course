package com.techyourchance.unittesting.questions

import com.techyourchance.unittesting.common.BaseObservable
import com.techyourchance.unittesting.common.time.TimeProvider
import com.techyourchance.unittesting.networking.questions.FetchQuestionDetailsEndpoint
import com.techyourchance.unittesting.networking.questions.QuestionSchema

open class FetchQuestionDetailsUseCase(
        private val mFetchQuestionDetailsEndpoint: FetchQuestionDetailsEndpoint,
        private val mTimeProvider: TimeProvider
) : BaseObservable<FetchQuestionDetailsUseCase.Listener?>() {

    interface Listener {
        fun onQuestionDetailsFetched(questionDetails: QuestionDetails)
        fun onQuestionDetailsFetchFailed()
    }

    companion object {
        private const val CACHE_TIMEOUT_MS = 60 * 1000
    }

    private data class CachedDetails(val questionDetails: QuestionDetails, val timeStamp: Long)

    private val cache: MutableMap<String, CachedDetails> = mutableMapOf()

    open fun fetchQuestionDetailsAndNotify(questionId: String) {
        val cachedData = cache[questionId]

        when {
            cachedData == null -> fetchFromEndpoint(questionId)
            isCachedValid(cachedData.timeStamp) -> notifySuccess(cachedData.questionDetails)
            else -> {
                invalidateCache(questionId)
                fetchFromEndpoint(questionId)
            }
        }
    }

    private fun fetchFromEndpoint(questionId: String) {
        mFetchQuestionDetailsEndpoint.fetchQuestionDetails(questionId, object : FetchQuestionDetailsEndpoint.Listener {
            override fun onQuestionDetailsFetched(question: QuestionSchema) {
                val details = QuestionDetails( question.id, question.title, question.body)
                cache[questionId] = CachedDetails(details, mTimeProvider.currentTimestamp)
                notifySuccess(details)
            }

            override fun onQuestionDetailsFetchFailed() {
                notifyFailure()
            }
        })
    }

    private fun isCachedValid(timeStamp: Long): Boolean {
        return mTimeProvider.currentTimestamp < timeStamp + CACHE_TIMEOUT_MS
    }

    private fun invalidateCache(questionId: String) {
        cache.remove(questionId)
    }

    private fun notifySuccess(questionDetails: QuestionDetails) {
        listeners.forEach { listener ->
            listener!!.onQuestionDetailsFetched(questionDetails)
        }
    }

    private fun notifyFailure() {
        listeners.forEach { listener ->
            listener!!.onQuestionDetailsFetchFailed()
        }
    }

}