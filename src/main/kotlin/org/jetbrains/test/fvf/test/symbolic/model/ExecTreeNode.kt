package org.jetbrains.test.fvf.test.symbolic.model

data class ExecTreeNode(
    val state: SymbolicState = SymbolicState(),
    val nextExpr: SymExpr? = null,
    val children: List<ExecTreeNode> = emptyList(),
)
