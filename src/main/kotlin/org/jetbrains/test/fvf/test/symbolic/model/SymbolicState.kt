package org.jetbrains.test.fvf.test.symbolic.model

data class SymbolicState(
    val storage: SymbolicStorage = SymbolicStorage(),
    val constraints: Constraints = Constraints(),
)