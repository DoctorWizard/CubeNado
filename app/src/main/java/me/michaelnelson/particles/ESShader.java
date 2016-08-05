package me.michaelnelson.particles;

//
// Book:      OpenGL(R) ES 2.0 Programming Guide
// Authors:   Aaftab Munshi, Dan Ginsburg, Dave Shreiner
// ISBN-10:   0321502795
// ISBN-13:   9780321502797
// Publisher: Addison-Wesley Professional
// URLs:      http://safari.informit.com/9780321563835
//            http://www.opengles-book.com
//

// ESShader
//
//    Utility functions for loading shaders and creating program objects.
//


import android.opengl.GLES30;
import android.util.Log;

public class ESShader {
    //
    ///
    /// \brief Load a shader, check for compile errors, print error messages to
    /// output log
    /// \param type Type of shader (GL_VERTEX_SHADER or GL_FRAGMENT_SHADER)
    /// \param shaderSrc Shader source string
    /// \return A new shader object on success, 0 on failure
    //
    public static int loadShader(int type, String shaderSrc) {
        int shader;
        int[] compiled = new int[1];

        // Create the shader object
        shader = GLES30.glCreateShader(type);

        if (shader == 0)
            return 0;

        // Load the shader source
        GLES30.glShaderSource(shader, shaderSrc);

        // Compile the shader
        GLES30.glCompileShader(shader);

        // Check the compile status
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
            Log.e("ESShader", GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    //
    ///
    /// \brief Load a vertex and fragment shader, create a program object, link
    ///	 program.
    /// Errors output to log.
    /// \param vertShaderSrc Vertex shader source code
    /// \param fragShaderSrc Fragment shader source code
    /// \return A new program object linked with the vertex/fragment shader
    ///	 pair, 0 on failure
    //
    public static int loadProgram(String vertShaderSrc, String fragShaderSrc) {
        int vertexShader;
        int fragmentShader;
        int programObject;
        int[] linked = new int[1];

        // Load the vertex/fragment shaders
        vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertShaderSrc);
        if (vertexShader == 0)
            return 0;

        fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragShaderSrc);
        if (fragmentShader == 0) {
            GLES30.glDeleteShader(vertexShader);
            return 0;
        }

        // Create the program object
        programObject = GLES30.glCreateProgram();

        if (programObject == 0)
            return 0;

        GLES30.glAttachShader(programObject, vertexShader);
        GLES30.glAttachShader(programObject, fragmentShader);

        // Link the program
        GLES30.glLinkProgram(programObject);

        // Check the link status
        GLES30.glGetProgramiv(programObject, GLES30.GL_LINK_STATUS, linked, 0);

        if (linked[0] == 0) {
            Log.e("ESShader", "Error linking program:");
            Log.e("ESShader", GLES30.glGetProgramInfoLog(programObject));
            GLES30.glDeleteProgram(programObject);
            return 0;
        }

        // Free up no longer needed shader resources
        GLES30.glDeleteShader(vertexShader);
        GLES30.glDeleteShader(fragmentShader);

        return programObject;
    }

}
