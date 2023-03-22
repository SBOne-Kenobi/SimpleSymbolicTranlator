package org.jetbrains.test.fvf.test.symbolic.model

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

/**
 * Constraints storage.
 *
 * @param data is persistent data structure to avoid extra copying
 */
data class Constraints(
    val data: PersistentList<SymBoolExpr> = persistentListOf()
) {
    operator fun plus(expr: SymBoolExpr) = Constraints(data.add(expr))
}
