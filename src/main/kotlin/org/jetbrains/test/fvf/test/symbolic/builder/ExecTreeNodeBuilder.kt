package org.jetbrains.test.fvf.test.symbolic.builder

import org.jetbrains.test.fvf.test.symbolic.model.ExecTreeNode
import org.jetbrains.test.fvf.test.symbolic.model.SymExpr
import org.jetbrains.test.fvf.test.symbolic.model.SymbolicState

class ExecTreeNodeBuilder(
    private var stateBuilder: SymbolicStateBuilder = SymbolicStateBuilder()
) {
    constructor(state: SymbolicState) : this(SymbolicStateBuilder(state))

    private val childrenBuilders: MutableList<ExecTreeNodeBuilder> = mutableListOf()

    var nextExpr: SymExpr? = null

    val state: SymbolicState
        get() = stateBuilder.build()

    fun addChild(node: ExecTreeNodeBuilder): ExecTreeNodeBuilder {
        childrenBuilders += node
        return this
    }

    fun setNextExpr(expr: SymExpr): ExecTreeNodeBuilder {
        nextExpr = expr
        return this
    }

    fun updateState(block: SymbolicStateBuilder.() -> Unit): ExecTreeNodeBuilder {
        stateBuilder.apply(block)
        return this
    }

    fun build(): ExecTreeNode =
        ExecTreeNode(
            children = childrenBuilders.map { it.build() },
            nextExpr = nextExpr,
            state = state
        )
}
