package io.stepuplabs.pvba

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.lang.ref.WeakReference

@Composable
@Preview
fun Screen(payViaBankAppResolver: SpaydPayViaBankAppResolver) {
    MaterialTheme {
        val context = LocalContext.current
        Row(Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (payViaBankAppResolver.isPayViaBankAppSupported()) {
                    var spayd by remember {
                        mutableStateOf("SPD*1.0*ACC:CZ7603000000000076327632*AM:200.00*CC:CZK*X-VS:1234567890*MSG:CLOVEK V TISNI")
                    }

                    OutlinedTextField(
                        value = spayd,
                        onValueChange = { spayd = it },
                        label = { Text("SPAYD") },
                        modifier = Modifier.padding(16.dp)
                    )
                    OutlinedButton(onClick = {
                        val result = payViaBankAppResolver.payViaBankApp(
                            spayd,
                            NavigationParameters(WeakReference(context as Activity))
                        )
                        if (result.isSuccess) {
                            Toast.makeText(context, "Bank app opened successfully", Toast.LENGTH_LONG).show()
                        } else {
                            when {
                                result.exceptionOrNull() is BankAppOpeningException -> {
                                    Toast.makeText(context, "Error when opening bank app", Toast.LENGTH_LONG).show()
                                }
                                result.exceptionOrNull() is QrCodeFileException -> {
                                    Toast.makeText(context, "Error when creating QR code file", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }) {
                        val bankAppIcon = payViaBankAppResolver.getSupportedBankAppIcon()
                        if (bankAppIcon != null) {
                            Icon(
                                painter = rememberDrawablePainter(bankAppIcon),
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        }
                        Text("Pay via bank app")
                    }
                } else {
                    Text("No bank app supporting SPAYD found")
                }
            }
        }
    }
}
