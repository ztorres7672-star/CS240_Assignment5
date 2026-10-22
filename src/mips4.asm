#FizzBuzz
li $t1 100
li $t2 0
li $v0, 1
li $t3 3
li $t5 5
li $t7 15
# main loop
j printItem
printItemEnd:


bne $t2, $t1, printItem
j Exit

# methods

printItem:

j printInt
printIntEnd:
j newLine
newLineEnd:

j printItemEnd

newLine:
li $v0, 11
li $a0, '\n'
syscall
j newLineEnd

printInt:
li $v0, 1
addi $t2, $t2, 1
move $a0, $t2
rem $t9, $t2, $t3
beq $t9, $zero, Fizz
rem $t9, $t2, $t5
beq $t9, $zero, Buzz
syscall
FizzEnd: 
BuzzEnd:
j printIntEnd

Fizz:
.data 
fizz: .asciiz "Fizz"
.text 
la $a0, fizz
li $v0, 4
syscall
rem $t9, $t2, $t5
beq $t9, $zero, Buzz
j FizzEnd

Buzz:
.data 
buzz: .asciiz "Buzz"
.text 
la $a0, buzz
li $v0, 4
syscall
j BuzzEnd


# end
Exit: