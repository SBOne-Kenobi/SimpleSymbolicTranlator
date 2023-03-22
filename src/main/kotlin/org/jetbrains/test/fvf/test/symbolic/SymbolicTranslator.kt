package org.jetbrains.test.fvf.test.symbolic

import org.jetbrains.test.fvf.test.ast.*
import org.jetbrains.test.fvf.test.symbolic.builder.ExecTreeNodeBuilder
import org.jetbrains.test.fvf.test.symbolic.builder.SymbolicStateBuilder
import org.jetbrains.test.fvf.test.symbolic.model.*

/**
 * Class for working with translation from AST to symbolic instructions and execution tree.
 */
class SymbolicTranslator(
    private val svm: SymbolicVirtualMachine
) {
    /**
     * Symbolic parameters will be named like @p{index}.
     */
    fun buildExecutionTree(ast: Expr, vararg parameters: String): ExecTreeNode = ExecTreeNodeBuilder().run {
        updateState {
            parameters.forEachIndexed { index, param ->
                updateStorage(SymLet(SymVar(param), SymVar("@p$index")))
            }
        }
        buildFrom(ast)
        build()
    }

    fun translate(expr: Expr): SymExpr = when (expr) {
        is Eq -> SymEq(translate(expr.left), translate(expr.right))
        is NEq -> SymNEq(translate(expr.left), translate(expr.right))
        is Const -> SymConst(expr.value)
        is If -> SymIf(translate(expr.cond) as SymBoolExpr)
        is Let -> SymLet(translate(expr.variable) as SymVar, translate(expr.value))
        is Minus -> SymMinus(translate(expr.left), translate(expr.right))
        is Mul -> SymMul(translate(expr.left), translate(expr.right))
        is Plus -> SymPlus(translate(expr.left), translate(expr.right))
        is Ret -> SymRet(translate(expr.expr))
        is Var -> SymVar(expr.name)
        else -> throw IllegalArgumentException("Unexpected expr $expr")
    }

    /**
     * Produce all possible branches from expression.
     *
     * @receiver root node
     * @return "last" nodes from all branches
     */
    private fun ExecTreeNodeBuilder.buildFrom(expr: Expr): List<ExecTreeNodeBuilder> =
        when (expr) {
            is Block -> buildFromBlock(expr)
            is If -> buildFromIf(expr)
            is Ret -> updateWithRet(expr)
            else -> listOf(updateWithExpr(expr))
        }

    private fun ExecTreeNodeBuilder.updateWithRet(expr: Ret): List<ExecTreeNodeBuilder> {
        val symExpr = translate(expr)
        nextExpr = symExpr
        return emptyList()
    }

    /**
     * @param expr must be non-branching expression
     */
    private fun ExecTreeNodeBuilder.updateWithExpr(expr: Expr): ExecTreeNodeBuilder {
        val symExpr = translate(expr)
        nextExpr = symExpr
        val (newState, jump) = svm.nextStates(state, symExpr).singleOrNull()
            ?: error("Unexpected branching")
        assert(jump == null) { "Unexpected jump" }
        return ExecTreeNodeBuilder(newState).also {
            addChild(it)
        }
    }

    private fun ExecTreeNodeBuilder.buildFromIf(expr: If): List<ExecTreeNodeBuilder> {
        val symExpr = translate(expr) as SymIf
        nextExpr = symExpr
        return svm.nextStates(state, symExpr).flatMap { (state, jump) ->
            assert(jump != null) { "Jump is expected" }
            val next = if (jump == symExpr.thenJump) {
                expr.thenExpr
            } else {
                expr.elseExpr
            }
            ExecTreeNodeBuilder(state).let {
                addChild(it)
                if (next != null) {
                    it.buildFrom(next)
                } else {
                    listOf(it)
                }
            }
        }.updateLocalVariables(this)
    }

    private fun ExecTreeNodeBuilder.buildFromBlock(block: Block): List<ExecTreeNodeBuilder> =
        block.exprs.fold(listOf(this)) { nodes, expr ->
            nodes.flatMap { node ->
                node.buildFrom(expr)
            }
        }.updateLocalVariables(this)

    private fun List<ExecTreeNodeBuilder>.updateLocalVariables(oldNode: ExecTreeNodeBuilder): List<ExecTreeNodeBuilder> =
        map { node ->
            node.updateState {
                val state = this
                oldNode.updateState {
                    state.updateLocalVariables(this)
                }
            }
        }

    private fun SymbolicStateBuilder.updateLocalVariables(state: SymbolicStateBuilder) {
        variables.retainAll(state.variables)
    }
}