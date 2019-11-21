precision highp float;

varying vec2 textureCoordinate;
uniform sampler2D from;
uniform sampler2D to;
uniform float progress;

vec4 getFromColor(vec2 uv) {
    return texture2D(from,uv);
}

vec4 getToColor(vec2 uv){
    return texture2D(to,uv);
}
// Author: gre
// License: MIT

float inHeart (vec2 p, vec2 center, float size) {
    if (size==0.0) return 0.0;
    vec2 o = (p-center)/(1.6*size);
    
    float a = o.x*o.x+o.y*o.y-0.3;
    return step(a*a*a, o.x*o.x*o.y*o.y*o.y);
}
vec4 transition (vec2 uv) {
    
#ifdef ANDROID
    return mix(
               getFromColor(uv),
               getToColor(uv),
               inHeart(uv, vec2(0.5, 0.4), progress)
               );
#else
    vec2 dst = uv;
    dst.y = 1.0-uv.y;
    return mix(
               getFromColor(uv),
               getToColor(uv),
               inHeart(dst, vec2(0.5, 0.4), progress)
               );
#endif
    
}
void main(){
    gl_FragColor = transition(textureCoordinate);
}
