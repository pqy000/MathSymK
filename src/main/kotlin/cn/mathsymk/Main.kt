package cn.mathsymk


fun testVariousLetters() {
    // blackboard bold
    val (ℕ, ℤ, ℚ, ℝ, ℂ) = listOf(1, 2, 3, 4, 5)
    println(listOf(ℕ, ℤ, ℚ, ℝ, ℂ))
    // bold
    val (𝐴, 𝐵, 𝐶, 𝐷, 𝐸) = listOf(1, 2, 3, 4, 5)
    println(listOf(𝐴, 𝐵, 𝐶, 𝐷, 𝐸))
    // italic
    val (𝑎, 𝑏, 𝑐, 𝑑, 𝑒) = listOf(1, 2, 3, 4, 5)
    println(listOf(𝑎, 𝑏, 𝑐, 𝑑, 𝑒))
    // mathcal
    //"U+0042"
    println("U+0042")
    val (𝒜,ℬ,𝒞,𝒟,ℰ) = listOf(1, 2, 3, 4, 5)
    println(listOf(𝒜,ℬ,𝒞,𝒟,ℰ))
}

//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
fun main() {
    val 𝙰 = 1

    val length = "a".let(String::length)
    println(length)
}