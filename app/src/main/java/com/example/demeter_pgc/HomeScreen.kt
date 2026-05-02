package com.example.demeter_pgc

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DemeterGreen = Color(0xFF0C7211)

@Preview
@Composable
fun HomeScreen(onNavigateToClassifier: () -> Unit = {}) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter            = painterResource(id = R.drawable.demeter),
                contentDescription = "Logo Demeter",
                modifier           = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text       = "Bienvenido a DEMETER",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = DemeterGreen
            )

            Text(
                text     = "Sistema inteligente de monitoreo agrícola",
                fontSize = 14.sp,
                color    = Color.Gray
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ── Botón Clasificador de Animales ──────────────────────────────
            Card(
                onClick   = onNavigateToClassifier,
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🐾", fontSize = 40.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text       = "Clasificador de Animales",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp,
                            color      = DemeterGreen
                        )
                        Text(
                            text     = "Identifica caballo, vaca, perro y más",
                            fontSize = 12.sp,
                            color    = Color.Gray
                        )
                    }
                }
            }

            // Aquí puedes añadir más módulos de la app en el futuro
        }
    }
}