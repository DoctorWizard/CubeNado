package me.michaelnelson.particles;

/**
 * Created by Michael on 7/25/2016.
 */
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


import android.content.Context;
import android.opengl.Matrix;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;


public class ParticleSystemRenderer implements GLSurfaceView.Renderer
{

    ///
    // Constructor
    //
    public ParticleSystemRenderer(Context context)
    {
        mContext = context;
    }




    ///
    // Initialize the shader and program object
    //
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {
        String vShaderStr =
                "uniform float u_time;                                \n" +
                        "uniform float u_numParticles;                                \n" +
                        "uniform float u_curParticles;                                \n" +
                        "uniform float u_random;                                \n" +
                        "uniform vec3 u_centerPosition;                       \n" +
                        "uniform mat4 u_MVPMatrix;                            \n" +
                        "attribute float a_lifetime;                          \n" +
                        "attribute vec3 a_startPosition;                      \n" +
                        "attribute vec3 a_endPosition;                        \n" +
                        "attribute vec4 a_vertPosition;                        \n" +
                        " \n" +
                        "varying float v_lifetime;                            \n" +
                        "varying float v_draw;                            \n" +
                        "float rand(vec2 n) { \n" +
                        "    return fract(sin(dot(n, vec2(12.9898, 4.1414))) * 43758.5453);\n" +
                        "} \n" +

                        "void main()                                          \n" +
                        "{                                                    \n" +
                        "  if ( u_time >= (a_startPosition.z)  )                        \n" +
                        "  {                                                  \n" +
                        "   v_draw =1.0;                                                  \n" +
                        "   mat3 rotationMat = mat3(\n" +
                        "        vec3( cos(u_time),  sin(u_time),  0.0),\n" +
                        "        vec3(-sin(u_time),  cos(u_time),  0.0),\n" +
                        "        vec3(        0.0,         0.0,  1.0)\n" +
                        "    );" +
                        "    float rando = rand(a_startPosition.xy) +1.0; \n" +
                        "    float yPos = a_startPosition.y/u_curParticles *100.0 +50.0 + rando* u_random *sin((u_time - a_startPosition.z)*u_random *rando);\n"+
                        "    vec4 rotation = vec4(float(rando *(u_time - a_startPosition.z) *100.0*cos((u_time - a_startPosition.z)*10.0))," +
                        "                               (u_time - a_startPosition.z)* 20.0 *sin((u_time - a_startPosition.z)*10.0)," +
                        "                               float(rando * (u_time - a_startPosition.z) *100.0*sin((u_time - a_startPosition.z)*10.0))," +
                        "                               1);             \n" +
                        "    gl_Position = u_MVPMatrix *  (a_vertPosition +vec4(0.0,-100.0,0,1)  + rotation + ((u_time - a_startPosition.z) * vec4(a_endPosition.x,yPos,a_endPosition.z,1)));                                              \n" +
                        "  }                                                  \n" +
                        "  else                                               \n" +
                        "  {                                                    \n" +
                        "     gl_Position = u_MVPMatrix *vec4( 0, -1000, 0, 1 );       \n" +
                        "     v_draw =0.0 ;                                                 \n" +
                        "  }                                                       \n" +
                        "}";


        String fShaderStr =
                "precision mediump float;                             \n" +
                        "uniform vec4 u_color;                                \n" +
                        "varying float v_lifetime;                            \n" +
                        "varying float v_draw;                            \n" +
                        "uniform sampler2D s_texture;                         \n" +
                        "void main()                                          \n" +
                        "{                                                    \n" +
                        " if(v_draw ==1.0)                            \n" +
                        //"  vec4 texColor;                                     \n" +
                        //"  texColor = texture2D( s_texture, gl_PointCoord );  \n" +
                        "   gl_FragColor = vec4( u_color ) ;         \n" +
                        //"  gl_FragColor.a *= v_lifetime;                      \n" +
                        "}                                                    \n";

        // Load the shaders and get a linked program object
        mProgramObject = ESShader.loadProgram(vShaderStr, fShaderStr);


        int [] genVAO = new int[1];
        GLES30.glGenVertexArrays(1,genVAO,0);
        GLES30.glBindVertexArray(genVAO[0]);


        // Get the attribute locations
        mLifetimeLoc = GLES30.glGetAttribLocation(mProgramObject, "a_lifetime");
        mStartPositionLoc = GLES30.glGetAttribLocation(mProgramObject, "a_startPosition" );
        mEndPositionLoc = GLES30.glGetAttribLocation(mProgramObject, "a_endPosition" );
        mVertPositionLoc = GLES30.glGetAttribLocation(mProgramObject, "a_vertPosition" );


        // Get the uniform locations
        mTimeLoc = GLES30.glGetUniformLocation ( mProgramObject, "u_time" );
        mColorLoc = GLES30.glGetUniformLocation ( mProgramObject, "u_color" );
        mCurrentParticlesLoc = GLES30.glGetUniformLocation ( mProgramObject, "u_curParticles" );
        mTotalParticlesLoc = GLES30.glGetUniformLocation ( mProgramObject, "u_numParticles" );
        mRandomFactorLoc = GLES30.glGetUniformLocation ( mProgramObject, "u_random" );




        GLES30.glClearColor ( 0.0f, 0.0f, 0.0f, 0.0f );

        // Use culling to remove back faces.
        //GLES20.glEnable(GLES30.GL_CULL_FACE);

        // Enable depth testing
        //GLES20.glEnable(GLES30.GL_DEPTH_TEST);

        // Position the eye in front of the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -200f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = 0.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        // Fill in particle data array
        Random generator = new Random();
        int positive;
        for ( int i = 0; i < NUM_PARTICLES; i++ )
        {

            // Lifetime of particle
            mParticleData[i * 7 + 0] = generator.nextFloat() *15;

            // End position of particle
            mParticleData[i * 7 + 1] = 0f;//generator.nextFloat() * i/NUM_PARTICLES *50f - 50.0f;
            mParticleData[i * 7 + 2] =   i/NUM_PARTICLES *100 + 50.0f;
            mParticleData[i * 7 + 3] = generator.nextFloat() * i/NUM_PARTICLES *30f - 30.0f;

            // Start position of particle
            mParticleData[i * 7 + 4] = i;//generator.nextFloat() * 0.25f - 0.125f;
            mParticleData[i * 7 + 5] = i;//generator.nextFloat() * 0.25f - 0.125f;
            mParticleData[i * 7 + 6] = (float)(i)/(float)(NUM_PARTICLES)*mMaxTime;//generator.nextFloat() * 0.25f - 0.125f;

        }



        mParticles = ByteBuffer.allocateDirect(mParticleData.length * 4)
                .order(ByteOrder.BIG_ENDIAN).asFloatBuffer(); //native order did not work with my One Plus One
        mParticles.put(mParticleData).position(0);



        // Initialize time to cause reset on first update
        mTime = 10.0f;



        int temp[] = {0,0};

        GLES30.glGenBuffers(2,temp,0);



        mMeshBuffer = temp[0];
        mInstanceDataBuffer = temp[1];

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,mMeshBuffer);



