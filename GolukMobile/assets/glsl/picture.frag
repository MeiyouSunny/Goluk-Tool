#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sampler ;
void main() {
    gl_FragColor = texture2D(sampler, vTextureCoord);
}