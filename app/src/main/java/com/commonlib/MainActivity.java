package com.commonlib;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.commonlib.filter.EffectFilter;
import com.commonlib.gl.util.MTGLUtil;
import com.commonlib.view.PhotoSurfaceView;

import static android.opengl.GLES20.glDeleteTextures;

public class MainActivity extends Activity {
    PhotoSurfaceView mSurfaceView;
    private EffectFilter mPhotoFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = findViewById(R.id.img_photo);
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceView.setZOrderMediaOverlay(true);
        mPhotoFilter = new EffectFilter();
        mSurfaceView.addDrawRun(() -> mPhotoFilter.init());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        mSurfaceView.setOriginBitmap(bitmap);
        setFilter("materials/mask.png");
    }

    /**
     * 给照片添加滤镜效果
     *
     * @param filterAssetPath 滤镜效果基准图路径
     */
    public void setFilter(final String filterAssetPath) {
        mSurfaceView.addDrawRun(() -> {
            Bitmap mask = MTGLUtil.getImageFromAssetsFile(filterAssetPath);
            if (mask != null) {
                glDeleteTextures(1, new int[]{mPhotoFilter.getMaterialMaskTextureId()}, 0);
                mPhotoFilter.setMaterialMaskTextureId(MTGLUtil.NO_TEXTURE);
                int mTextureEraserMask = MTGLUtil.loadTexture(mask, true);
                mPhotoFilter.setMaterialMaskTextureId(mTextureEraserMask);
                mSurfaceView.setFilter(mPhotoFilter);
            }
        });
    }

}
