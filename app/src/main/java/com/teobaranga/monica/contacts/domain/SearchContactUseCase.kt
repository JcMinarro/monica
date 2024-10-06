package com.teobaranga.monica.contacts.domain

import com.teobaranga.monica.contacts.data.ContactEntity
import com.teobaranga.monica.contacts.data.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

internal class SearchContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
) {
    operator fun invoke(query: String, excludeContacts: List<Int> = emptyList()): Flow<List<ContactEntity>> {
        val trimmedQuery = query.trim().takeIf { it.isNotBlank() }
            ?: return emptyFlow()
        return contactRepository.searchContact(trimmedQuery, excludeContacts)
    }
}