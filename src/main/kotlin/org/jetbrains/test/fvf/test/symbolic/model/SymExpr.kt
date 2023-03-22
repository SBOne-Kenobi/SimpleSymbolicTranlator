package org.jetbrains.test.fvf.test.symbolic.model

sealed class SymExpr

/**
 * Jump is used by branch instructions and says which next symbolic instruction we should use.
 *
 * It will be useful for switch, for example.
 */
data class SymJump(val localId: Int) : SymExpr()

data class SymIf(
    val cond: SymBoolExpr,
    val thenJump: SymJump = SymJump(0),
    val elseJump: SymJump = SymJump(1)
) : SymExpr()

data class SymConst(val value: Int) : SymExpr()
data class SymVar(val name: String) : SymExpr()
data class SymLet(val variable: SymVar, val value: SymExpr) : SymExpr()
data class SymPlus(val left: SymExpr, val right: SymExpr) : SymExpr()
data class SymMinus(val left: SymExpr, val right: SymExpr) : SymExpr()
data class SymMul(val left: SymExpr, val right: SymExpr) : SymExpr()
data class SymRet(val expr: SymExpr) : SymExpr()

sealed class SymBoolExpr : SymExpr()
data class SymNot(val expr: SymBoolExpr) : SymBoolExpr()
data class SymEq(val left: SymExpr, val right: SymExpr) : SymBoolExpr()
data class SymNEq(val left: SymExpr, val right: SymExpr) : SymBoolExpr()
object SymTrue : SymBoolExpr()
object SymFalse : SymBoolExpr()
