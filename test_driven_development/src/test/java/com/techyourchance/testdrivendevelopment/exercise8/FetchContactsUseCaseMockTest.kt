package com.techyourchance.testdrivendevelopment.exercise8

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.techyourchance.testdrivendevelopment.example11.cart.CartItem
import com.techyourchance.testdrivendevelopment.exercise8.contacts.Contact
import com.techyourchance.testdrivendevelopment.exercise8.networking.ContactSchema
import com.techyourchance.testdrivendevelopment.exercise8.networking.GetContactsHttpEndpoint
import com.techyourchance.testdrivendevelopment.exercise8.networking.GetContactsHttpEndpoint.Callback
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*


class FetchContactsUseCaseMockTest {

    companion object {
        private val FILTER_TERM = "filter"
        private val ITEMS_SCHEMA = listOf(ContactSchema("ID", "NAME", "PHONE", "IMAGE", 1.0))
        private val ITEMS = listOf(Contact("ID", "NAME", "IMAGE"))
    }

    private val getContactsHttpEndpoint: GetContactsHttpEndpoint = mock(GetContactsHttpEndpoint::class.java)
    private val listener1: FetchContactsUseCase.Listener = mock(FetchContactsUseCase.Listener::class.java)
    private val listener2: FetchContactsUseCase.Listener = mock(FetchContactsUseCase.Listener::class.java)
    private lateinit var SUT: FetchContactsUseCase

    @Before
    fun setUp(){
        reset(getContactsHttpEndpoint, listener1, listener2)

        SUT = FetchContactsUseCase(getContactsHttpEndpoint)
        SUT.registerListener(listener1)
        SUT.registerListener(listener2)
    }

    @Test
    fun `fetchContactsAndNotify passes FilterTerm to the endpoint`(){
        success()
        SUT.fetchContactsAndNotify(FILTER_TERM)

        val ac = ArgumentCaptor.forClass(String::class.java)
        verify(getContactsHttpEndpoint).getContacts(ac.capture(), any(Callback::class.java))
        Assert.assertThat(ac.value, CoreMatchers.`is`(FILTER_TERM))
    }

    @Test
    fun `fetchContactsAndNotify on success notifies registered listeners`(){
        success()
        SUT.fetchContactsAndNotify(FILTER_TERM)

        val ac = argumentCaptor<List<Contact>>()
        verify(listener1).onSuccess(ac.capture())
        verify(listener2).onSuccess(ac.capture())
        Assert.assertThat(ac.allValues[0], CoreMatchers.`is`(ITEMS))
        Assert.assertThat(ac.allValues[1], CoreMatchers.`is`(ITEMS))
    }

    @Test
    fun `fetchContactsAndNotify on success doesn't notify unregistered listeners`(){
        success()
        SUT.unregisterListener(listener2)

        SUT.fetchContactsAndNotify(FILTER_TERM)

        val ac = argumentCaptor<List<Contact>>()
        verify(listener1).onSuccess(ac.capture())
        verifyZeroInteractions(listener2)
        Assert.assertThat(ac.allValues[0], CoreMatchers.`is`(ITEMS))
    }

    @Test
    fun `fetchContactsAndNotify endpoint general error notifies of failure`(){
        generalError()

        SUT.fetchContactsAndNotify(FILTER_TERM)

        val ac = argumentCaptor<FetchContactsUseCase.Error>()
        verify(listener1).onFail(ac.capture())
        verify(listener2).onFail(ac.capture())
        Assert.assertThat(ac.allValues[0], CoreMatchers.`is`(FetchContactsUseCase.Error.FAILURE))
        Assert.assertThat(ac.allValues[1], CoreMatchers.`is`(FetchContactsUseCase.Error.FAILURE))
    }

    @Test
    fun `fetchContactsAndNotify endpoint network error notifies of network error`(){
        networkError()
        SUT.fetchContactsAndNotify(FILTER_TERM)

        val ac = argumentCaptor<FetchContactsUseCase.Error>()
        verify(listener1).onFail(ac.capture())
        verify(listener2).onFail(ac.capture())
        Assert.assertThat(ac.allValues[0], CoreMatchers.`is`(FetchContactsUseCase.Error.NETWORK_ERROR))
        Assert.assertThat(ac.allValues[1], CoreMatchers.`is`(FetchContactsUseCase.Error.NETWORK_ERROR))
    }

    private fun success() {
        prepareEndpointCallback { callback ->
            callback.onGetContactsSucceeded(ITEMS_SCHEMA)
        }
    }

    private fun networkError() {
        prepareEndpointCallback { callback ->
            callback.onGetContactsFailed(GetContactsHttpEndpoint.FailReason.NETWORK_ERROR)
        }
    }

    private fun generalError() {
        prepareEndpointCallback { callback ->
            callback.onGetContactsFailed(GetContactsHttpEndpoint.FailReason.GENERAL_ERROR)
        }
    }

    private fun prepareEndpointCallback(runOnInvocation: (Callback) -> Unit){
        `when`(getContactsHttpEndpoint.getContacts(eq(FILTER_TERM), any(Callback::class.java)))
                .thenAnswer {
                    val callback = it.arguments[1] as Callback
                    runOnInvocation(callback)
                }
    }

}