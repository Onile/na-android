package com.na.camera.filter;

import com.na.camera.filter.gpuimage.GPUImageFilter;

/**
 * @actor:taotao
 * @DATE: 17/3/1
 */

public abstract class NaFilter extends GPUImageFilter implements INaFilter {
    public final static String POSITION_COORDINATE = "position";
    public final static String TEXTURE_UNIFORM = "inputImageTexture";
    public final static String TEXTURE_COORDINATE = "inputTextureCoordinate";

    public NaFilter() {
        super();
    }

    public NaFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }
}
