package org.jetbrains.test.fvf.test.symbolic

import org.jetbrains.test.fvf.test.ast.*
import org.jetbrains.test.fvf.test.symbolic.simplifier.ConstSimplifier
import org.jetbrains.test.fvf.test.util.ExecTreePrinter
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SymbolicTranslatorTest {

    private fun translate(ast: Expr, vararg parameters: String): String {
        val svm = SymbolicVirtualMachine(ConstSimplifier())
        val translator = SymbolicTranslator(svm)
        val tree = translator.buildExecutionTree(ast, *parameters)

        val printer = ExecTreePrinter()
        val builder = StringBuilder()
        printer.print(builder, tree)
        return builder.toString().trim()
    }

    @Test
    fun simpleTest() {
        val ast = Block(
            Let(Var("x"), Const(1)),
            Ret(Var("x"))
        )
        val result = translate(ast)
        assertEquals("""
            ────────
            Pi: true
            S: {}
            Expr: x = 1
            ────────
            Pi: true
            S: {x ↦ 1}
            Expr: return x
        """.trimIndent(), result)
    }

    @Test
    fun exampleTest() {
        val ast = Block(
            Let(Var("x"), Const(1)),
            Let(Var("y"), Const(0)),
            If(
                NEq(Var("a"), Const(0)),
                Block(
                    Let(Var("y"), Plus(Const(3), Var("x"))),
                    If(
                        Eq(Var("b"), Const(0)),
                        Let(Var("x"), Mul(Const(2), Plus(Var("a"), Var("b")))),
                    )
                )
            ),
            Ret(Minus(Var("x"), Var("y")))
        )
        val result = translate(ast, "a", "b")
        assertEquals("""
            ────────
            Pi: true
            S: {a ↦ @p0, b ↦ @p1}
            Expr: x = 1
            ────────
            Pi: true
            S: {a ↦ @p0, b ↦ @p1, x ↦ 1}
            Expr: y = 0
            ────────
            Pi: true
            S: {a ↦ @p0, b ↦ @p1, x ↦ 1, y ↦ 0}
            Expr: if (a != 0)
            ├── ────────
            │   Pi: (@p0 != 0)
            │   S: {a ↦ @p0, b ↦ @p1, x ↦ 1, y ↦ 0}
            │   Expr: y = 3 + x
            │   ────────
            │   Pi: (@p0 != 0)
            │   S: {a ↦ @p0, b ↦ @p1, x ↦ 1, y ↦ 4}
            │   Expr: if (b == 0)
            │   ├── ────────
            │   │   Pi: (@p0 != 0) ∧ (@p1 == 0)
            │   │   S: {a ↦ @p0, b ↦ @p1, x ↦ 1, y ↦ 4}
            │   │   Expr: x = 2 * (a + b)
            │   │   ────────
            │   │   Pi: (@p0 != 0) ∧ (@p1 == 0)
            │   │   S: {a ↦ @p0, b ↦ @p1, x ↦ 2 * (@p0 + @p1), y ↦ 4}
            │   │   Expr: return x - y
            │   └── ────────
            │       Pi: (@p0 != 0) ∧ !(@p1 == 0)
            │       S: {a ↦ @p0, b ↦ @p1, x ↦ 1, y ↦ 4}
            │       Expr: return x - y
            └── ────────
                Pi: !(@p0 != 0)
                S: {a ↦ @p0, b ↦ @p1, x ↦ 1, y ↦ 0}
                Expr: return x - y
        """.trimIndent(), result)
    }

    @Test
    fun testLocalVarsInIf() {
        val ast = Block(
            If(
                NEq(Var("a"), Const(0)),
                Block(
                    Let(Var("x"), Var("a")),
                    Let(Var("a"), Plus(Var("a"), Var("x")))
                )
            ),
            Ret(Var("a"))
        )
        val result = translate(ast, "a")
        assertEquals("""
            ────────
            Pi: true
            S: {a ↦ @p0}
            Expr: if (a != 0)
            ├── ────────
            │   Pi: (@p0 != 0)
            │   S: {a ↦ @p0}
            │   Expr: x = a
            │   ────────
            │   Pi: (@p0 != 0)
            │   S: {a ↦ @p0, x ↦ @p0}
            │   Expr: a = a + x
            │   ────────
            │   Pi: (@p0 != 0)
            │   S: {a ↦ @p0 + @p0}
            │   Expr: return a
            └── ────────
                Pi: !(@p0 != 0)
                S: {a ↦ @p0}
                Expr: return a
        """.trimIndent(), result)
    }

    @Test
    fun testLocalVarsInBlock() {
        val ast = Block(
            Block(
                Let(Var("x"), Var("a")),
                Let(Var("a"), Plus(Var("a"), Var("x")))
            ),
            Ret(Var("a"))
        )
        val result = translate(ast, "a")
        assertEquals("""
            ────────
            Pi: true
            S: {a ↦ @p0}
            Expr: x = a
            ────────
            Pi: true
            S: {a ↦ @p0, x ↦ @p0}
            Expr: a = a + x
            ────────
            Pi: true
            S: {a ↦ @p0 + @p0}
            Expr: return a
        """.trimIndent(), result)
    }

}