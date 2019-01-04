#extension GL_OES_EGL_image_external : require
precision highp float;

varying vec2 texcoordOut;
uniform samplerExternalOES srctexture;         //原图

void main()
{
   gl_FragColor = texture2D(srctexture, texcoordOut);
}