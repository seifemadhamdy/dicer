package seifemadhamdy.dicer.view.renderer

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import seifemadhamdy.dicer.util.Dice
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class DiceRenderer(
    private val context: Context,
    private val textures: Dice.Textures = Dice.Textures()
) :
    GLSurfaceView.Renderer {
    var angleX: Float
    var angleY: Float

    private var indeces = shortArrayOf(
        0, 1, 2, 2, 3, 0,
        4, 5, 7, 5, 6, 7,
        8, 9, 11, 9, 10, 11,
        12, 13, 15, 13, 14, 15,
        16, 17, 19, 17, 18, 19,
        20, 21, 23, 21, 22, 23
    )

    private val modelMatrix = FloatArray(16)

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
     * it positions things relative to our eye.
     */
    private val viewMatrix = FloatArray(16)

    // Store the projection matrix. This is used to project the scene onto a 2D viewport.
    private val projectionMatrix = FloatArray(16)

    // Allocate storage for the final combined matrix. This will be passed into the shader program.
    private val mvpMatrix = FloatArray(16)
    private val diceColors: FloatBuffer

    // Store our model data in a float buffer.
    private val diceVertices: FloatBuffer
    private var indexBuffer: ShortBuffer

    // Store our model data in a float buffer.
    private val diceTextureCoordinates: FloatBuffer

    // This will be used to pass in the texture.
    private var textureUniformHandle = 0

    // This will be used to pass in model texture coordinate information.
    private var textureCoordinateHandle = 0

    // Size of the texture coordinate data in elements.
    private val textureCoordinateDataSize = 2

    // This is a handle to our texture data.
    private var textureDataHandle0 = 0
    private var textureDataHandle1 = 0
    private var textureDataHandle2 = 0
    private var textureDataHandle3 = 0
    private var textureDataHandle4 = 0
    private var textureDataHandle5 = 0

    // This will be used to pass in the transformation matrix.
    private var mvpMatrixHandle = 0

    // This will be used to pass in model position information.
    private var positionHandle = 0

    // This is a handle to our dice shading program.
    private var programHandle = 0

    // This will be used to pass in model color information.
    private var colorHandle = 0

    // How many bytes per float.
    private val mBytesPerFloat = 4

    /**
     * Initialize the model data.
     */
    init {
        Dice.face.apply {
            this@DiceRenderer.angleX = angleX
            this@DiceRenderer.angleY = angleY
        }

        // Define points for equilateral triangles.

        // This triangle is red, green, and blue.
        val triangle1VerticesData = floatArrayOf(
            // X, Y, Z,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f
        )
        // R, G, B, A
        val diceColorData = floatArrayOf(
            // Front face
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            // Right face
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            // Back face
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            // Left face
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            // Top face
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            // Bottom face
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
        )

        // This triangle is yellow, cyan, and magenta.

        // S, T (or X, Y)
        // Texture coordinate data.
        // Because images have a Y axis pointing downward (values increase as you move down the image) while
        // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
        // What's more is that the texture coordinates are the same for every face.
        val diceTextureCoordinateData = floatArrayOf(
            //front face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            // Right face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            // Back face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            // Left face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            // Top face
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            // Bottom face
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f
        )

        // Initialize the buffers.
        diceVertices =
            ByteBuffer.allocateDirect(triangle1VerticesData.size * mBytesPerFloat).order(
                ByteOrder.nativeOrder()
            ).asFloatBuffer().apply {
                put(triangle1VerticesData).position(0)
            }

        diceTextureCoordinates =
            ByteBuffer.allocateDirect(diceTextureCoordinateData.size * mBytesPerFloat).order(
                ByteOrder.nativeOrder()
            ).asFloatBuffer().apply {
                put(diceTextureCoordinateData).position(0)
            }

        diceColors = ByteBuffer.allocateDirect(diceColorData.size * mBytesPerFloat)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                put(diceColorData).position(0)
            }

        indexBuffer = ByteBuffer.allocateDirect(indeces.size * 2).order(ByteOrder.nativeOrder())
            .asShortBuffer().apply {
                put(indeces).position(0)
            }
    }

    override fun onSurfaceCreated(glUnused: GL10, config: EGLConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Position the eye behind the origin.
        val eyeX = 0.0f
        val eyeY = 0.0f
        val eyeZ = 1.5f

        // We are looking toward the distance
        val lookX = 0.0f
        val lookY = 0.0f
        val lookZ = -5.0f

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        val upX = 0.0f
        val upY = 1.0f
        val upZ = 0.0f

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ)
        //please note i am making view matrix as identity matrix intentionally here to avoid the
        //effects of view matrix. if you want explore the effect of view matrix you can uncomment this line
        Matrix.setIdentityM(viewMatrix, 0)
        val vertexShader = """uniform mat4 u_MVPMatrix;      
                              attribute vec4 a_Position;     
                              attribute vec4 a_Color;        
                              attribute vec2 a_TexCoordinate;
                              varying vec2 v_TexCoordinate;  
                              varying vec4 v_Color;          
                              void main()                    
                              {                              
                                    v_Color = a_Color;          
                                    v_TexCoordinate = a_TexCoordinate;
                                    gl_Position = u_MVPMatrix * a_Position;
                              }"""

        // normalized screen coordinates.
        val fragmentShader = """precision mediump float;       
                                varying vec4 v_Color;          
                                uniform sampler2D u_Texture;   
                                varying vec2 v_TexCoordinate;   
                                void main()                    
                                {                              
                                    gl_FragColor = (v_Color * texture2D(u_Texture, v_TexCoordinate));     
                                }"""

        // Load in the vertex shader.
        var vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)

        if (vertexShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader)

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle)

            // Get the compilation status.
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(vertexShaderHandle)
                vertexShaderHandle = 0
            }
        }

        if (vertexShaderHandle == 0) {
            throw RuntimeException("Error creating vertex shader.")
        }

        // Load in the fragment shader shader.
        var fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)

        if (fragmentShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader)

            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle)

            // Get the compilation status.
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(fragmentShaderHandle)
                fragmentShaderHandle = 0
            }
        }

        if (fragmentShaderHandle == 0) {
            throw RuntimeException("Error creating fragment shader.")
        }

        // Create a program object and store the handle to it.
        programHandle = GLES20.glCreateProgram()

        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle)

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle)

            // Bind attributes
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position")
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color")
            GLES20.glBindAttribLocation(programHandle, 2, "a_TexCoordinate")

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle)

            textures.apply {
                textureDataHandle0 =
                    loadTexture(faceOneTextureResId)

                textureDataHandle1 =
                    loadTexture(faceFiveTextureResId)

                textureDataHandle2 =
                    loadTexture(faceSixTextureResId)

                textureDataHandle3 =
                    loadTexture(faceTwoTextureResId)

                textureDataHandle4 =
                    loadTexture(faceThreeTextureResId)

                textureDataHandle5 =
                    loadTexture(faceFourTextureResId)
            }

            // Get the link status.
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programHandle)
                programHandle = 0
            }
        }

        if (programHandle == 0) {
            throw RuntimeException("Error creating program.")
        }
    }

    override fun onSurfaceChanged(glUnused: GL10, width: Int, height: Int) {
        if (width > height) {
            GLES20.glViewport(
                (width - height) / 2,
                0,
                height,
                height
            )
        } else {
            GLES20.glViewport(
                0,
                (height - width) / 2,
                width,
                width
            )
        }

        // Create a new perspective projection matrix.
        val left = -1.0f
        val right = 1.0f
        val bottom = -1.0f
        val top = 1.0f
        val near = 1.0f
        val far = 10.0f
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far)
        Matrix.setIdentityM(projectionMatrix, 0)
    }

    override fun onDrawFrame(glUnused: GL10) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(programHandle)

        // Set program handles for dice drawing.
        mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix")
        textureUniformHandle = GLES20.glGetUniformLocation(programHandle, "u_Texture")
        positionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position")
        colorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color")
        textureCoordinateHandle = GLES20.glGetAttribLocation(programHandle, "a_TexCoordinate")

        // Draw the triangle facing straight on.
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, angleX, 0.0f, 1.0f, 0.0f)
        Matrix.rotateM(modelMatrix, 0, -angleY, 1.0f, 0.0f, 0.0f)

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle0)

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 0)
        draw(diceVertices, 0)

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle1)

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 1)
        draw(diceVertices, 1)

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle2)

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 2)
        draw(diceVertices, 2)

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle3)

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 3)
        draw(diceVertices, 3)

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE4)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle4)

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 4)
        draw(diceVertices, 4)

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE5)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle5)

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureUniformHandle, 5)
        draw(diceVertices, 5)
    }

    private fun draw(aDiceBuffer: FloatBuffer, i: Int) {

        // Pass in the position information. each vertex needs 3 values and each face of the
        // dice needs 4 vertices. so total 3*4 = 12
        aDiceBuffer.position(12 * i)
        GLES20.glVertexAttribPointer(
            positionHandle, 3, GLES20.GL_FLOAT, false,
            0, aDiceBuffer
        )

        GLES20.glEnableVertexAttribArray(positionHandle)

        // Pass in the color information. every vertex color is defined by 4 values and each dice
        // face has 4 vertices so 4*4 = 16
        diceColors.position(16 * i)
        GLES20.glVertexAttribPointer(
            colorHandle, 4, GLES20.GL_FLOAT, false,
            0, diceColors
        )

        GLES20.glEnableVertexAttribArray(colorHandle)

        // Pass in the texture coordinate information. every vertex needs 2 values to define texture
        // for each face of the dice we need 4 textures . so 4*2=8
        diceTextureCoordinates.position(8 * i)
        GLES20.glVertexAttribPointer(
            textureCoordinateHandle, textureCoordinateDataSize, GLES20.GL_FLOAT, false,
            0, diceTextureCoordinates
        )

        GLES20.glEnableVertexAttribArray(textureCoordinateHandle)

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)

        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        //*each face of the dice is drawn using 2 triangles. so 2*3=6 lines
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
    }

    private fun loadTexture(resId: Int): Int {
        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)

        if (textureHandle[0] != 0) {
            val options = BitmapFactory.Options()
            options.inScaled = false // No pre-scaling

            // Read in the resource
            val bitmap =
                AppCompatResources.getDrawable(context, resId)?.toBitmap()

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

            // Set filtering
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST
            )

            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST
            )

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap?.recycle()
        }

        if (textureHandle[0] == 0) {
            throw RuntimeException("Error loading texture.")
        }

        return textureHandle[0]
    }
}