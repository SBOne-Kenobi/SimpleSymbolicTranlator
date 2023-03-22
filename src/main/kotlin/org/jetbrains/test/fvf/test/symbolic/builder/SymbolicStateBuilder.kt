package org.jetbrains.test.fvf.test.symbolic.builder

import org.jetbrains.test.fvf.test.symbolic.model.*

/**
 * Builder for effective work with persistent data structures.
 */
class SymbolicStateBuilder(
    storage: SymbolicStorage = SymbolicStorage(),
    constraints: Constraints = Constraints(),
) {
    constructor(state: SymbolicState) : this(state.storage, state.constraints)

    private val storageBuilder = storage.data.builder()
    private val constraintsBuilder = constraints.data.builder()

    val storage: SymbolicStorage
        get() = SymbolicStorage(storageBuilder.build())

    val constraints: Constraints
        get() = Constraints(constraintsBuilder.build())

    val variables: MutableSet<SymVar>
        get() = storageBuilder.keys

    fun updateStorage(expr: SymLet): SymbolicStateBuilder {
        storageBuilder[expr.variable] = expr.value
        return this
    }

    fun updateConstraints(expr: SymBoolExpr): SymbolicStateBuilder {
        constraintsBuilder.add(expr)
        return this
    }

    fun build(): SymbolicState = SymbolicState(storage, constraints)
}