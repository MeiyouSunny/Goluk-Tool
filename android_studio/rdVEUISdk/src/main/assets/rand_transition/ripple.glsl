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
const float amplitude= 100.0;
const float speed = 50.0;

vec4 transition (vec2 uv) {
  vec2 dir = uv - vec2(.5);
  float dist = length(dir);
  vec2 offset = dir * (sin(progress * dist * amplitude - progress * speed) + .5) / 30.;
  return mix(
    getFromColor(uv + offset),
    getToColor(uv),
    smoothstep(0.2, 1.0, progress)
  );
}
void main(){
	gl_FragColor = transition(textureCoordinate);
}
