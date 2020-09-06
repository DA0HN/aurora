section .data
    fmtin: db "%d", 0x0
    fmtout: db "%d", 0xA, 0x0

    str_0: db "Variavel menor que 1", 0x10, 0x0
    str_1: db "Variavel maior que 3", 0x10, 0x0

section .bss
    variavel_0: resd 1
    constante_0: resd 1

section .text
    global main
    extern printf
    extern scanf

main:
    push ebp
    mov ebp, esp

    mov eax, 3
    mov [constante_0], eax
    mov eax, 3
    mov ebx, [constante_0]
    mul ebx
    mov ecx, eax
    mov eax, 2
    mov ebx, ecx
    add eax, ebx
    mov [variavel_0], eax
    mov eax, [variavel_0]
    cmp eax, 1
    jge _L1

    push dword str_0
    call printf
    add esp, 4

    jmp _L2
_L1:
    mov eax, [variavel_0]
    cmp eax, 3
    jle _L3

    push dword str_1
    call printf
    add esp, 4

_L3:
_L2:
    mov esp, ebp
    pop ebp
    ret
