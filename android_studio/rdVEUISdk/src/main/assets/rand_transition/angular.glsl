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


#define PI 3.141592653589

float startingAngle = 90.0;

vec4 transition (vec2 uv) {
    
    float offset = startingAngle * PI / 180.0;
    float angle = atan(uv.y - 0.5, uv.x - 0.5) + offset;
    float normalizedAngle = (angle + PI) / (2.0 * PI);
    
    normalizedAngle = normalizedAngle - floor(normalizedAngle);
    
    return mix(
               getFromColor(uv),
               getToColor(uv),
               step(normalizedAngle, progress)
               );
}
void main(){
    gl_FragColor = transition(textureCoordinate);
}
