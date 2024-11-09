//
// Created by Time-Machine on 2024/11/9.
//

#include "util/LogUtils.h"

class MyGLRenderContext {
private:
    static MyGLRenderContext *m_pContext;

    MyGLRenderContext();

    ~MyGLRenderContext();

public:

    static MyGLRenderContext *GetInstance();

    static void DestroyInstance();

    void SetImageData(int format, int width, int height, uint8_t *pData);

    void OnSurfaceCreated();

    void OnSurfaceChanged(int width, int height);

    void OnDrawFrame();
};
