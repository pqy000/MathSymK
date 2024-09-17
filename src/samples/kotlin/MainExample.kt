package cn.mathsymk

import cn.mathsymk.structure.Group


//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
fun main() {
//    println(Group::class.java.simpleName)
    println((2..4).scan(1, Int::times))
}