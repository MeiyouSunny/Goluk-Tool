#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sampler;
void main() {
    lowp vec4 c1= texture2D(sampler, vTextureCoord);
    gl_FragColor = c1;
}