        ByteBuffer bb = ByteBuffer.allocateDirect(mVertexCoords.length *4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(mVertexCoords);
        vertexBuffer.position(0);



        // pushed to gpu
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, mVertexCoords.length *4,vertexBuffer,GLES30.GL_STATIC_DRAW);


        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,mInstanceDataBuffer);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,mParticleData.length *4,mParticles,GLES30.GL_STATIC_DRAW);


        // clear the cpu mem
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);



    }

    private void update()
    {

        if (mLastTime == 0)
            mLastTime = SystemClock.uptimeMillis();
        long curTime = SystemClock.uptimeMillis();
        long elapsedTime = curTime - mLastTime;
        float deltaTime = elapsedTime / 1000.0f;
        mLastTime = curTime;

        mTime += deltaTime;


        if ( mTime >= mMaxTime )
        {
            mTime = 0.0f;
        }

        // Load uniform time variable
        GLES30.glUniform1f ( mTimeLoc, mTime );

    }

    ///
    // Draw a triangle using the shader pair created in onSurfaceCreated()
    //
    public void onDrawFrame(GL10 glUnused)
    {
        // Use the program object
        GLES30.glUseProgram ( mProgramObject );

        update();

        float[] color = new float[4];





        // Random color
        color[0] =  1f;
        color[1] = 0.5f;
        color[2] =  0.5f;
        color[3] = 0.5f;

        GLES30.glUniform4f ( mColorLoc, color[0], color[1], color[2], color[3] );

        GLES30.glUniform1f(mCurrentParticlesLoc,CUR_PARTICLES);
        GLES30.glUniform1f(mTotalParticlesLoc,NUM_PARTICLES);
        GLES30.glUniform1f(mRandomFactorLoc,RANDOM_FACTOR);



        // Set the viewport
        GLES30.glViewport ( 0, 0, mWidth, mHeight );

        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramObject, "u_MVPMatrix");
        //mMVMatrixHandle = GLES30.glGetUniformLocation(mProgramObject, "u_MVMatrix");

        // Translate the cube into the screen.
        Matrix.setIdentityM(mModelMatrix, 0);
        //Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -3.5f);

        // This multiplies the view matrix by the model matrix, and stores
        // the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix , 0, mModelMatrix, 0);


        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix , 0,  mMVPMatrix, 0);

        // Pass in the modelview matrix.
        //GLES30.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix,
        // and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        //Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        //System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);

        // Pass in the combined matrix.
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);


        // Clear the color buffer
        GLES30.glClear ( GLES30.GL_COLOR_BUFFER_BIT );



        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,mMeshBuffer);
        GLES30.glVertexAttribPointer(mVertPositionLoc,3,GLES30.GL_FLOAT,false,0,0);
        GLES30.glEnableVertexAttribArray ( mVertPositionLoc );


        // Load the vertex attributes


        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,mInstanceDataBuffer);

        if(mLifetimeLoc >=0) {
            GLES30.glVertexAttribPointer(mLifetimeLoc, 1, GLES30.GL_FLOAT,
                    false, PARTICLE_SIZE * 4,
                    0);
            GLES30.glVertexAttribDivisor(mLifetimeLoc, 1);

            GLES30.glEnableVertexAttribArray ( mLifetimeLoc );
        }


        if(mEndPositionLoc >=0) {
            GLES30.glVertexAttribPointer(mEndPositionLoc, 3, GLES30.GL_FLOAT,
                    false, PARTICLE_SIZE * 4,
                    1);

            GLES30.glVertexAttribDivisor(mEndPositionLoc, 1);
            GLES30.glEnableVertexAttribArray ( mEndPositionLoc );
        }


        if(mStartPositionLoc >=0) {
            GLES30.glVertexAttribPointer(mStartPositionLoc, 3, GLES30.GL_FLOAT,
                    false, PARTICLE_SIZE * 4,
                    4);
            GLES30.glVertexAttribDivisor(mStartPositionLoc, 1);
            GLES30.glEnableVertexAttribArray ( mStartPositionLoc );
        }

        GLES30.glDrawArraysInstanced( GLES30.GL_TRIANGLE_STRIP, 0, 4, CUR_PARTICLES  );
    }

    ///
    // Handle surface changes
    //
    public void onSurfaceChanged(GL10 glUnused, int width, int height)
    {
        mWidth = width;
        mHeight = height;

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 0.01f;
        final float far = 1000.0f;

        Matrix.perspectiveM(mProjectionMatrix,0,45,ratio,0.1f,1000);
    }



    // Handle to a program object
    private int mProgramObject;

    // Attribute locations
    private int mLifetimeLoc;
    private int mStartPositionLoc;
    private int mEndPositionLoc;
    private int mVertPositionLoc;

    // Uniform location
    private int mTimeLoc;
    private int mColorLoc;
    private int mCurrentParticlesLoc;
    private int mTotalParticlesLoc;
    private int mRandomFactorLoc;


    private int VAO;

    // Texture handle
    private int mTextureId;

    // Update time
    private float mTime;
    private long mLastTime;
    private float mMaxTime =12;

    // Additional member variables
    private int mWidth;
    private int mHeight;
    private FloatBuffer mParticles;
    private Context mContext;

    private final int NUM_PARTICLES = 100000;
    public int CUR_PARTICLES = 10;
    private final int PARTICLE_SIZE = 7;


    public int RANDOM_FACTOR =1;


    public void setCUR_PARTICLES(int CUR_PARTICLES) {
        this.CUR_PARTICLES = CUR_PARTICLES;
    }

    public void setRANDOM_FACTOR(int RANDOM_FACTOR) {
        this.RANDOM_FACTOR = RANDOM_FACTOR;
    }


    private int mMeshBuffer;
    private int mInstanceDataBuffer;

    /** This will be used to pass in the transformation matrix. */
    private int mMVPMatrixHandle;


    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    /** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
    private float[] mProjectionMatrix = new float[16];



    /** Allocate storage for the final combined matrix. This will be passed into the shader program. */
    private float[] mMVPMatrix = new float[16];

    private float mVertexCoords[] = {
            -5f, -5f, 5f,   // front bottom left
            -5f,  5f, 5f,   // front top left

            5f, -5f, 5f,   // front bottom right
            5f,  5f, 5f,  // front top right

            5f, -5f, -5f,   // back bottom right
            5f, 5f, -5f,   // back top right

            -5f, -5f, -5f,// back bottom left
            -5f, -5f, -5f// back top left


    };
    private final float[] mParticleData = new float[NUM_PARTICLES * PARTICLE_SIZE];


}