package com.pablormlopez.iphonedialer.ui

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** Bounded, local-only photo decode. No contact photo is copied to persistent storage. */
object CallPhotoProcessor {
    const val MAX_EDGE = 960
    fun sampleSize(width: Int, height: Int): Int {
        var sample=1
        while ((width.toLong()+sample-1)/sample>MAX_EDGE || (height.toLong()+sample-1)/sample>MAX_EDGE) sample*=2
        return sample
    }
    fun load(resolver: ContentResolver, photo: String): Bitmap? = runCatching {
        val uri=Uri.parse(photo)
        if(uri.scheme!="content") return@runCatching null
        val bounds=BitmapFactory.Options().apply { inJustDecodeBounds=true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it,null,bounds) }
        if(bounds.outWidth<=0 || bounds.outHeight<=0) return@runCatching null
        val decoded=resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it,null,BitmapFactory.Options().apply {
            inSampleSize=sampleSize(bounds.outWidth,bounds.outHeight);inPreferredConfig=Bitmap.Config.ARGB_8888
        }) } ?: return@runCatching null
        try { soften(decoded) } finally { decoded.recycle() }
    }.getOrNull()

    /** Two separable sliding-window passes: O(pixels), also works on Android 10/11. */
    fun soften(source: Bitmap): Bitmap {
        require(source.width<=MAX_EDGE && source.height<=MAX_EDGE)
        val w=source.width;val h=source.height
        var pixels=IntArray(w*h);source.getPixels(pixels,0,w,0,0,w,h)
        val scratch=IntArray(w*h);val radius=(minOf(w,h)/100).coerceIn(1,8)
        repeat(2) {
            pass(pixels,scratch,w,h,radius,true)
            pass(scratch,pixels,w,h,radius,false)
        }
        return Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888)
    }
    private fun pass(src: IntArray,dst: IntArray,w: Int,h: Int,radius: Int,horizontal: Boolean) {
        val length=if(horizontal) w else h;val lines=if(horizontal) h else w;val count=radius*2+1
        for(line in 0 until lines) {
            fun index(position: Int)=if(horizontal) line*w+position.coerceIn(0,w-1) else position.coerceIn(0,h-1)*w+line
            var red=0;var green=0;var blue=0
            for(i in -radius..radius) { val c=src[index(i)];red+=c ushr 16 and 255;green+=c ushr 8 and 255;blue+=c and 255 }
            for(i in 0 until length) {
                dst[index(i)]=(255 shl 24) or ((red/count) shl 16) or ((green/count) shl 8) or (blue/count)
                val old=src[index(i-radius)];val next=src[index(i+radius+1)]
                red+=(next ushr 16 and 255)-(old ushr 16 and 255)
                green+=(next ushr 8 and 255)-(old ushr 8 and 255)
                blue+=(next and 255)-(old and 255)
            }
        }
    }
}

@Composable fun rememberCallPhoto(photo: String?): Bitmap? {
    val context=LocalContext.current
    return key(photo) {
        val bitmap by produceState<Bitmap?>(null,photo) {
            if(photo!=null) {
                val loaded=withContext(Dispatchers.Default) { CallPhotoProcessor.load(context.contentResolver,photo) }
                ensureActive();value=loaded
            }
        }
        bitmap
    }
}

/** Only the photo is softened; foreground composables stay sharp and touchable. */
@Composable fun CallPhotoBackdrop(photo: Bitmap?,modifier: Modifier=Modifier,content: @Composable BoxScope.()->Unit) {
    Box(modifier.fillMaxSize().background(Color.Black)) {
        if(photo!=null) {
            Image(photo.asImageBitmap(),contentDescription=null,contentScale=ContentScale.Crop,modifier=Modifier.matchParentSize().testTag("call-photo-background"))
            // White text on a fully white photo remains >= 5.7:1 at the lightest point.
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha=.60f),Color.Black.copy(alpha=.64f),Color.Black.copy(alpha=.86f)))))
        }
        content()
    }
}
