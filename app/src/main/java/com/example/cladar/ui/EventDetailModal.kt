package com.example.cladar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cladar.model.EventItem
import com.example.cladar.ui.components.MathMarkdownViewer

@Composable
fun EventDetailModal(
    event: EventItem,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onSaveNotes: (String) -> Unit
) {
    var notesText by remember { mutableStateOf(event.notesMarkdown) }
    var isEditing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onSaveNotes(notesText)
                onDismiss()
            }) { Text("Close & Save") }
        },
        title = {
            Column {
                Text(event.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${event.date} at ${event.time} - ${event.location}", fontSize = 12.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Chronicle Notes (Markdown & Math)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { isEditing = !isEditing }) {
                        Text(if (isEditing) "Preview Math" else "Edit Markdown", fontSize = 11.sp)
                    }
                }

                if (isEditing) {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("Write notes in Markdown... Use $$ x^2 $$ for math formulas.") }
                    )
                } else {
                    MathMarkdownViewer(
                        content = notesText.ifBlank { "*No notes recorded for this event yet.*" },
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    )
}
