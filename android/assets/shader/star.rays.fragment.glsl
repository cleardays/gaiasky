#version 120

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// v_texCoords are UV coordinates in [0..1]
varying vec2 v_texCoords;
varying vec4 v_color;

uniform float u_radius;
uniform float u_apparent_angle;
uniform float u_inner_rad;
uniform float u_time;
uniform float u_thpoint;
// Distance in u to the star
uniform float u_distance;
// Whether light scattering is enabled or not
uniform int u_lightScattering;


// Time multiplier
#define time u_time * 0.02

// Constants as a factor of the radius
#define model_const 172.4643429
#define rays_const 30000.0

// Decays
#define corona_decay 0.2
#define light_decay 0.08

vec4 mod289(vec4 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0; }

float mod289(float x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0; }

vec4 permute(vec4 x) {
     return mod289(((x*34.0)+1.0)*x);
}

float permute(float x) {
     return mod289(((x*34.0)+1.0)*x);
}

vec4 taylorInvSqrt(vec4 r)
{
  return 1.79284291400159 - 0.85373472095314 * r;
}

float taylorInvSqrt(float r)
{
  return 1.79284291400159 - 0.85373472095314 * r;
}

vec4 grad4(float j, vec4 ip)
  {
  const vec4 ones = vec4(1.0, 1.0, 1.0, -1.0);
  vec4 p,s;

  p.xyz = floor( fract (vec3(j) * ip.xyz) * 7.0) * ip.z - 1.0;
  p.w = 1.5 - dot(abs(p.xyz), ones.xyz);
  s = vec4(lessThan(p, vec4(0.0)));
  p.xyz = p.xyz + (s.xyz*2.0 - 1.0) * s.www; 

  return p;
  }
						
// (sqrt(5) - 1)/4 = F4, used once below
#define F4 0.309016994374947451

float snoise(vec4 v)
  {
  const vec4  C = vec4( 0.138196601125011,  // (5 - sqrt(5))/20  G4
                        0.276393202250021,  // 2 * G4
                        0.414589803375032,  // 3 * G4
                       -0.447213595499958); // -1 + 4 * G4

// First corner
  vec4 i  = floor(v + dot(v, vec4(F4)) );
  vec4 x0 = v -   i + dot(i, C.xxxx);

// Other corners

// Rank sorting originally contributed by Bill Licea-Kane, AMD (formerly ATI)
  vec4 i0;
  vec3 isX = step( x0.yzw, x0.xxx );
  vec3 isYZ = step( x0.zww, x0.yyz );
//  i0.x = dot( isX, vec3( 1.0 ) );
  i0.x = isX.x + isX.y + isX.z;
  i0.yzw = 1.0 - isX;
//  i0.y += dot( isYZ.xy, vec2( 1.0 ) );
  i0.y += isYZ.x + isYZ.y;
  i0.zw += 1.0 - isYZ.xy;
  i0.z += isYZ.z;
  i0.w += 1.0 - isYZ.z;

  // i0 now contains the unique values 0,1,2,3 in each channel
  vec4 i3 = clamp( i0, 0.0, 1.0 );
  vec4 i2 = clamp( i0-1.0, 0.0, 1.0 );
  vec4 i1 = clamp( i0-2.0, 0.0, 1.0 );

  //  x0 = x0 - 0.0 + 0.0 * C.xxxx
  //  x1 = x0 - i1  + 1.0 * C.xxxx
  //  x2 = x0 - i2  + 2.0 * C.xxxx
  //  x3 = x0 - i3  + 3.0 * C.xxxx
  //  x4 = x0 - 1.0 + 4.0 * C.xxxx
  vec4 x1 = x0 - i1 + C.xxxx;
  vec4 x2 = x0 - i2 + C.yyyy;
  vec4 x3 = x0 - i3 + C.zzzz;
  vec4 x4 = x0 + C.wwww;

// Permutations
  i = mod289(i); 
  float j0 = permute( permute( permute( permute(i.w) + i.z) + i.y) + i.x);
  vec4 j1 = permute( permute( permute( permute (
             i.w + vec4(i1.w, i2.w, i3.w, 1.0 ))
           + i.z + vec4(i1.z, i2.z, i3.z, 1.0 ))
           + i.y + vec4(i1.y, i2.y, i3.y, 1.0 ))
           + i.x + vec4(i1.x, i2.x, i3.x, 1.0 ));

// Gradients: 7x7x6 points over a cube, mapped onto a 4-cross polytope
// 7*7*6 = 294, which is close to the ring size 17*17 = 289.
  vec4 ip = vec4(1.0/294.0, 1.0/49.0, 1.0/7.0, 0.0) ;

  vec4 p0 = grad4(j0,   ip);
  vec4 p1 = grad4(j1.x, ip);
  vec4 p2 = grad4(j1.y, ip);
  vec4 p3 = grad4(j1.z, ip);
  vec4 p4 = grad4(j1.w, ip);

// Normalise gradients
  vec4 norm = taylorInvSqrt(vec4(dot(p0,p0), dot(p1,p1), dot(p2, p2), dot(p3,p3)));
  p0 *= norm.x;
  p1 *= norm.y;
  p2 *= norm.z;
  p3 *= norm.w;
  p4 *= taylorInvSqrt(dot(p4,p4));

// Mix contributions from the five corners
  vec3 m0 = max(0.6 - vec3(dot(x0,x0), dot(x1,x1), dot(x2,x2)), 0.0);
  vec2 m1 = max(0.6 - vec2(dot(x3,x3), dot(x4,x4)            ), 0.0);
  m0 = m0 * m0;
  m1 = m1 * m1;
  return 49.0 * ( dot(m0*m0, vec3( dot( p0, x0 ), dot( p1, x1 ), dot( p2, x2 )))
               + dot(m1*m1, vec2( dot( p3, x3 ), dot( p4, x4 ) ) ) ) ;

  }

  float noise(vec4 position, int octaves, float frequency, float persistence) {
      float total = 0.0; // Total value so far
      float maxAmplitude = 0.0; // Accumulates highest theoretical amplitude
      float amplitude = 1.0;
      for (int i = 0; i < octaves; i++) {
          // Get the noise sample
          total += snoise(position * frequency) * amplitude;
          // Make the wavelength twice as small
          frequency *= 2.0;
          // Add to our maximum possible amplitude
          maxAmplitude += amplitude;
          // Reduce amplitude according to persistence for the next octave
          amplitude *= persistence;
      }
      // Scale the result by the maximum amplitude
      return total / maxAmplitude;
  }

