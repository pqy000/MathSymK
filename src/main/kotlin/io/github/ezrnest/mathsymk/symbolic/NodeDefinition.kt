package io.github.ezrnest.mathsymk.symbolic

import io.github.ezrnest.mathsymk.symbolic.alg.SymSets


interface NodeContextInfo {
    val nodeSignature: NodeSig
    fun enterContext(root: Node, rootCtx: EContext, cal: ExprCal): List<EContext>
}

class QualifierNodeContextInfo(
    override val nodeSignature: NodeSig,
    val qualifierIdx: Int = 0,
) : NodeContextInfo {
    override fun enterContext(root: Node, rootCtx: EContext, cal: ExprCal): List<EContext> {
        root as NodeChilded
        val restrictedVarNode = root.children[qualifierIdx]
        require(restrictedVarNode is Node2 && restrictedVarNode.name == SymSets.Names.BELONGS)
        val variable = restrictedVarNode.children[0] as NSymbol
        val ctxVariable = rootCtx.addQualified(variable, cal)
        val ctxSubExpr =  rootCtx.addRestrictedQualified(variable, restrictedVarNode, cal)
        val result = ArrayList<EContext>(root.childCount)
        repeat(root.childCount) {
            result.add(ctxSubExpr)
        }
        result[qualifierIdx] = ctxVariable
        return result
    }
}