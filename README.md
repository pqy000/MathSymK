
[![](https://jitpack.io/v/ezrnest/mathsymk.svg)](https://jitpack.io/#ezrnest/mathsymk)

This project aims to provide a modern and easy-to-use computer algebra and symbolic calculation library in Kotlin.
This project tries to cover a wide range of topics in mathematics, including but not limited to:
* algebraic structures
* linear algebra
* symbolic calculation
* number theory and combinatorics
* calculus
* analytic geometry
* differential geometry

This library enables users to treat various mathematical objects in a unified way and provides a set of utilities to manipulate them. 
For example, one can consider polynomials over a field, matrices over a ring, etc.
See the Quick Start section.

The project is still under development. 
Currently, it includes
- Interfaces for algebraic structures, such as `Group`, `Ring`, `Field`, etc.
- Basic implementations of algebraic structures 
  - `Fraction`, `Complex`, etc.
  - `Polynomial`: Algebraic operations, `gcd`, etc. 
  - `Multinomial`: Algebraic operations
- Linear algebra
  - `Vector`
  - `Matrix`
  - `Tensor`
- Utilities for number theory and combinatorics

# Installation

Currently, we provide the library through JitPack.
To use this library, you need to add the following to your build file.

#### Gradle (Kotlin DSL):
* Add the following to your root `build.gradle.kts` file at the end of the repositories section:

```kotlin
maven { url = uri("https://jitpack.io") }
```
* Add the dependency to your module's `build.gradle.kts` file:

```kotlin
implementation("com.github.ezrnest:mathsymk:v0.0.1")
```
#### Maven:
* Add the following to your `pom.xml` file:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
* Add the dependency:

```xml
<dependency>
    <groupId>com.github.ezrnest</groupId>
    <artifactId>mathsymk</artifactId>
    <version>v0.0.1</version>
</dependency>
```

# Quick Start

We provide a few examples to demonstrate the usage of this library.

### Fraction
#### Arithmetic operations:
```kotlin
val a = Fraction(1, 2)
val b = Fraction(1, 3)
println(
    listOf(a + b, a - b, a * b, a / b) // 5/6, 1/6, 1/6, 3/2
)
println(listOf(a.pow(2), a.pow(-1))) // 1/4, 2
```

#### Fraction fields:
```kotlin
// First example: fractions over integers, just as plain fractions
val Z = NumberModels.integers()
with(RFraction.over(Z)) {
    val f1 = frac(3, 4)
    val f2 = frac(1, 2)
    println(f1 + f2) // 5/4
}

// Second example: fractions over polynomials
val polyF = Polynomial.over(Z) // Polynomials over Z
with(polyF) {
    with(RFraction.over(polyF)) { // Fraction of polynomials over Z
        val f1 = (1 + x) / (1 + 2.x) // an overloaded operator `/` to create a fraction
        val f2 = (2 + 3.x) / (1 + 2.x)
        println(f1 + f2) // (3 + 4x)/(1 + 2x)
        println(f1 / f2) // (1 + x)/(2 + 3x)

        val f3 = x / (1 + 2.x)
        println(f1 + f3) // 1

        val f4 = (1 + x) / (1 + 3.x)
        println(f1 + f4) // (5*x^2 + 7*x + 2)/(6*x^2 + 5*x + 1)
    }
}
```


### Complex numbers

#### Complex of Double:
```kotlin
val z1 = ComplexD(1.0, 2.0)
val z2 = ComplexD(3.0, 4.0)
println(listOf(z1 + z2, z1 - z2, z1 * z2, z1 / z2)) // (4.0, 6.0), (-2.0, -2.0), (-5.0, 10.0), (0.44, 0.08)
println(listOf(z2.mod,z2.arg)) // 5.0, 0.93
```

#### Complex over Various Models:
```kotlin
val Z = NumberModels.integers()
val GaussianInt = Complex.over(Z)
with(GaussianInt){
    val z1 = 1 + 2.i
    val z2 = 3 + 4.i
    println(listOf(z1 + z2, z1 - z2, z1 * z2)) // (4, 6), (-2, -2), (-5, 10)
}

val Q = NumberModels.fractions()
val complexR = Complex.over(Q) // Complex numbers with rational components
with(Q){
    with(complexR) {
        val z1 = frac(1, 2) + frac(1, 3).i
        val z2 = frac(1, 4) + frac(1, 5).i
        println(listOf(z1 + z2, z1 - z2, z1 * z2)) 
        // (3/4, 8/15), (1/4, 2/15), (7/120, 11/60)
    }
}
```

### Polynomial
```kotlin
val Z97 = NumberModels.intModP(97) // Integers mod 97, Z/97Z, a field
val polyZ = Polynomial.over(Z97) // Polynomials over Z/97Z
with(polyZ) {
    val f = x+1 // predefined variable x polyZ
    val g = x-1
    val p1 = f * g // x^2 - 1
    val p2 = f pow 2 // (x+1)^2 = x^2 + 2x + 1
    println("p1 = $p1")
    println("p2 = $p2")
    val h = gcd(p1,p2).toMonic()
    println("gcd(p1,p2) = $h") // x + 1
}
```

### Multinomial




### Matrix and vector
```kotlin
val ℤ = NumberModels.integers()
val n = 3
val A = Matrix(n, ℤ) { i, j -> (5 * cos(i + 2.0 * j)).toInt() } // generate a non-singular matrix
println("Matrix A:")
println(A)
val B = A * A.T // Matrix multiplication and transpose
println("det(A) = ${A.det()}; det(A*A.T) = ${B.det()}") // determinant
val u = Vector(n, ℤ) { i -> i + 1 }
println(A * u) // matrix-vector multiplication
println(u.T * B * u) // quadratic form
```

### Tensor

#### Basic usage
```kotlin
val ℤ = NumberModels.integers()
val a = Tensor.of(intArrayOf(2, 3, 2, 5), ℤ, 0 until 60) // create a 4D tensor
// a is a 4D tensor with shape (2, 3, 2, 5)
a[intArrayOf(1, 2, 1, 3)] // get a single element
a[0, 1, 1, 2] // get a single element, operator overloaded version, need `import io.github.ezrnest.linear.get`
a[-1, 1..2, null, 0..<5 step 2] // slicing, need `import io.github.ezrnest.linear.get`
a.slice(-1, 1..2, null, 0..<5 step 2) // slicing, another way
a[1, Tensor.DOTS, 1, Tensor.NEW_AXIS, 2] // support omitting some axes and adding new axes
```

#### Multiplication
```kotlin
val ℤ = NumberModels.integers()
val a = Tensor.of(intArrayOf(3, 4, 5), ℤ, 0 until 60)
val b = Tensor.of(intArrayOf(4, 3, 2), ℤ, 0 until 24)
// The following three ways give the same result:
val res0 = Tensor.zeros(ℤ, 5, 2)
for ((i, j, k, n) in IterUtils.prodIdxN(intArrayOf(5, 2, 3, 4))) {
  res0[i, j] += a[k, n, i] * b[n, k, j] // direct computation
}
val res1 = a.permute(2, 1, 0).matmul(b, 2) // matmul at the last 2 axes
val res2 = Tensor.einsum("kni,nkj->ij", a, b) // einsum
```

#### More samples can be found in the `samples` directory.

# Features



## Number Models
This library provides a set of interfaces for algebraic structures, such as `Group`, `Ring`, `Field`, etc.
We refer to concrete implementations of these interfaces as **models**.
We list a few examples of algebraic structures and their models:
- `Monoid`: `String` (with concatenation), `List`, `Set`, etc.
- `Group`: `Permutation`, `Matrix`(invertible), etc.
- `Ring`: `Int`, `Polynomial`, etc.
- `Field`: `Double`, `Fraction`, `Complex`, etc.

## Flexibility of choosing models
Many mathematical objects can be treated in different ways depending on the context, 
or the underlying **model**. 
For example, we can consider polynomials over a ring, a field, or an integral domain.
This library provides the flexibility to choose the model for the objects, 
allowing users to work with different models in a unified way.
One can easily build up a matrix of integers or a matrix of polynomials over a field.


# Development

Currently, this project will be migrated and redesigned from my other math lib [Ancono](https://github.com/140378476/Ancono).

I am very glad to receive any feedback or suggestions.
Please feel free to open an issue or pull request.
Any contribution is welcome.

## Roadmap
- [ ] Linear algebra related
- [ ] Preliminary implementation of symbolic calculation
- [ ] Implement more algebraic structures
- [ ] Symbolic differentiation and integration
