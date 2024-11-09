package com.ei.treasure.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLSurfaceView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {
    companion object {
        private const val TAG = "MyGLSurfaceView"
        private const val IMAGE_FORMAT_RGBA = 0x01
        private const val IMAGE_FORMAT_NV21 = 0x02
        private const val IMAGE_FORMAT_NV12 = 0x03
        private const val IMAGE_FORMAT_I420 = 0x04
    }

    private val mNativeRender = MyNativeRender()
    private val mGLRender = MyGLRender(mNativeRender)

    init {
        setEGLContextClientVersion(3)
        setRenderer(mGLRender)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    class MyGLRender internal constructor(private val mNativeRender: MyNativeRender) : Renderer {

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            Log.d(TAG, "onSurfaceCreated() called with: gl = [$gl], config = [$config]")
            mNativeRender.native_OnSurfaceCreated()
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            Log.d(
                TAG,
                "onSurfaceChanged() called with: gl = [$gl], width = [$width], height = [$height]"
            )
            mNativeRender.native_OnSurfaceChanged(width, height)
        }

        override fun onDrawFrame(gl: GL10) {
            Log.d(TAG, "onDrawFrame() called with: gl = [$gl]")
            mNativeRender.native_OnDrawFrame()
        }
    }
}