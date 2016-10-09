# CPU

A simple virtual CPU with its own assembly language.


## CPU specification
Version 0.5 - 16.07.15

!!! WORK IN PROGRESS - MAY CHANGE !!!

### SUMMARY 
* 32 bit words
* 0x10000 words of memory
* 8 registers (A, B, C, X, Y, Z, I, J)
* Program counter (PC)
* Stack pointer (SP)
* Extra (EX)

Anything within this document with an asterisk (*x) refers to the value at the memory location of x.

Whenever the CPU processes an instruction, it reads PC, and then increases PC by one. The default value of the stack pointer is 0xffff. Pushing words onto the stack will decrease SP by one, and popping will increase SP by one.

Each instruction is assembled to an array of words. The instructions are based on this order:

1. Length of instruction
2. Instruction identifier

Extra elements in certain instructions:

x. Register or register value
y. Whether the passed value in element x was in fact a register (true = 1, false = 0)


### REGISTERS 


 |  VALUE    | DESCRIPTION
—+———————————+—————————————————————————————————————————
 | 0x00-0x07 | register (A, B, C, X, Y, Z, I, J)
 |      0x08 | PC
 |      0x09 | SP
 |      0x0a | PUSH if in a (SP--), or POP if in b (SP++)
 |      0x0b | PEEK
 |      0x0c | EX
—+———————————+——————————————————————————————————————————


### INSTRUCTIONS

a = Register.
b = Register's value or integer value.
c = Offset in memory
d = Size in memory

n = arbitrary amount of arguments (1-n)

Arguments marked with parentheses are optional.


 | VALUE | NAME                | DESCRIPTION
—+———————+—————————————————————+——————————————————————————
 | 0x00  | SET a b             | Sets a to b.
 | 0x01  | GET a b             | Gets the value of b and places it in a.
 |       |                     | This instruction is used for retrieving information about the
 |       |                     | the CPU during runtime. A detailed description can be found
 |       |                     | further down the specification.
 |       |                     |
 | 0x10  | ADD a b             | Sets a to a + b.
 | 0x11  | SUB a b             | Sets a to a - b.
 | 0x12  | MUL a b             | Sets a to a * b.
 | 0x13  | DIV a b             | Sets a to a / b. a will be rounded down.
 | 0x14  | MOD a b             | Sets a to a % b.
 |       |                     |
 | 0x20  | AND a b             | Sets a to a & b.
 | 0x21  | OR a b              | Sets a to a | b.
 | 0x22  | XOR a b             | Sets a to a ^ b.
 | 0x23  | NOT a               | Sets a to ~a.
 | 0x24  | SHR a b             | Sets a to a >> b.
 | 0x25  | SHL a b             | Sets a to a << b.
 | 0x26  | USHR a b            | Sets a to a >>> b.
 |       |                     |
 | 0x30  | IFE a b             | Executes the next instruction if a == b.
 | 0x31  | IFN a b             | Executes the next instruction if a != b.
 | 0x32  | IFG a b             | Executes the next instruction if a > b.
 | 0x33  | IFL a b             | Executes the next instruction if a < b.
 |       |                     |
 | 0x40  | JSR a               | Pushes PC onto the stack and sets PC to a.
 |       |                     |
 | 0x50  | HDP a n             | Dispatches n to hardware location a.
 | 0x51  | HRT a b n           | Retrieves n from location a to b.
 |       |                     |
 |       |                     |
 |       |                     |
—+———————+—————————————————————+————————————————————————————


### RETRIEVABLE VALUES 

 | VALUE | DESCRIPTION
—+———————+——————————————————————————————————————————
 | 0x00  | Amount of hardware memory (in words) installed in the CPU.
 | 0x01  | Milliseconds since the boot of the CPU. Will reset after 0xffffffff milliseconds.
—+———————+——————————————————————————————————————————
