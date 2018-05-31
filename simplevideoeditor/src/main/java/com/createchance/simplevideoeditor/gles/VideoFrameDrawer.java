package com.createchance.simplevideoeditor.gles;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;

/**
 * ${DESC}
 *
 * @author gaochao1-iri
 * @date 30/04/2018
 */
public class VideoFrameDrawer {

    private static final String TAG = "VideoFrameDrawer";

    /**
     * 用于显示的变换矩阵
     */
    private float[] SM = new float[16];
    private SurfaceTexture surfaceTexture;

    /**
     * Filter draw origin frames of video to texture.
     */
    private OesFilter mOesFilter;
    /**
     * 显示的滤镜
     */
    private NoFilter mShow;

    /**
     * Surface's size
     */
    private int surfaceWidth;
    private int surfaceHeight;

    /**
     * 用于视频旋转的参数
     */
    private int rotation;

    private int[] fboFrame = new int[1];
    private int[] fboTexture = new int[1];

    private List<AbstractFilter> filterList = new ArrayList<>();

    public VideoFrameDrawer() {
        mOesFilter = new OesFilter();
        mShow = new NoFilter();
    }

    public void createSurfaceTexture() {
        int oesTexture = OpenGlUtil.createOneOesTexture();
        surfaceTexture = new SurfaceTexture(oesTexture);
        mOesFilter.setInputTextureId(oesTexture);
    }

    public void setSurfaceSize(int width, int height) {
        surfaceWidth = width;
        surfaceHeight = height;

        // delete old frame buffer.
//        deleteFrameBuffer();
        // create a new frame buffer now.
//        createFrameBuffer();

        mOesFilter.setViewSize(surfaceWidth, surfaceHeight);

        for (AbstractFilter filter : filterList) {
            filter.setViewSize(surfaceWidth, surfaceHeight);
        }

//        mShow.setInputTextureId(fboTexture[0]);
    }

    public void draw() {
        surfaceTexture.updateTexImage();
        // clear screen to black default.
//        glClearColor(0.0f, 1.0f, 0.0f, 0.0f);
//        glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
//        bindFrameBuffer();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        mOesFilter.draw();
        for (AbstractFilter filter : filterList) {
            filter.draw();
        }
//        unbindFrameBuffer();
//        mShow.draw();
        OpenGlUtil.assertNoError("onDrawFrame");
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
        if (mOesFilter != null) {
            mOesFilter.setRotation(this.rotation);
        }
    }

    public void release() {
//        deleteFrameBuffer();
    }

    public void addFilter(AbstractFilter filter) {
        filterList.add(filter);
    }

    private void createFrameBuffer() {
        GLES20.glGenFramebuffers(1, fboFrame, 0);
        GLES20.glGenTextures(1, fboTexture, 0);
        // bind to fbo texture cause we are going to do setting.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTexture[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, surfaceWidth, surfaceHeight,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        // 设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        // 设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        // 设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        // 设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        // unbind fbo texture.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private void bindFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboFrame[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, fboTexture[0], 0);
    }

    private void unbindFrameBuffer() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, fboFrame, 0);
        GLES20.glDeleteTextures(1, fboTexture, 0);
    }
}
