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


vec2 offset(float progress, float x, float theta) {
  float phase = progress*progress + progress + theta;
  float shifty = 0.03*progress*cos(10.0*(progress+x));
  return vec2(0, shifty);
}
vec4 transition(vec2 p) {
  return mix(getFromColor(p + offset(progress, p.x, 0.0)), getToColor(p + offset(1.0-progress, p.x, 3.14)), progress);
}
void main(){
	gl_FragColor = transition(textureCoordinate);
}