float core(float distance_center, float inner_rad){
	float core = 1.0 - step(inner_rad / 5.0, distance_center);
	float core_glow = smoothstep(inner_rad / 2.0, inner_rad / 5.0, distance_center);
	return core_glow + core;
}

float light(float distance_center, float decay) {
    float light = 1.0 - pow(distance_center, decay);
    return clamp(light, 0.0, 0.97);
}

float corona(float distance_center, float cor_decay, float cor_noise){
        vec3 fPosition = vec3(v_texCoords - vec2(0.5), 0.0) * 2.0;

        // Move outward
        float t = time - length(fPosition) * cor_noise;
        // Offset normal with noise
        float frequency = 1.5;
        float ox = snoise(vec4(fPosition * frequency, t));
        float oy = snoise(vec4((fPosition + 2000.0) * frequency, t));
        float oz = snoise(vec4((fPosition + 4000.0) * frequency, t));
        // Store offsetVec since we want to use it twice.
        vec3 offsetVec = vec3(ox, oy, oz) * 0.0003;

        // Get the distance vector from the centre
        vec3 nDistVec = normalize(fPosition + offsetVec);

        // Get noise with normalized position to offset the original position
        vec3 position = fPosition + noise(vec4(nDistVec, t), 5, 2.0, 0.7) * 0.3;    
        float dist = length(position + offsetVec);
 
        // Calculate brightness based on distance
        return dist * (1.0 - pow(distance_center, cor_decay));
}

vec4 draw() {
    float dist = distance (vec2 (0.5), v_texCoords.xy) * 2.0;
    vec2 uv = v_texCoords - 0.5;

	float level = (u_distance - u_radius) / ((u_radius * model_const) - u_radius);

	if(level >= 1.0){
		// We are far away from the star
		level = u_distance / (u_radius * rays_const);
		float light_level = smoothstep(u_thpoint, u_thpoint * 1.4, u_apparent_angle);
				
		if(u_lightScattering == 1){
			// Light scattering, simple star
			float core = core(dist, u_inner_rad);
			float light = light(dist, light_decay / 3.0) * light_level;
			return vec4(v_color.rgb + vec3(core * 10.0), light + core);
		} else {
			// No light scattering, star rays
			level = min(level, 1.0);
			float corona = corona(dist, corona_decay, 0.0);
	        float light = light(dist, light_decay) * light_level;
	        float core = core(dist, u_inner_rad);
	
			return vec4(v_color.rgb + core, (corona * (1.0 - level) + light + core));
		}
	} else {
		// We are close to the star
		
		level = min(level, 1.0);
		float level_corona = u_lightScattering * level;
        
        	float corona = corona(dist, corona_decay, 0.5 - level / 2.0);
        	float light = light(dist, light_decay);
        	float core = core(dist, u_inner_rad);

		return vec4(v_color.rgb + core, (corona * (1.0 - level_corona) + light + level * core));
	}
}

void main() {
    gl_FragColor = clamp(draw(), 0.0, 1.0);
}
