package com.techyourchance.unittesting.screens.questiondetails

import com.techyourchance.unittesting.questions.FetchQuestionDetailsUseCase
import com.techyourchance.unittesting.questions.QuestionDetails
import com.techyourchance.unittesting.screens.common.screensnavigator.ScreensNavigator
import com.techyourchance.unittesting.screens.common.toastshelper.ToastsHelper
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.lang.RuntimeException

class QuestionDetailsControllerTest {

    companion object {
        private const val QUESTION_ID = "ID"
        private val QUESTION_DETAIL = QuestionDetails("id1", "title1", "body1")    }

    private lateinit var fetchQuestionDetailsUseCaseTD: FetchQuestionDetailsUseCaseTD
    private val screensNavigator: ScreensNavigator = mock(ScreensNavigator::class.java)
    private val toastsHelper: ToastsHelper = mock(ToastsHelper::class.java)
    private val viewMvc: QuestionDetailsViewMvc = mock(QuestionDetailsViewMvc::class.java)
    private lateinit var SUT: QuestionDetailsController

    @Before
    fun setUp(){
        Mockito.reset(screensNavigator, toastsHelper)

        fetchQuestionDetailsUseCaseTD = FetchQuestionDetailsUseCaseTD()
        SUT = QuestionDetailsController(fetchQuestionDetailsUseCaseTD, screensNavigator, toastsHelper)
        SUT.bindView(viewMvc)
        SUT.bindQuestionId(QUESTION_ID)

        success()
    }

    @Test
    fun `onStart fetches the question details with the bound question ID`(){
        SUT.bindQuestionId(QUESTION_ID)
        SUT.onStart()

        assertThat(fetchQuestionDetailsUseCaseTD.callCount, CoreMatchers.`is`(1))
    }

    @Test
    fun `onStart only fetches the question details once`(){
        SUT.bindQuestionId(QUESTION_ID)
        SUT.onStart()
        SUT.onStart()

        assertThat(fetchQuestionDetailsUseCaseTD.callCount, CoreMatchers.`is`(1))
    }

    @Test
    fun `onStart registers as a FetchQuestionDetailsUseCase listener`(){
        SUT.onStart()

        assertThat(fetchQuestionDetailsUseCaseTD.verifyRegisteredListener(SUT), CoreMatchers.`is`(true))
    }

    @Test
    fun `onStart registers as a ViewMvc listener`(){
        SUT.onStart()

        verify(viewMvc).registerListener(SUT)
    }

    @Test
    fun `onStart shows a loading indicator ViewMvc`(){
        SUT.onStart()

        verify(viewMvc).showProgressIndication()
    }

    @Test
    fun `onStop unregisters listener from FetchQuestionDetailsUseCase`(){
        SUT.onStop()

        assertThat(fetchQuestionDetailsUseCaseTD.verifyUnRegisteredListener(SUT), CoreMatchers.`is`(true))
    }

    @Test
    fun `onStop unregisters listener from ViewMvc`(){
        SUT.onStop()

        verify(viewMvc).unregisterListener(SUT)
    }

    @Test
    fun `onStart FetchQuestionDetailsUseCase success binds the question details to ViewMvc`(){
        success()

        SUT.onStart()

        verify(viewMvc).bindQuestion(QUESTION_DETAIL)
    }

    @Test
    fun `onStart FetchQuestionDetailsUseCase success hides the loading indicator`(){
        success()

        SUT.onStart()

        verify(viewMvc).hideProgressIndication()
    }

    @Test
    fun `onStart FetchQuestionDetailsUseCase failure shows an error toast`(){
        failure()

        SUT.onStart()

        verify(toastsHelper).showUseCaseError()
    }

    @Test
    fun `onStart FetchQuestionDetailsUseCase failure hides the loading indicator`(){
        failure()

        SUT.onStart()

        verify(viewMvc).hideProgressIndication()
    }

    @Test
    fun `onNavigateUpClicked navigates back`(){
        SUT.onNavigateUpClicked()

        verify(screensNavigator).navigateUp()
    }

    private fun success() {
        fetchQuestionDetailsUseCaseTD.isSuccess = true
    }

    private fun failure() {
        fetchQuestionDetailsUseCaseTD.isSuccess = false
    }

    class FetchQuestionDetailsUseCaseTD: FetchQuestionDetailsUseCase(null) {
        var isSuccess: Boolean? = null
        var callCount: Int = 0

        override fun fetchQuestionDetailsAndNotify(questionId: String?) {
            callCount++

            when(isSuccess){
                true -> {
                    listeners.forEach { it.onQuestionDetailsFetched(QUESTION_DETAIL) }
                }
                false -> {
                    listeners.forEach { it.onQuestionDetailsFetchFailed() }
                }
                else -> throw RuntimeException("isSuccess is not set")
            }
        }

        fun verifyRegisteredListener(listener: Listener): Boolean =
                listeners.find { it === listener } != null

        fun verifyUnRegisteredListener(listener: Listener): Boolean =
                listeners.find { it === listener } === null
    }
}