package com.example.demeter_pgc

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost

@Composable
fun NavigationApp() {
    val myNavController = rememberNavController()
    val myStartDestination = "login"

    NavHost(
        navController = myNavController,
        startDestination = myStartDestination,
    ) {
        composable("login") {
            LoginScreen(onClickRegister = {
                myNavController.navigate("register")
            }, onSuccessfulLogin ={
                myNavController.navigate("home"){
                    popUpTo("login"){
                        inclusive = true
                    }
                }
            })
        }
        composable("register") {
            RegisterScreen(
                onClickBack = {
                    myNavController.popBackStack()
                }, onSuccessfullRegister = {
                    myNavController.navigate("home") {
                        popUpTo(0)
                    }
                }
            )
        }
        composable("home") {
            HomeScreen()
        }
    }
}