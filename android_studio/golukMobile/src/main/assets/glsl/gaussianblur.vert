attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vTextureCoord;
uniform float                uni_Size;

varying vec2 offset[9];

void	main()
{
    gl_Position = aPosition;
    vTextureCoord = (aTextureCoord).xy ;
    float   x = 1.0/uni_Size;
    float   xc = 2.0 * x;
    float   y  = 1.0/uni_Size;
    float   yc = 2.0*y ;
    offset[0] = vec2(aTextureCoord.x - xc, aTextureCoord.y-yc);
    offset[1] = vec2(aTextureCoord.x - x, aTextureCoord.y-y);
    offset[2] = vec2(aTextureCoord.x - xc, aTextureCoord.y+yc);
    offset[3] = vec2(aTextureCoord.x - x, aTextureCoord.y+y);
    offset[4] = aTextureCoord;
    offset[5] = vec2(aTextureCoord.x + x, aTextureCoord.y+y);
    offset[6] = vec2(aTextureCoord.x + xc, aTextureCoord.y+yc);
    offset[7] = vec2(aTextureCoord.x + x, aTextureCoord.y-y);
    offset[8] = vec2(aTextureCoord.x + xc, aTextureCoord.y-yc);
}
