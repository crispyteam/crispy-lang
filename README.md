# Crispy (Kotlin implementation)  
Copyright (c) 2018 crispyteam  
See the LICENSE file for additional information.  

**If you want to use Crispy for your own projects you should use the the C implementation of Crispy,  
 which can be found <a href="https://github.com/funkschy/crispy">here</a>!**

### Table of Contents  
* [Introduction](#introduction)  
* [Syntax](#syntax)  
* [Implementation](#implementation_details)

<a name="introduction"/>  
  
## Introduction  

Crispy is an easy to learn scripting language with a minimalistic and elegant syntax.  

<a name="syntax"/>  
  
## Syntax  

**The complete grammar specification can be found in the "grammar.txt" file**   
**The following documentation is written for the C implementation of Crispy which can be found <a href="https://github.com/funkschy/crispy">here</a>!**  

### 1. Keywords  
Crispy currently has only 12 keywords, even though more will follow, as the development process advances.  
### var  
The var keyword is used to declare reassignable variables.  

	var greeting = "Hello World"
	greeting = "How are you?"
  
### val
'val' works just like 'var' with the small difference, that variables which are declared using 'val' keyword (these variables are called values) cannot be reassigned.
  
	val greeting = "Hello World"
	// this will cause an error
	greeting = "How are you?"
  
### nil, true & false  
  
'nil' is Crispys version of a null value.
'true' and 'false' work just like they would in other languages.  
  
	println(1 == 1) // prints true

### if & else 
The 'if' and 'else' keywords works just like you would expect. The only difference is, that you dont have to put the condition in parentheses (even though you can, if you want (please don't ;) ).  
  
	if 1 < 2 {
		println("this will be printed")
	} else if 1 == 1 {
		println("this won't")
	} else {
		println("this won't either")
	}
  
However, in Crispy, every if-block is an expression (just like any other block except for while-blocks), so you can actually assign it to a variable.
  
	val result = if true {
		1 + 1
		"Hello"
	}

	println(result) // prints "Hello"
  
  if no block is executed, the result will be 'nil'.

### while  
The 'while' keyword works exactly the same way as in every other C based language. The only difference is, that you dont have to put the condition in parentheses (even though you can, if you want).  
  
	// prints 0 - 9
	var i = 0
	while i < 10 {
		println(i++)
	}
  
### fun  
The 'fun' keyword can be used to declare user defined functions. All functions in Crispy are anonymous functions, so called <a href="https://en.wikipedia.org/wiki/Anonymous_function">lambdas</a>. That means that most of the time you will simply assign the declared lambda to a variable or pass it as a parameter to another function.
  
	val greet = fun -> println("Hello World")
  
After the fun keyword you can declare as many parameters as you wish. The paramter list is followed by the '->' operator, after which you can declare a single expression.

	val add = fun a, b -> a + b
	println(add(1, 2)) // prints 3
  
Since blocks in crispy are expressions, the lambda body can have an arbitrary length.
  
	// of course you could just write ... -> a + b + c
	// you could also just remove the return, since the last expression in the block
	// will be the return value
	val add_3 = fun a, b, c -> {
		val temp = a + b
		val result = temp + c
		return result
	}

	var result = add_3(1, 2, 3)
	println(result) // prints 6
  
You can use any expression as a lambda body
  
	val fib = fun n -> if n < 2 {
		n
	} else {
		fib(n - 2) + fib(n - 1)
	}

	println(fib(20)) // prints 6765 
  
As you can see, recursion works aswell.

<a name="implementation_details" />
  
## Implementation details  
  
### 1. Compilation  
The first part of running any Crispy program is the compilation. During this phase, the source code is broken down into its individual pieces (Tokens). Since Crispy only needs a lookahead of one Token, the source code is actually scanned one Token at a time. The compiler uses recursive descent parsing to convert complex productions into simple bytecode instructions. These bytecode instructions then get interpreted by a simple RISC virtual processor. The compiler is also responsible for initialising any constants found in the code. This means that strings are actually initialised (and <a href="https://en.wikipedia.org/wiki/String_interning">interned</a>) during compilation.

  
### 2. Virtual machine  
