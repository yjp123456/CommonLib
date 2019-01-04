attribute vec2 posCoord;
attribute vec2 texCoord;
varying vec2 texcoordOut;
uniform mat4 u_Matrix;


void main()
{
	texcoordOut = texCoord;
	gl_Position = u_Matrix * vec4(posCoord.x, posCoord.y, 0.0, 1.0);
}