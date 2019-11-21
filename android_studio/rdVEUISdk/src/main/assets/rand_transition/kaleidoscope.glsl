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


float speed = 1.0;
float angle = 1.0;
float power = 1.5;

vec4 transition(vec2 uv) {
    vec2 p = uv.xy / vec2(1.0).xy;
    vec2 q = p;
    float t = pow(progress, power)*speed;
    p = p -0.5;
    for (int i = 0; i < 7; i++) {
        p = vec2(sin(t)*p.x + cos(t)*p.y, sin(t)*p.y - cos(t)*p.x);
        t += angle;
        p = abs(mod(p, 2.0) - 1.0);
    }
    abs(mod(p, 1.0));
    return mix(
               mix(getFromColor(q), getToColor(q), progress),
               mix(getFromColor(p), getToColor(p), progress), 1.0 - 2.0*abs(progress - 0.5));
}
void main()
{
    gl_FragColor = transition(textureCoordinate);
}
