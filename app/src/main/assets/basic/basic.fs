precision highp float;

varying vec2 texcoordOut;
uniform sampler2D srctexture;         //原图

void main()
{
   gl_FragColor = texture2D(srctexture, texcoordOut);
}