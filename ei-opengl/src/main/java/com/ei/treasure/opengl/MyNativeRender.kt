package com.ei.treasure.opengl

class MyNativeRender {
    companion object {
        init {
            System.loadLibrary("ei-opengl")
        }
    }

    external fun native_OnInit()
    external fun native_OnUnInit()
    external fun native_SetImageData(format: Int, width: Int, height: Int, bytes: ByteArray?)
    external fun native_OnSurfaceCreated()
    external fun native_OnSurfaceChanged(width: Int, height: Int)
    external fun native_OnDrawFrame()

}