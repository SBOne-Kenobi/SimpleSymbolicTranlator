package org.jetbrains.test.fvf.test.symbolic.simplifier

import org.jetbrains.test.fvf.test.symbolic.model.SymExpr

interface Simplifier {
    fun simplify(expr: SymExpr): SymExpr
}