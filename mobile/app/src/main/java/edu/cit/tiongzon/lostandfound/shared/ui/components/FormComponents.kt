package edu.cit.tiongzon.lostandfound.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*

@Composable
fun LabeledField(label: String, placeholder: String) {
    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth().height(52.dp),
        singleLine = true
    )
}

@Composable
fun LabeledTextArea(label: String, placeholder: String) {
    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
    OutlinedTextField(
        value = "",
        onValueChange = {},
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth().height(92.dp)
    )
}

@Composable
fun LabeledSelect(label: String, value: String) {
    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
    OutlinedButton(
        onClick = {},
        modifier = Modifier.fillMaxWidth().height(46.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate900)
    ) {
        Text(value, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
        Text("v")
    }
}

@Composable
fun SegmentedControl(options: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Status", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
        options.forEach {
            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(34.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate900)
            ) { Text(it, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun DashedPlaceholder(text: String, height: Int) {
    Box(
        Modifier.fillMaxWidth().height(height.dp).background(Slate50).padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Slate400, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}
