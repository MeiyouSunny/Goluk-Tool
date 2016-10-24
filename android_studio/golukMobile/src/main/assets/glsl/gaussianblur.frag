#extension GL_OES_EGL_image_external : require
precision mediump float;
precision mediump int;
varying highp   vec2 offset[9];
uniform samplerExternalOES sampler;
uniform float       uni_Weight[5];
varying highp vec2 vTextureCoord;
uniform int    lastFrame;
void	main()
{
    vec4   sum = vec4(0.0);
    sum += texture2D(sampler, offset[0]) * uni_Weight[4] ;
    sum += texture2D(sampler, offset[1]) * uni_Weight[3];
    sum += texture2D(sampler, offset[2]) * uni_Weight[2];
    sum += texture2D(sampler, offset[3]) * uni_Weight[1];
    sum += texture2D(sampler, offset[4]) * uni_Weight[0];
    sum += texture2D(sampler, offset[5]) * uni_Weight[1];
    sum += texture2D(sampler, offset[6]) * uni_Weight[2];
    sum += texture2D(sampler, offset[7]) * uni_Weight[3];
    sum += texture2D(sampler, offset[8]) * uni_Weight[4];
    
    
//    sum += texture2D(sampler, offset[0]) ;
//    sum += texture2D(sampler, offset[1]);
//    sum += texture2D(sampler, offset[2]);
//    sum += texture2D(sampler, offset[3]);
//    sum += texture2D(sampler, offset[4]);
//    sum += texture2D(sampler, offset[5]) ;
//    sum += texture2D(sampler, offset[6]);
//    sum += texture2D(sampler, offset[7]);
//    sum += texture2D(sampler, offset[8]) ;
    if(lastFrame==1){
        gl_FragColor = sum;
 
    }else{
        gl_FragColor = texture2D(sampler,vTextureCoord);
    }
}
