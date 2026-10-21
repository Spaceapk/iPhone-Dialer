package com.pablormlopez.iphonedialer.ui
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.pablormlopez.iphonedialer.data.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
@Composable fun GlassPanel(glass: Boolean=true, modifier: Modifier=Modifier, content: @Composable ColumnScope.()->Unit) {
    val color=MaterialTheme.colorScheme.surface
    Surface(modifier=modifier,shape=RoundedCornerShape(28.dp),color=color.copy(alpha=if(glass) .86f else 1f),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant.copy(alpha=.5f))) { Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content) }
}
@Composable fun Avatar(contact: Contact?, name: String, size: Int=48) {
    val context=LocalContext.current
    val image by produceState<android.graphics.Bitmap?>(null,contact?.photo) { value=withContext(Dispatchers.IO) { runCatching { contact?.photo?.let { context.contentResolver.openInputStream(Uri.parse(it))?.use { stream -> BitmapFactory.decodeStream(stream) } } }.getOrNull() } }
    Box(Modifier.size(size.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),contentAlignment=Alignment.Center) {
        if(image!=null) Image(image!!.asImageBitmap(),contentDescription=null,contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize())
        else if(name.none { it.isLetter() }) Icon(Icons.Default.Person,contentDescription=null,modifier=Modifier.size((size*.52f).dp),tint=MaterialTheme.colorScheme.onPrimaryContainer)
        else Text(name.trim().split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercaseChar() }.joinToString("").ifBlank { "?" },fontSize=(size*.32).sp,fontWeight=FontWeight.Medium,color=MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
@Composable fun EmptyPanel(title: String, message: String, action: String?=null, onAction: ()->Unit={}) { GlassPanel(modifier=Modifier.fillMaxWidth()) { Text(title,style=MaterialTheme.typography.titleLarge);Text(message,color=MaterialTheme.colorScheme.onSurfaceVariant);if(action!=null) Button(onClick=onAction) { Text(action) } } }
@Composable fun ConfirmDialog(title: String, message: String, action: String="Confirmar", onDismiss: ()->Unit, onConfirm: ()->Unit) {
    AlertDialog(onDismissRequest=onDismiss,title={Text(title)},text={Text(message)},confirmButton={TextButton(onClick={onDismiss();onConfirm()}) { Text(action) }},dismissButton={TextButton(onClick=onDismiss) { Text("Cancelar") }})
}
@Composable fun Control(icon: ImageVector, label: String, enabled: Boolean=true, selected: Boolean=false, onClick: ()->Unit) {
    Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.widthIn(min=88.dp)) {
        FilledTonalIconButton(onClick=onClick,enabled=enabled,modifier=Modifier.size(64.dp),colors=IconButtonDefaults.filledTonalIconButtonColors(containerColor=if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,contentColor=if(selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)) { Icon(icon,contentDescription=label,modifier=Modifier.size(28.dp)) }
        Spacer(Modifier.height(8.dp));Text(label,style=MaterialTheme.typography.bodyMedium)
    }
}
