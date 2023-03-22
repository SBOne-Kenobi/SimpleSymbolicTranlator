package org.jetbrains.test.fvf.test.ast

sealed class Expr

class Block(vararg val exprs: Expr) : Expr()

data class Const(val value: Int) : Expr()
data class Var(val name: String) : Expr()
data class Let(val variable: Var, val value: Expr) : Expr()
data class If(val cond: BoolExpr, val thenExpr: Expr, val elseExpr: Expr? = null) : Expr()
data class Plus(val left: Expr, val right: Expr) : Expr()
data class Minus(val left: Expr, val right: Expr) : Expr()
data class Mul(val left: Expr, val right: Expr) : Expr()
data class Ret(val expr: Expr) : Expr()

sealed class BoolExpr : Expr()
data class Eq(val left: Expr, val right: Expr) : BoolExpr()
data class NEq(val left: Expr, val right: Expr) : BoolExpr()
