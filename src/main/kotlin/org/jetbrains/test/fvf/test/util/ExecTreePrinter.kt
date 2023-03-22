package org.jetbrains.test.fvf.test.util

import org.jetbrains.test.fvf.test.symbolic.model.*

class ExecTreePrinter {
    fun print(appendable: Appendable, execTreeNode: ExecTreeNode) {
        appendable.print(execTreeNode, "", "")
    }

    private fun Appendable.surroundedIf(
        need: Boolean,
        prefix: String,
        suffix: String,
        block: Appendable.() -> Unit
    ) {
        if (need) {
            append(prefix)
        }
        block()
        if (need) {
            append(suffix)
        }
    }

    private fun Appendable.printOp(inner: Boolean, left: SymExpr, right: SymExpr, op: String) {
        surroundedIf(inner, "(", ")") {
            printExpr(left, true)
            append(" $op ")
            printExpr(right, true)
        }
    }

    private fun Appendable.printExpr(expr: SymExpr?, inner: Boolean = false) {
        when (expr) {
            is SymEq -> printOp(inner, expr.left, expr.right, "==")
            SymFalse -> append("false")
            is SymNEq -> printOp(inner, expr.left, expr.right, "!=")
            is SymNot -> {
                append("!")
                printExpr(expr.expr, true)
            }

            SymTrue -> append("true")
            is SymConst -> append(expr.value.toString())
            is SymIf -> {
                append("if (")
                printExpr(expr.cond)
                append(")")
            }

            is SymLet -> {
                append(expr.variable.name)
                append(" = ")
                printExpr(expr.value)
            }

            is SymMinus -> printOp(inner, expr.left, expr.right, "-")
            is SymMul -> printOp(inner, expr.left, expr.right, "*")
            is SymPlus -> printOp(inner, expr.left, expr.right, "+")
            is SymRet -> {
                append("return ")
                printExpr(expr.expr)
            }

            is SymVar -> append(expr.name)
            is SymJump -> append("Jump child ${expr.localId}")
            null -> append("null")
        }
    }

    private fun Appendable.printConstraints(constraints: Constraints) {
        if (constraints.data.isEmpty()) {
            append("true")
        } else {
            constraints.data.forEachIndexed { index, symBoolExpr ->
                if (index != 0) {
                    append(" ∧ ")
                }
                printExpr(symBoolExpr, true)
            }
        }
    }

    private fun Appendable.printStorage(storage: SymbolicStorage) {
        surroundedIf(true, "{", "}") {
            storage.data.asIterable().forEachIndexed { index, (name, value) ->
                if (index != 0) {
                    append(", ")
                }
                append("${name.name} ↦ ")
                printExpr(value)
            }
        }
    }

    private fun Appendable.printInfo(execTreeNode: ExecTreeNode, prefix: String, childrenPrefix: String) {
        append("${prefix}────────\n")

        append("${childrenPrefix}Pi: ")
        printConstraints(execTreeNode.state.constraints)
        append("\n")

        append("${childrenPrefix}S: ")
        printStorage(execTreeNode.state.storage)
        append("\n")

        append("${childrenPrefix}Expr: ")
        printExpr(execTreeNode.nextExpr, inner = false)
        append("\n")
    }

    private fun Appendable.print(execTreeNode: ExecTreeNode, prefix: String, childrenPrefix: String) {
        printInfo(execTreeNode, prefix, childrenPrefix)
        val sequential = generateSequence(execTreeNode.children.singleOrNull()) { it.children.singleOrNull() }
        var last = execTreeNode
        sequential.forEach {
            last = it
            printInfo(it, childrenPrefix, childrenPrefix)
        }
        last.children.forEachIndexed { index, nextNode ->
            if (index == last.children.lastIndex) {
                print(nextNode, "$childrenPrefix└── ", "$childrenPrefix    ")
            } else {
                print(nextNode, "$childrenPrefix├── ", "$childrenPrefix│   ")
            }
        }
    }
}