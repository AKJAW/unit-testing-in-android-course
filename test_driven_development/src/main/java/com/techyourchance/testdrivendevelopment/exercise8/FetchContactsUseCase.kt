package com.techyourchance.testdrivendevelopment.exercise8

import com.techyourchance.testdrivendevelopment.exercise8.contacts.Contact
import com.techyourchance.testdrivendevelopment.exercise8.networking.ContactSchema
import com.techyourchance.testdrivendevelopment.exercise8.networking.GetContactsHttpEndpoint

class FetchContactsUseCase(private val getContactsHttpEndpoint: GetContactsHttpEndpoint) : GetContactsHttpEndpoint.Callback {
    interface Listener {
        fun onSuccess(items: List<Contact>)

        fun onFail(error: Error)
    }

    enum class Error {
        FAILURE,
        NETWORK_ERROR
    }

    private val listeners = mutableListOf<Listener>()

    fun fetchContactsAndNotify(filterTerm: String = "") {
        getContactsHttpEndpoint.getContacts(filterTerm, this)
    }

    fun registerListener(listener: Listener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: Listener) {
        listeners.remove(listener)
    }

    override fun onGetContactsSucceeded(cartItems: MutableList<ContactSchema>) {
        listeners.forEach { listener ->
            val items: List<Contact> = cartItems.map { it.toContact() }
            listener.onSuccess(items)
        }
    }

    private fun ContactSchema.toContact() = Contact(this.id, this.fullName, this.imageUrl)

    override fun onGetContactsFailed(failReason: GetContactsHttpEndpoint.FailReason) {
        val error = getError(failReason)

        listeners.forEach { listener ->
            listener.onFail(error)
        }
    }

    private fun getError(failReason: GetContactsHttpEndpoint.FailReason): Error {
        return when (failReason) {
            GetContactsHttpEndpoint.FailReason.GENERAL_ERROR -> Error.FAILURE
            GetContactsHttpEndpoint.FailReason.NETWORK_ERROR -> Error.NETWORK_ERROR
        }
    }
}