.text
    addiu $t1, $zero, 100
    addiu $t2, $zero, 0
    addiu $v0, $zero, 1
    addiu $t3, $zero, 3
    addiu $t5, $zero, 5
    addiu $t7, $zero, 15
    j label_00000024
label_0000001C:
    bne $t2, $t1, label_00000024
    beq $t2, $t1, label_00000028
label_00000024:
    j label_0000008C
label_00000028:
    j label_00000040
label_0000002C:
    j label_00000030
label_00000030:
    j label_0000001C
    addiu $v0, $zero, 11
    addiu $a0, $zero, 10
    syscall
label_00000040:
    j label_0000002C
    addiu $v0, $zero, 1
    addi $t2, $t2, 1
    addu $a0, $t2, $zero
    div $t2, $t3
    mfhi $t9
    div $t2, $t5
    mfhi $t9
label_00000060:
    syscall
    j label_00000028
    lui $a0, 4097
    ori $a0, $a0, 0
    addiu $v0, $zero, 4
    syscall
    div $t2, $t5
    mfhi $t9
    j label_00000060
    lui $a0, 4097
    ori $a0, $a0, 5
label_0000008C:
    addiu $v0, $zero, 4
    syscall
    j label_00000060
