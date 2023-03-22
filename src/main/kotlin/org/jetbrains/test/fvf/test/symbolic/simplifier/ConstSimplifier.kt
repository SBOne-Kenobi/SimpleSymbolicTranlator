package org.jetbrains.test.fvf.test.symbolic.simplifier

import org.jetbrains.test.fvf.test.symbolic.model.*

/**
 * Transform expressions by executing concrete instructions.
 */
class ConstSimplifier : Simplifier {
    private fun SymExpr.isConcrete(): Boolean = this is SymConst || this is SymTrue || this is SymFalse

    private fun checkEquals(left: SymExpr, right: SymExpr): Boolean? =
        if (left.isConcrete() && right.isConcrete()) {
            left == right
        } else {
            null
        }

    override fun simplify(expr: SymExpr): SymExpr = when (expr) {
        is SymEq -> {
            val left = simplify(expr.left)
            val right = simplify(expr.right)
            when (checkEquals(left, right)) {
                true -> SymTrue
                false -> SymFalse
                null -> SymEq(left, right)
            }
        }

        is SymNEq -> {
            val left = simplify(expr.left)
            val right = simplify(expr.right)
            when (checkEquals(left, right)) {
                true -> SymFalse
                false -> SymTrue
                null -> SymNEq(left, right)
            }
        }

        is SymNot -> when (val body = simplify(expr.expr) as SymBoolExpr) {
            SymTrue -> SymFalse
            SymFalse -> SymTrue
            else -> SymNot(body)
        }

        is SymConst -> expr
        is SymIf -> when (val cond = simplify(expr.cond) as SymBoolExpr) {
            SymTrue -> expr.thenJump
            SymFalse -> expr.elseJump
            else -> expr.copy(cond = cond)
        }

        is SymLet -> SymLet(expr.variable, simplify(expr.value))
        is SymMinus -> {
            val left = simplify(expr.left)
            val right = simplify(expr.right)
            if (left is SymConst && right is SymConst) {
                SymConst(left.value - right.value)
            } else {
                SymMinus(left, right)
            }
        }

        is SymMul -> {
            val left = simplify(expr.left)
            val right = simplify(expr.right)
            if (left is SymConst && right is SymConst) {
                SymConst(left.value * right.value)
            } else {
                SymMul(left, right)
            }
        }

        is SymPlus -> {
            val left = simplify(expr.left)
            val right = simplify(expr.right)
            if (left is SymConst && right is SymConst) {
                SymConst(left.value + right.value)
            } else {
                SymPlus(left, right)
            }
        }

        is SymRet -> SymRet(simplify(expr.expr))
        is SymVar -> expr
        is SymJump -> expr
        SymFalse -> SymFalse
        SymTrue -> SymTrue
    }
}