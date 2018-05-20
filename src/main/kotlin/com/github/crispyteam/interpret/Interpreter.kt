package com.github.crispyteam.interpret

import com.github.crispyteam.parse.Expr
import com.github.crispyteam.parse.Stmt

class Interpreter : Stmt.Visitor<Unit>, Expr.Visitor<Any?> {
    override fun visitExpression(expressionStmt: Stmt.Expression) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitIncDec(incdecStmt: Stmt.IncDec) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitReturn(returnStmt: Stmt.Return) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitBreak(breakStmt: Stmt.Break) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitContinue(continueStmt: Stmt.Continue) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitBlock(blockStmt: Stmt.Block) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitIf(ifStmt: Stmt.If) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitValDecl(valdeclStmt: Stmt.ValDecl) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitVarDecl(vardeclStmt: Stmt.VarDecl) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitWhile(whileStmt: Stmt.While) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitBinary(binaryExpr: Expr.Binary): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitUnary(unaryExpr: Expr.Unary): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitLambda(lambdaExpr: Expr.Lambda): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitCall(callExpr: Expr.Call): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitGet(getExpr: Expr.Get): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitLiteral(literalExpr: Expr.Literal): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitVariable(variableExpr: Expr.Variable): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitGrouping(groupingExpr: Expr.Grouping): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitDictionary(dictionaryExpr: Expr.Dictionary): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitCrispyList(crispylistExpr: Expr.CrispyList): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitAssignment(assignmentExpr: Expr.Assignment): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitIncrement(incrementExpr: Expr.Increment): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitDecrement(decrementExpr: Expr.Decrement): Any? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}