package com.carlosalcina.drivelist.ui.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordDialog(
    emailInput: String,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onDismissRequest: () -> Unit,
    isLoading: Boolean,
    feedbackMessage: String?
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    text = "Restablecer Contraseña",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Introduce tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = onEmailChange,
                    label = { Text("Correo Electrónico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = feedbackMessage != null && !feedbackMessage.contains("Se ha enviado un correo") // Considera una mejor lógica para isError
                )

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }

                feedbackMessage?.let {
                    Text(
                        text = it,
                        color = if (it.contains("Se ha enviado un correo")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSendClick,
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                        }else{
                            Text("Enviar")
                        }
                    }
                }
            }
        }
    }
}