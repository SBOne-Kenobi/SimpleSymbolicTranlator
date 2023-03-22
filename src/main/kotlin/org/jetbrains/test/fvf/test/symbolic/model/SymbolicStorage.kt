package org.jetbrains.test.fvf.test.symbolic.model

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * Symbolic storage.
 *
 * @param data is persistent data structure to avoid extra copying
 */
data class SymbolicStorage(
    val data: PersistentMap<SymVar, SymExpr> = persistentMapOf()
) {
    operator fun get(x: SymVar) = data[x]

    operator fun plus(expr: SymLet) = SymbolicStorage(data.put(expr.variable, expr.value))
}