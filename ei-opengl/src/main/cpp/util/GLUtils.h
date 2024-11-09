//
// Created by Time-Machine on 2024/11/9.
//

#include "LogUtils.h"
#include <stdlib.h>
#include <GLES3/gl3.h>

class GLUtils {
public:
    static GLuint LoadShader(GLenum shaderType, const char *pSource);
    static GLuint CreateProgram(const char *pVertexShaderSource, const char *pFragShaderSource,
                                GLuint &vertexShaderHandle, GLuint &fragShaderHandle);
    void DeleteProgram(GLuint &program);
    static void CheckGLError(const char *pGLOperation);
};
