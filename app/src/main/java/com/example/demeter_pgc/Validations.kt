package com.example.demeter_pgc

import android.util.Patterns

// retornar un true si es valido y un flase si no es evalido
// tambien retorne una cadena que me diga que paso si no es valido

fun validateEmail(email: String): Pair<Boolean, String> {
    return when {
        email.isEmpty() -> Pair(false, "El correo es requerido.")
        !Patterns.EMAIL_ADDRESS.matcher(email).matches()-> Pair(false,"El correo es invalido")
        !email.endsWith("@gmail.com") -> Pair(false, "Ese email no es corporativo")
        else -> Pair (true,"")

    }
}
fun validatePassword(password: String): Pair<Boolean, String> {
    return when {
        password.isEmpty() -> Pair(false, "La contraseña es requerida.")
        !password.any{it.isDigit()} -> Pair(false, "La contraseña debe tener al menos 1 digito")
        password.length < 8 -> Pair(false, "La contraseña debe tener al menos 8 caracteres")
        else -> Pair(true, "")
    }
}
fun validateName(name: String): Pair<Boolean, String> {
    return when {
        name.isEmpty() -> Pair(false, "El nombre es requerido.")
        name.length < 3 -> Pair(false, "El nombre debe tener al menos 3 caracteres.")
        else -> Pair(true, "")
    }
}

fun validateConfirmPassword(
    password: String,
    confirmPassword: String
): Pair<Boolean, String> {
    return when {
        confirmPassword.isEmpty() -> Pair(false, "La contraseña es requerida.")
        confirmPassword != password -> Pair(false, "Las contraseñas no coinciden.")
        else -> Pair(true, "")
    }
}