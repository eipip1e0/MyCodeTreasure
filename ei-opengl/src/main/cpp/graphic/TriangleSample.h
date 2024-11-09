//
// Created by Time-Machine on 2024/11/9.
//

#include <GLES3/gl3.h>

class TriangleSample {

public:
    TriangleSample();
    ~TriangleSample();

    void Init();
    void Draw();

private:
    GLuint m_ProgramObj;
    GLuint m_VertexShader;
    GLuint m_FragmentShader;
};