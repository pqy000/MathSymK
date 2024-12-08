package io.github.ezrnest.mathsymk.symbolic

import java.util.Collections



interface NodeProperties {
    val symbol: ESymbol

    fun enterContext(root: Node, rootCtx: EContext, cal: ExprCal): List<EContext>

    fun qualifiedVariables(root: Node, rootCtx: EContext, cal: ExprCal): List<ESymbol>
}

class QualifierNodeProperties(
    override val symbol: ESymbol,
) : NodeProperties {

    override fun qualifiedVariables(root: Node, rootCtx: EContext, cal: ExprCal): List<ESymbol> {
        require(root is NodeChilded)
        val variablesNode = root.children[0]
        require(variablesNode is NodeN)
        return variablesNode.children.map { (it as NSymbol).symbol }
    }


    override fun enterContext(root: Node, rootCtx: EContext, cal: ExprCal): List<EContext> {
        require(root is NodeChilded)
        val ctxIntro = EContextImpl()
        val variablesNode = root.children[0]
        require(variablesNode is NodeN)
        for(varNode in variablesNode.children){
            require(varNode is NSymbol)
            ctxIntro.qualifiedSymbols.add(varNode.symbol)
        }
        val conditionNode = root.children[1]
        ctxIntro.addCondition(conditionNode)

        val newCtx = rootCtx.with(ctxIntro)

        // return n copies of the context
        return Collections.nCopies(root.childCount, newCtx)
    }
}