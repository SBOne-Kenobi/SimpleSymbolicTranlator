package org.jetbrains.test.fvf.test

import org.jetbrains.test.fvf.test.ast.*
import org.jetbrains.test.fvf.test.symbolic.SymbolicTranslator
import org.jetbrains.test.fvf.test.symbolic.SymbolicVirtualMachine
import org.jetbrains.test.fvf.test.symbolic.simplifier.ConstSimplifier
import org.jetbrains.test.fvf.test.util.ExecTreePrinter

fun main() {
    val fooBarAst = Block(
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

    val svm = SymbolicVirtualMachine(ConstSimplifier())
    val translator = SymbolicTranslator(svm)
    val tree = translator.buildExecutionTree(fooBarAst, "a", "b")

    val printer = ExecTreePrinter()
    printer.print(System.out, tree)
}
