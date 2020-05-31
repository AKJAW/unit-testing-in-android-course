package com.techyourchance.unittesting.questions

import com.techyourchance.unittesting.common.time.TimeProvider
import com.techyourchance.unittesting.networking.questions.FetchQuestionDetailsEndpoint
import com.techyourchance.unittesting.networking.questions.QuestionSchema
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.internal.verification.Times

class FetchQuestionDetailsUseCaseTest {

    companion object {
        private const val QUESTION_ID = "ID"
        private val QUESTION_SCHEMA = QuestionSchema("title", "id", "body")
        private val QUESTION_DETAILS = QuestionDetails("id", "title", "body")
    }

    private val fetchQuestionDetailsEndpoint: FetchQuestionDetailsEndpoint = mock(FetchQuestionDetailsEndpoint::class.java)
    private val timeProvider: TimeProvider = mock(TimeProvider::class.java)
    private lateinit var SUT: FetchQuestionDetailsUseCase
    private val listener1: FetchQuestionDetailsUseCase.Listener = mock(FetchQuestionDetailsUseCase.Listener::class.java)
    private val listener2: FetchQuestionDetailsUseCase.Listener = mock(FetchQuestionDetailsUseCase.Listener::class.java)

    @Before
    fun setUp(){
        SUT = FetchQuestionDetailsUseCase(fetchQuestionDetailsEndpoint, timeProvider)

        SUT.registerListener(listener1)
        SUT.registerListener(listener2)
    }

    @Test
    fun `fetchQuestionDetailsAndNotify success notifies registered listeners with data`(){
        success()

        SUT.fetchQuestionDetailsAndNotify(QUESTION_ID)

        verify(listener1).onQuestionDetailsFetched(QUESTION_DETAILS)
        verify(listener2).onQuestionDetailsFetched(QUESTION_DETAILS)
    }

    @Test
    fun `fetchQuestionDetailsAndNotify success doesn't notify unregistered listeners`(){
        success()
        SUT.unregisterListener(listener2)
        SUT.fetchQuestionDetailsAndNotify(QUESTION_ID)

        verify(listener1).onQuestionDetailsFetched(QUESTION_DETAILS)
        verifyZeroInteractions(listener2)
    }

    @Test
    fun `fetchQuestionDetailsAndNotify failure notifies registered listeners of the failure`(){
        failure()

        SUT.fetchQuestionDetailsAndNotify(QUESTION_ID)

        verify(listener1).onQuestionDetailsFetchFailed()
        verify(listener2).onQuestionDetailsFetchFailed()
    }

    @Test
    fun `fetchQuestionDetailsAndNotify failure doesn't notify unregistered listeners`(){
        failure()

        SUT.unregisterListener(listener2)
        SUT.fetchQuestionDetailsAndNotify(QUESTION_ID)

        verify(listener1).onQuestionDetailsFetchFailed()
        verifyZeroInteractions(listener2)
    }

    @Test
    fun `fetchQuestionDetailsAndNotify uses the cache if cache timeout was not exceeded`(){
        success()

        `when`(timeProvider.currentTimestamp).thenReturn(0)
        SUT.fetchQuestionDetailsAndNotify(QUESTION_ID)

        `when`(timeProvider.currentTimestamp).thenReturn(59990)
        SUT.fetchQuestionDetailsAndNotify(QUESTION_ID)

        verify(fetchQuestionDetailsEndpoint, Times(1)).fetchQuestionDetails(eq(QUESTION_ID), ArgumentMatchers.any())
        verify(listener1, Times(2)).onQuestionDetailsFetched(QUESTION_DETAILS)
        verify(listener2, Times(2)).onQuestionDetailsFetched(QUESTION_DETAILS)
    }

    @Test
    fun `fetchQuestionDetailsAndNotify doesn't use the cache if the cache timeout was exceeded`(){
        success()

        `when`(timeProvider.currentTimestamp).thenReturn(0)
        SUT.fetchQuestionDetailsAndNotify(QUESTION_ID)

        `when`(timeProvider.currentTimestamp).thenReturn(60000)
        SUT.fetchQuestionDetailsAndNotify(QUESTION_ID)

        verify(fetchQuestionDetailsEndpoint, Times(2)).fetchQuestionDetails(eq(QUESTION_ID), ArgumentMatchers.any())
        verify(listener1, Times(2)).onQuestionDetailsFetched(QUESTION_DETAILS)
        verify(listener2, Times(2)).onQuestionDetailsFetched(QUESTION_DETAILS)
    }

    private fun success() {
        prepareEndpoint { listener ->
            listener.onQuestionDetailsFetched(QUESTION_SCHEMA)
        }
    }

    private fun failure() {
        prepareEndpoint { listener ->
            listener.onQuestionDetailsFetchFailed()
        }
    }

    private fun prepareEndpoint(onAction: (FetchQuestionDetailsEndpoint.Listener) -> Unit) {
        `when`(fetchQuestionDetailsEndpoint.fetchQuestionDetails(
                ArgumentMatchers.eq(QUESTION_ID),
                any(FetchQuestionDetailsEndpoint.Listener::class.java))
        ).thenAnswer {
            val listener = it.getArgument<FetchQuestionDetailsEndpoint.Listener>(1)
            onAction(listener)
        }
    }
}