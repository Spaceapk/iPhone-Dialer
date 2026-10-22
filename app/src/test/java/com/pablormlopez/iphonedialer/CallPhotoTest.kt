package com.pablormlopez.iphonedialer

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import com.pablormlopez.iphonedialer.ui.CallPhotoProcessor
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[29],application=Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CallPhotoTest {
    @Test fun decodingIsBoundedForLargePhotos() {
        for((w,h) in listOf(8000 to 6000,1920 to 1080,1921 to 1921,500 to 900)) {
            val sample=CallPhotoProcessor.sampleSize(w,h)
            assertTrue(w/sample<=960);assertTrue(h/sample<=960)
        }
    }
    @Test fun uniformPhotoKeepsItsColorAndOriginalIsNotModified() {
        val source=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888);source.eraseColor(Color.rgb(90,140,180))
        val softened=CallPhotoProcessor.soften(source)
        assertEquals(source.getPixel(16,16),softened.getPixel(16,16));assertFalse(source.isRecycled)
        assertNotSame(source,softened)
    }
    @Test fun edgesAreSoftenedWithoutChangingDimensions() {
        val source=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888)
        for(y in 0..31) for(x in 0..31) source.setPixel(x,y,if(x<16) Color.BLACK else Color.WHITE)
        val softened=CallPhotoProcessor.soften(source)
        assertEquals(32,softened.width);assertEquals(32,softened.height)
        assertTrue(Color.red(softened.getPixel(15,16)) in 1..254)
        assertEquals(Color.BLACK,source.getPixel(15,16))
    }
    @Test fun unsupportedUriFallsBackWithoutNetworkAccess() {
        val resolver=org.robolectric.RuntimeEnvironment.getApplication().contentResolver
        assertNull(CallPhotoProcessor.load(resolver,"https://invalid.example/photo.jpg"))
        assertNull(CallPhotoProcessor.load(resolver,"content://missing.provider/1"))
    }
}
