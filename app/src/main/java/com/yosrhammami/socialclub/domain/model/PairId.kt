package com.yosrhammami.socialclub.domain.model

/**
 * Builds the Firestore document ID for the contact request between two attendees,
 * formatted as `"<smaller>_<larger>"` (lexicographic String comparison).
 *
 * The result is the same regardless of argument order: `generateId(a, b) == generateId(b, a)`,
 * even though the parameters are named sender and receiver. This is deliberate — a pair of
 * attendees has at most one request document, whoever sent it, so either side can look it up.
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