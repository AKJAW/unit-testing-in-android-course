package com.techyourchance.testdrivendevelopment.exercise8

import com.techyourchance.testdrivendevelopment.exercise8.contacts.Contact
import com.techyourchance.testdrivendevelopment.exercise8.networking.ContactSchema
import com.techyourchance.testdrivendevelopment.exercise8.networking.GetContactsHttpEndpoint
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

class FetchContactsUseCaseTDTest {

    companion object {
        private val FILTER_TERM = "filter"
        private val ITEMS_SCHEMA = listOf(ContactSchema("ID", "NAME", "PHONE", "IMAGE", 1.0))
        private val ITEMS = listOf(Contact("ID", "NAME", "IMAGE"))
    }

    private lateinit var getContactsHttpEndpoint: GetContactsHttpEndpointTD
    private lateinit var listener1: FetchContactsUseCaseListenerTD
    private lateinit var listener2: FetchContactsUseCaseListenerTD
    private lateinit var SUT: FetchContactsUseCase

    @Before
    fun setUp(){
        getContactsHttpEndpoint = GetContactsHttpEndpointTD()
        SUT = FetchContactsUseCase(getContactsHttpEndpoint)

        listener1 = FetchContactsUseCaseListenerTD()
        SUT.registerListener(listener1)
        listener2 = FetchContactsUseCaseListenerTD()
        SUT.registerListener(listener2)
    }

    @Test
    fun `fetchContactsAndNotify passes FilterTerm to the endpoint`(){
        getContactsHttpEndpoint.success()
        SUT.fetchContactsAndNotify(FILTER_TERM)

        Assert.assertThat(getContactsHttpEndpoint.filter, CoreMatchers.`is`(FILTER_TERM))
    }

    @Test
    fun `fetchContactsAndNotify on success notifies registered listeners`(){
        getContactsHttpEndpoint.success()
        SUT.fetchContactsAndNotify(FILTER_TERM)

        Assert.assertThat(listener1.successValue, CoreMatchers.`is`(ITEMS))
        Assert.assertThat(listener2.successValue, CoreMatchers.`is`(ITEMS))
    }

    @Test
    fun `fetchContactsAndNotify on success doesn't notify unregistered listeners`(){
        getContactsHttpEndpoint.success()
        SUT.unregisterListener(listener2)

        SUT.fetchContactsAndNotify(FILTER_TERM)

        Assert.assertThat(listener1.successValue, CoreMatchers.`is`(ITEMS))
        Assert.assertThat(listener2.successValue, CoreMatchers.nullValue())
    }

    @Test
    fun `fetchContactsAndNotify endpoint general error notifies of failure`(){
        getContactsHttpEndpoint.generalError()

        SUT.fetchContactsAndNotify(FILTER_TERM)

        Assert.assertThat(listener1.failValue, CoreMatchers.`is`(FetchContactsUseCase.Error.FAILURE))
        Assert.assertThat(listener2.failValue, CoreMatchers.`is`(FetchContactsUseCase.Error.FAILURE))
    }

    @Test
    fun `fetchContactsAndNotify endpoint network error notifies of network error`(){
        getContactsHttpEndpoint.networkError()

        SUT.fetchContactsAndNotify(FILTER_TERM)

        Assert.assertThat(listener1.failValue, CoreMatchers.`is`(FetchContactsUseCase.Error.NETWORK_ERROR))
        Assert.assertThat(listener2.failValue, CoreMatchers.`is`(FetchContactsUseCase.Error.NETWORK_ERROR))
    }

    private class GetContactsHttpEndpointTD: GetContactsHttpEndpoint {
        private var willSucceed = false
        private var failReason: GetContactsHttpEndpoint.FailReason? = null
        lateinit var filter: String

        override fun getContacts(filterTerm: String, callback: GetContactsHttpEndpoint.Callback) {
            filter = filterTerm
            if(willSucceed){
                callback.onGetContactsSucceeded(ITEMS_SCHEMA)
            } else {
                callback.onGetContactsFailed(failReason!!)
            }
        }

        fun success(){
            willSucceed = true
        }

        fun generalError(){
            failReason = GetContactsHttpEndpoint.FailReason.GENERAL_ERROR
        }

        fun networkError(){
            failReason = GetContactsHttpEndpoint.FailReason.NETWORK_ERROR
        }
    }

    private class FetchContactsUseCaseListenerTD: FetchContactsUseCase.Listener {
        var successValue: List<Contact>? = null
        var failValue: FetchContactsUseCase.Error? = null

        override fun onSuccess(items: List<Contact>) {
            successValue = items
        }

        override fun onFail(error: FetchContactsUseCase.Error) {
            failValue = error
        }
    }
}