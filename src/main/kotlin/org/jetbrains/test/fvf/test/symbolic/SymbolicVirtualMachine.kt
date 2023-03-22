package org.jetbrains.test.fvf.test.symbolic

import org.jetbrains.test.fvf.test.symbolic.builder.SymbolicStateBuilder
import org.jetbrains.test.fvf.test.symbolic.model.*
import org.jetbrains.test.fvf.test.symbolic.simplifier.Simplifier

/**
 * Class for working with symbolic instructions.
 *
 * Key idea: State x Instruction -> States[]
 */
class SymbolicVirtualMachine(
    private val simplifier: Simplifier? = null
) {

    /**
     * Storing jump instructions is needed for understanding of which branch is associated with produced state
     */
    data class StateWithJump(val state: SymbolicStateBuilder, val jump: SymJump? = null) {
        constructor(state: SymbolicState, jump: SymJump? = null) : this(SymbolicStateBuilder(state), jump)
    }

    fun nextStates(state: SymbolicState, expr: SymExpr): List<StateWithJump> {
        return when (val optExpr = expr
            .substitute(state.storage)
            .simplify()
        ) {
            is SymIf -> updateWithIf(state, optExpr)
            is SymLet -> updateWithLet(state, optExpr)
            is SymJump -> listOf(StateWithJump(state, optExpr))
            else -> listOf(StateWithJump(state))
        }
    }

    private fun updateWithIf(state: SymbolicState, expr: SymIf): List<StateWithJump> =
        buildList {
            val thenState = SymbolicStateBuilder(state)
            thenState.updateConstraints(expr.cond)
            add(StateWithJump(thenState, expr.thenJump))

            val elseState = SymbolicStateBuilder(state)
            elseState.updateConstraints(SymNot(expr.cond))
            add(StateWithJump(elseState, expr.elseJump))
        }

    private fun updateWithLet(state: SymbolicState, expr: SymLet): List<StateWithJump> = buildList {
        val newState = SymbolicStateBuilder(state)
            .updateStorage(expr)
        add(StateWithJump(newState))
    }

    private fun SymExpr.substitute(storage: SymbolicStorage): SymExpr = when (this) {
        is SymEq -> SymEq(left.substitute(storage), right.substitute(storage))
        is SymNEq -> SymNEq(left.substitute(storage), right.substitute(storage))
        is SymNot -> SymNot(expr.substitute(storage) as SymBoolExpr)
        is SymConst -> this
        is SymIf -> this.copy(cond = cond.substitute(storage) as SymBoolExpr)
        is SymLet -> SymLet(variable, value.substitute(storage))
        is SymMinus -> SymMinus(left.substitute(storage), right.substitute(storage))
        is SymMul -> SymMul(left.substitute(storage), right.substitute(storage))
        is SymPlus -> SymPlus(left.substitute(storage), right.substitute(storage))
        is SymRet -> SymRet(expr.substitute(storage))
        is SymVar -> storage[this] ?: error("Unknown variable $this")
        is SymJump -> this
        SymFalse -> SymFalse
        SymTrue -> SymTrue
    }

    private fun SymExpr.simplify(): SymExpr = simplifier?.simplify(this) ?: this
}