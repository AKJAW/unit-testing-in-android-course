package com.techyourchance.unittesting.screens.questiondetails

import com.techyourchance.unittesting.questions.FetchQuestionDetailsUseCase
import com.techyourchance.unittesting.questions.QuestionDetails
import com.techyourchance.unittesting.screens.common.screensnavigator.ScreensNavigator
import com.techyourchance.unittesting.screens.common.toastshelper.ToastsHelper

class QuestionDetailsController(
        private val fetchQuestionDetailsUseCase: FetchQuestionDetailsUseCase,
        private val screensNavigator: ScreensNavigator,
        private val toastsHelper: ToastsHelper
) : FetchQuestionDetailsUseCase.Listener, QuestionDetailsViewMvc.Listener{

    private lateinit var questionId: String
    private lateinit var viewMvc: QuestionDetailsViewMvc
    private var questionDetails: QuestionDetails? = null

    fun bindQuestionId(questionId: String) {
        this.questionId = questionId
    }

    fun bindView(viewMvc: QuestionDetailsViewMvc) {
        this.viewMvc = viewMvc
    }

    fun onStart() {
        viewMvc.registerListener(this)
        fetchQuestionDetailsUseCase.registerListener(this)

        val questionDetails = questionDetails
        if(questionDetails == null){
            viewMvc.showProgressIndication()
            fetchQuestionDetailsUseCase.fetchQuestionDetailsAndNotify(questionId)
        } else {
            onQuestionDetailsFetched(questionDetails)
        }
    }

    fun onStop() {
        viewMvc.unregisterListener(this)
        fetchQuestionDetailsUseCase.unregisterListener(this)
    }

    override fun onQuestionDetailsFetched(questionDetails: QuestionDetails) {
        viewMvc.hideProgressIndication()
        viewMvc.bindQuestion(questionDetails)
        this.questionDetails = questionDetails
    }

    override fun onQuestionDetailsFetchFailed() {
        viewMvc.hideProgressIndication()
        toastsHelper.showUseCaseError()
    }

    override fun onNavigateUpClicked() {
        screensNavigator.navigateUp()
    }

}