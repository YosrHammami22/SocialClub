package com.yosrhammami.socialclub.domain.model

/**
 * Builds the ID shared by everything that belongs to a pair of attendees: their contact request
 * (`contactRequests/{pairId}`) and their conversation (`conversations/{pairId}`), formatted as
 * `"<smaller>_<larger>"` (lexicographic String comparison).
 *
 * The result is the same regardless of argument order: `pairIdOf(a, b) == pairIdOf(b, a)`.
 * This is deliberate — a pair has at most one request and one conversation, whoever started it,
 * so either side can look them up, and security rules can read the contact from a conversation ID.
 *
 * @throws IllegalArgumentException if a uid is blank, contains `_`, or both uids are equal.
 */
fun pairIdOf(uidA: String, uidB: String): String {

    require(uidA.isNotBlank() && uidB.isNotBlank()) { "uid must not be blank" }
    // "_" is the separator: allowing it would let two different pairs map to the same ID.
    require('_' !in uidA && '_' !in uidB) { "uid must not contain '_'" }
    require(uidA != uidB) { "a pair needs two distinct uids" }

    return if (uidA < uidB) {
        "${uidA}_$uidB"
    } else {
        "${uidB}_$uidA"
    }
}