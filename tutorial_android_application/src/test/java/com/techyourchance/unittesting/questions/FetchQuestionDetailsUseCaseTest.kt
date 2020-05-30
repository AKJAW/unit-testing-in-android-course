package com.techyourchance.unittesting.questions

import com.techyourchance.unittesting.networking.questions.FetchQuestionDetailsEndpoint
import com.techyourchance.unittesting.networking.questions.QuestionSchema
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

class FetchQuestionDetailsUseCaseTest {

    companion object {
        private const val QUESTION_ID = "id"
        private val SCHEMA = QuestionSchema("title", "id", "body")
        private val DETAILS = QuestionDetails("id", "title", "body")
    }

    private val fetchQuestionDetailsEndpoint: FetchQuestionDetailsEndpoint = Mockito.mock(FetchQuestionDetailsEndpoint::class.java)
    private val listener1: FetchQuestionDetailsUseCase.Listener = Mockito.mock(FetchQuestionDetailsUseCase.Listener::class.java)
    private val listener2: FetchQuestionDetailsUseCase.Listener = Mockito.mock(FetchQuestionDetailsUseCase.Listener::class.java)
    private lateinit var SUT: FetchQuestionDetailsUseCase

    @Before
    fun setUp(){
        Mockito.reset(fetchQuestionDetailsEndpoint, listener1, listener2)

        SUT = FetchQuestionDetailsUseCase(fetchQuestionDetailsEndpoint)

        SUT.registerListener(listener1)
        SUT.registerListener(listener2)
    }

    @Test
    fun `fetchQuestionDetailsAndNotify success notifies the listeners with correct data`(){
        success()
        val ac = ArgumentCaptor.forClass(QuestionDetails::class.java)

        SUT.fetchQuestionDetailsAndNotify(QUESTION_ID)

        Mockito.verify(listener1).onQuestionDetailsFetched(ac.capture())
        Mockito.verify(listener2).onQuestionDetailsFetched(ac.capture())

        val (firstValue, secondValue) = ac.allValues
        Assert.assertThat(firstValue, CoreMatchers.`is`(DETAILS))
        Assert.assertThat(secondValue, CoreMatchers.`is`(DETAILS))
    }

    @Test
    fun `fetchQuestionDetailsAndNotify success doesn't notify unsubscribed listeners`(){
        success()
        SUT.unregisterListener(listener2)

        SUT.fetchQuestionDetailsAndNotify(QUESTION_ID)

        Mockito.verify(listener1).onQuestionDetailsFetched(Mockito.any())
        Mockito.verifyZeroInteractions(listener2)
    }

    @Test
    fun `fetchQuestionDetailsAndNotify failure notifies listeners`(){
        failure()

        SUT.fetchQuestionDetailsAndNotify(QUESTION_ID)

        Mockito.verify(listener1).onQuestionDetailsFetchFailed()
        Mockito.verify(listener2).onQuestionDetailsFetchFailed()
    }

    @Test
    fun `fetchQuestionDetailsAndNotify failure doesn't notify unsubscribed listeners`(){
        failure()
        SUT.unregisterListener(listener2)

        SUT.fetchQuestionDetailsAndNotify(QUESTION_ID)

        Mockito.verify(listener1).onQuestionDetailsFetchFailed()
        Mockito.verifyZeroInteractions(listener2)
    }

    private fun success() {
        prepareEndpoint { listener ->
            listener.onQuestionDetailsFetched(SCHEMA)
        }
    }

    private fun failure() {
        prepareEndpoint { listener ->
            listener.onQuestionDetailsFetchFailed()
        }
    }

    private fun prepareEndpoint(doWithListener: (FetchQuestionDetailsEndpoint.Listener) -> Unit) {
        Mockito.`when`(fetchQuestionDetailsEndpoint.fetchQuestionDetails(Mockito.eq(QUESTION_ID), Mockito.any()))
                .thenAnswer {
                    val listener = it.getArgument<FetchQuestionDetailsEndpoint.Listener>(1)
                    doWithListener(listener)
                }
    }
}