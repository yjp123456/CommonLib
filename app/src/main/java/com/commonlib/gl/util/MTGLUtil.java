package com.commonlib.gl.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.util.Log;

import com.commonlib.BaseApplication;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;
import static android.opengl.GLUtils.texImage2D;

/**
 * GL工具类
 */
public class MTGLUtil {
    /**
     * 统一日志输出tag
     */
    private static final String LOG_TAG = "[MT_OpenGl]";
    private static final String TAG = "MTGLUtil";

    public static final String cacheTexturePath = BaseApplication.get().getExternalCacheDir() + "/temp/";

    /**
     * 空纹理
     */
    public static final int NO_TEXTURE = 0;
    public static final int NO_FRAMEBUFFER = 0;
    public static final int NO_PROGRAM = 0;
    /**
     * 调试模式，只有调试模式下才会进行一些状态的检查
     */
    private static boolean DEBUG = true;

    public static float[] VERTEX = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };


    /**
     * 日志输出
     */
    public static void d(String tag, String content) {
        Log.d(LOG_TAG, tag + "--->" + content);
    }

    /**
     * 日志输出
     */
    private static void e(String tag, String content) {
        Log.e(LOG_TAG, tag + "--->" + content);
    }

    /**
     * 读取assets目录下文件
     */
    public static String readAssetsText(String filePath) {
        try {
            InputStream is = BaseApplication.get().getAssets().open(filePath);
            int size = is.available();
            byte[] buffer = new byte[size];
            //noinspection ResultOfMethodCallIgnored
            is.read(buffer);
            is.close();
            //noinspection UnnecessaryLocalVariable
            String text = new String(buffer, "utf-8");
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取resources文件
     */
    public static String readResourcesText(Context context, int resourceId) {
        StringBuilder body = new StringBuilder();
        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body.toString();
    }

    /**
     * 加载纹理
     *
     * @param image   Bitmap
     * @param recycle 是否自动回收Bitmap
     * @return 纹理
     */
    public static int loadTexture(@NonNull final Bitmap image, final boolean recycle) {
        final int[] textureObjectId = new int[1];
        glGenTextures(1, textureObjectId, 0);
        if (textureObjectId[0] <= 0) {
            MTGLUtil.e(TAG, "创建纹理失败");
            return 0;
        }
        glBindTexture(GL_TEXTURE_2D, textureObjectId[0]);

        //下面是设置图片放大缩小后如何选择像素点进行优化处理来让图片尽量保持清晰
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        texImage2D(GL_TEXTURE_2D, 0, image, 0);
        if (recycle) {
            image.recycle();
        }
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);
        return textureObjectId[0];
    }

    public static int loadOESTexture(@NonNull final Bitmap image, final boolean recycle) {
        final int[] textureObjectId = new int[1];
        glGenTextures(1, textureObjectId, 0);
        if (textureObjectId[0] <= 0) {
            MTGLUtil.e(TAG, "创建纹理失败");
            return 0;
        }
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureObjectId[0]);

        //下面是设置图片放大缩小后如何选择像素点进行优化处理来让图片尽量保持清晰
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        texImage2D(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0, image, 0);
        if (recycle) {
            image.recycle();
        }
        glGenerateMipmap(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return textureObjectId[0];
    }

    public static int createOESTextureObject() {
        int[] tex = new int[1];
        //生成一个纹理
        GLES20.glGenTextures(1, tex, 0);
        //将此纹理绑定到外部纹理上
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        //设置纹理过滤参数
        //下面是设置图片放大缩小后如何选择像素点进行优化处理来让图片尽量保持清晰
        //设置纹理过滤参数
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        //解除纹理绑定
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }


    public static Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = BaseApplication.get().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 加载纹理
     *
     * @param textureWidth  宽
     * @param textureHeight 高
     * @return 纹理
     */
    public static int loadTexture(final int textureWidth, final int textureHeight) {
        int textures[] = new int[1];

        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, textureWidth, textureHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);    //copy到bind的纹理对象中

        return textures[0];
    }


    /**
     * 编译着色器
     *
     * @param type      着色器类型 {@link GLES20#GL_VERTEX_SHADER} {@link GLES20#GL_FRAGMENT_SHADER}
     * @param shadeCode 着色器源码
     * @return 着色器
     */
    public static int compileShader(int type, String shadeCode) {
        final int shaderObjectId = glCreateShader(type);

        if (shaderObjectId <= 0) {
            MTGLUtil.e(TAG, "创建shader失败");
            return 0;
        }
        glShaderSource(shaderObjectId, shadeCode);
        glCompileShader(shaderObjectId);

        if (MTGLUtil.DEBUG) {
            final int[] compileStatus = new int[1];
            glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);
            MTGLUtil.d(TAG, "GL_COMPILE_STATUS: " + compileStatus[0]
                    + "\n日志:" + glGetShaderInfoLog(shaderObjectId));
            if (compileStatus[0] == 0) {
                glDeleteShader(shaderObjectId);
                return 0;
            }
        }

        return shaderObjectId;
    }

    /**
     * 链接着色器到program上
     *
     * @param vertexShaderId   顶点着色器
     * @param fragmentShaderId 片段着色器
     * @return program
     */
    private static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        final int programObjectId = glCreateProgram();
        if (programObjectId <= 0) {
            MTGLUtil.e(TAG, "创建program失败");
            return 0;
        }

        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);

        glLinkProgram(programObjectId);

        if (MTGLUtil.DEBUG) {
            final int[] linkStatus = new int[1];
            glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);
            MTGLUtil.d(TAG, "GL_LINK_STATUS：" + linkStatus[0]
                    + "\n日志:" + glGetProgramInfoLog(programObjectId));
            if (linkStatus[0] == 0) {
                glDeleteProgram(programObjectId);
                return 0;
            }
        }

        return programObjectId;
    }

    /**
     * 判断program是否可用
     *
     * @param programObjectId program
     * @return 是否可用
     */
    private static boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
        MTGLUtil.d(TAG, "GL_VALIDATE_STATUS：" + validateStatus[0]
                + "\n日志:" + glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }

    /**
     * 构建program
     *
     * @param vertexShader   顶点着色器shader id
     * @param fragmentShader 片段着色器shader id
     * @return program
     */
    public static int buildProgram(int vertexShader, int fragmentShader) {
        int program;

        program = linkProgram(vertexShader, fragmentShader);

        if (MTGLUtil.DEBUG) {
            boolean validated = validateProgram(program);
            if (!validated) {
                return 0;
            }
        }
        return program;
    }

    public static void saveTexture(Bitmap bitmap) {
        File folder = new File(cacheTexturePath);
        if (!folder.exists() && !folder.mkdirs()) {
            MTGLUtil.d(TAG, "path is not exist");
            return;
        }
        final String jpegName = cacheTexturePath + "des.jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteCacheTextures() {
        File folder = new File(cacheTexturePath);
        if (!folder.exists()) {
            MTGLUtil.d(TAG, "path is not exist");
            return;
        }
        for (File file : folder.listFiles()) {
            file.delete();
        }
        folder.delete();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void switchToSwellFragment();

        void switchToFaceFragment();

        void saveAction();

        void clearAction();

        void saveBitmap();

        void exitBitmap();
    }

}
