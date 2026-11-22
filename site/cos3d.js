/* YA 3D: renders the real YA CLIENT cosmetics (geometry exported from the game code, see geo.js) on a 3D mannequin. No libraries. */
(function () {
  var D = Math.PI / 180;
  var VS = 'attribute vec3 p;attribute vec4 c;attribute vec2 t;uniform mat4 m;varying vec4 vc;varying vec2 vt;void main(){vc=c;vt=t;gl_Position=m*vec4(p,1.);}';
  var FS = 'precision mediump float;varying vec4 vc;varying vec2 vt;uniform sampler2D s;uniform float ut;void main(){vec4 col=vc;if(ut>.5)col*=texture2D(s,vt);if(col.a<.04)discard;gl_FragColor=col;}';

  // ---- tiny matrix library (column-major) ----
  function I() { return [1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1]; }
  function mul(a, b) { var o = new Array(16); for (var c = 0; c < 4; c++) for (var r = 0; r < 4; r++) { var s = 0; for (var k = 0; k < 4; k++) s += a[k * 4 + r] * b[c * 4 + k]; o[c * 4 + r] = s; } return o; }
  function T(x, y, z) { var m = I(); m[12] = x; m[13] = y; m[14] = z; return m; }
  function S(x, y, z) { var m = I(); m[0] = x; m[5] = y; m[10] = z; return m; }
  function RX(a) { var c = Math.cos(a), s = Math.sin(a), m = I(); m[5] = c; m[6] = s; m[9] = -s; m[10] = c; return m; }
  function RY(a) { var c = Math.cos(a), s = Math.sin(a), m = I(); m[0] = c; m[2] = -s; m[8] = s; m[10] = c; return m; }
  function RZ(a) { var c = Math.cos(a), s = Math.sin(a), m = I(); m[0] = c; m[1] = s; m[4] = -s; m[5] = c; return m; }
  function persp(fov, asp, n, f) { var t = 1 / Math.tan(fov / 2), m = new Array(16).fill(0); m[0] = t / asp; m[5] = t; m[10] = (f + n) / (n - f); m[11] = -1; m[14] = 2 * f * n / (n - f); return m; }
  function look(e, c) {
    var z = [e[0] - c[0], e[1] - c[1], e[2] - c[2]], l = Math.hypot(z[0], z[1], z[2]); z = z.map(function (v) { return v / l; });
    var x = [z[2], 0, -z[0]]; l = Math.hypot(x[0], x[1], x[2]); x = x.map(function (v) { return v / l; });
    var y = [z[1] * x[2] - z[2] * x[1], z[2] * x[0] - z[0] * x[2], z[0] * x[1] - z[1] * x[0]];
    return [x[0], y[0], z[0], 0, x[1], y[1], z[1], 0, x[2], y[2], z[2], 0, -(x[0] * e[0] + x[1] * e[1] + x[2] * e[2]), -(y[0] * e[0] + y[1] * e[1] + y[2] * e[2]), -(z[0] * e[0] + z[1] * e[1] + z[2] * e[2]), 1];
  }

  // ---- geometry helpers: stride 9 = x y z r g b a u v ----
  function quadsToTris(v, stride) {
    var n = v.length / stride, out = [];
    function push(i) { var o = i * stride; out.push(v[o], v[o + 1], v[o + 2], v[o + 3], v[o + 4], v[o + 5], v[o + 6], stride > 7 ? v[o + 7] : 0, stride > 7 ? v[o + 8] : 0); }
    for (var q = 0; q < n; q += 4) { push(q); push(q + 1); push(q + 2); push(q); push(q + 2); push(q + 3); }
    return new Float32Array(out);
  }
  var GEO = window.YA_GEO || {};
  // ---- cape thickness (like the vanilla cape): the baked cloth is pushed back by THK, the inner face and the sides are built here
  var THK = 0.055, CEDGE = {"ya": 5900810, "mono": 789518, "qlux": 13225949, "sakura": 15178420, "shinobi": 1710626, "kitsune": 14248234, "santa": 11542046, "frost": 10276072, "clown": 14737640, "luck": 3051336, "sky": 6471167, "sunset": 3943034, "forest": 1989162, "ocean": 1723002, "space": 1182250, "storm": 1711146, "vampire": 657933, "vamp_crimson": 2753801, "vamp_scarlet": 3804424, "vamp_orange": 4004870, "vamp_amber": 3877382, "vamp_gold": 4207116, "vamp_lime": 1978378, "vamp_green": 731156, "vamp_emerald": 337952, "vamp_teal": 470574, "vamp_cyan": 668480, "vamp_sky": 1192014, "vamp_blue": 793156, "vamp_indigo": 1183548, "vamp_violet": 2232383, "vamp_purple": 1706798, "vamp_magenta": 3804976, "vamp_pink": 4856376, "vamp_rose": 4198431, "vamp_white": 15001326, "vamp_silver": 2895672};
  function capeIn(u, v) { return [(u - .5) * .66 * (1 + .06 * v), v, .012 + .02 * v]; }
  function capeOut(u, v) { var p = capeIn(u, v); p[2] += THK; return p; }
  function shadeN(a, b, c, d) {
    var ux = b[0] - a[0], uy = b[1] - a[1], uz = b[2] - a[2], vx = d[0] - a[0], vy = d[1] - a[1], vz = d[2] - a[2];
    var nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx, l = Math.sqrt(nx * nx + ny * ny + nz * nz) || 1;
    return .4 + .6 * Math.abs((nx * .3 - ny * .78 - nz * .55) / (l * 1.0004));
  }
  function capeShell(sty) {
    var h = CEDGE[sty] == null ? 0x16161c : CEDGE[sty], e = [(h >> 16 & 255) / 255, (h >> 8 & 255) / 255, (h & 255) / 255], out = [], C = 14, R = 40;
    function q(a, b, c, d, col, k) { var s = shadeN(a, b, c, d); [a, b, c, d].forEach(function (p) { out.push(p[0], p[1], p[2], col[0] * s * k, col[1] * s * k, col[2] * s * k, 1); }); }
    var i, j;
    for (i = 0; i < C; i++) for (j = 0; j < R; j++) q(capeIn(i / C, j / R), capeIn((i + 1) / C, j / R), capeIn((i + 1) / C, (j + 1) / R), capeIn(i / C, (j + 1) / R), e, .7);
    for (j = 0; j < R; j++) for (i = 0; i <= 1; i++) q(capeOut(i, j / R), capeOut(i, (j + 1) / R), capeIn(i, (j + 1) / R), capeIn(i, j / R), e, 1);
    for (i = 0; i < C; i++) q(capeOut(i / C, 1), capeOut((i + 1) / C, 1), capeIn((i + 1) / C, 1), capeIn(i / C, 1), e, 1);
    return out;
  }

  function batches(key) { return GEO[key] || []; }

  // mannequin (model space: y down, 1 px = 1/16)
  function mannequin() {
    var out = [];
    function face(a, b, c, d, col, k) { [a, b, c, a, c, d].forEach(function (p) { out.push(p[0], p[1], p[2], col[0] * k, col[1] * k, col[2] * k, 1, 0, 0); }); }
    function box(x0, y0, z0, x1, y1, z1, col) {
      x0 /= 16; x1 /= 16; y0 /= 16; y1 /= 16; z0 /= 16; z1 /= 16;
      face([x0, y0, z0], [x1, y0, z0], [x1, y1, z0], [x0, y1, z0], col, .93);   // front (-z)
      face([x0, y0, z1], [x1, y0, z1], [x1, y1, z1], [x0, y1, z1], col, .72);   // back (+z)
      face([x0, y0, z0], [x0, y0, z1], [x0, y1, z1], [x0, y1, z0], col, .8);
      face([x1, y0, z0], [x1, y0, z1], [x1, y1, z1], [x1, y1, z0], col, .8);
      face([x0, y0, z0], [x1, y0, z0], [x1, y0, z1], [x0, y0, z1], col, 1);     // top (y up is -y)
      face([x0, y1, z0], [x1, y1, z0], [x1, y1, z1], [x0, y1, z1], col, .5);
    }
    var skin = [.74, .74, .78], torso = [.52, .52, .58], leg = [.4, .4, .46];
    box(-4, -8, -4, 4, 0, 4, skin);
    box(-4, 0, -2, 4, 12, 2, torso);
    box(-8, 0, -2, -4, 12, 2, skin); box(4, 0, -2, 8, 12, 2, skin);
    box(-4, 12, -2, 0, 24, 2, leg); box(0, 12, -2, 4, 24, 2, leg);
    // eyes
    function eye(x0, x1) { var z = -4.01 / 16; face([x0 / 16, -4.4 / 16, z], [x1 / 16, -4.4 / 16, z], [x1 / 16, -3.2 / 16, z], [x0 / 16, -3.2 / 16, z], [.08, .08, .1], 1); }
    eye(-2.8, -1.2); eye(1.2, 2.8);
    return new Float32Array(out);
  }

  var logoImg = null, logoReady = false;
  function loadLogo(cb) {
    if (logoImg) { if (logoReady) cb(); else logoImg.addEventListener('load', cb); return; }
    logoImg = new Image(); logoImg.onload = function () { logoReady = true; cb(); }; logoImg.src = '/logo.png';
  }

  var ITEM = { ya_cape: ['cape', 'ya'], ya_wings: ['wing', 'ya'], ya_bandana: ['bandana', 'ya'], ya_pet: ['pet', 'ya'],
               mono_cape: ['cape', 'mono'], mono_wings: ['wing', 'mono'], mono_bandana: ['bandana', 'mono'], mono_pet: ['pet', 'mono'],
               qlux_cape: ['cape', 'qlux'], qlux_wings: ['wing', 'qlux'], qlux_bandana: ['bandana', 'qlux'], qlux_pet: ['pet', 'qlux'], sakura_cape: ['cape', 'sakura'], sakura_wings: ['wing', 'sakura'], sakura_bandana: ['bandana', 'sakura'], sakura_pet: ['pet', 'sakura'], shinobi_cape: ['cape', 'shinobi'], shinobi_wings: ['wing', 'shinobi'], shinobi_bandana: ['bandana', 'shinobi'], shinobi_pet: ['pet', 'shinobi'], kitsune_cape: ['cape', 'kitsune'], kitsune_wings: ['wing', 'kitsune'], kitsune_bandana: ['bandana', 'kitsune'], kitsune_pet: ['pet', 'kitsune'], santa_cape: ['cape', 'santa'], santa_wings: ['wing', 'santa'], santa_bandana: ['bandana', 'santa'], santa_pet: ['pet', 'santa'], frost_cape: ['cape', 'frost'], frost_wings: ['wing', 'frost'], frost_bandana: ['bandana', 'frost'], frost_pet: ['pet', 'frost'], clown_cape: ['cape', 'clown'], clown_wings: ['wing', 'clown'], clown_bandana: ['bandana', 'clown'], clown_pet: ['pet', 'clown'], luck_cape: ['cape', 'luck'], luck_wings: ['wing', 'luck'], luck_bandana: ['bandana', 'luck'], luck_pet: ['pet', 'luck'], sky_cape: ['cape', 'sky'], ocean_cape: ['cape', 'ocean'], sunset_cape: ['cape', 'sunset'], forest_cape: ['cape', 'forest'], space_cape: ['cape', 'space'], dragon_wings: ['wing', 'dragon'], butterfly_wings: ['wing', 'butterfly'], azure_wings: ['wing', 'azure'], venom_wings: ['wing', 'venom'], dragonfly_wings: ['wing', 'dragonfly'], ninja_bandana: ['bandana', 'ninja'], storm_cape: ['cape', 'storm'], vampire_wings: ['wing', 'vampire'], vampire_cape: ['cape', 'vampire'], vamp_crimson_cape: ['cape', 'vamp_crimson'], vamp_scarlet_cape: ['cape', 'vamp_scarlet'], vamp_orange_cape: ['cape', 'vamp_orange'], vamp_amber_cape: ['cape', 'vamp_amber'], vamp_gold_cape: ['cape', 'vamp_gold'], vamp_lime_cape: ['cape', 'vamp_lime'], vamp_green_cape: ['cape', 'vamp_green'], vamp_emerald_cape: ['cape', 'vamp_emerald'], vamp_teal_cape: ['cape', 'vamp_teal'], vamp_cyan_cape: ['cape', 'vamp_cyan'], vamp_sky_cape: ['cape', 'vamp_sky'], vamp_blue_cape: ['cape', 'vamp_blue'], vamp_indigo_cape: ['cape', 'vamp_indigo'], vamp_violet_cape: ['cape', 'vamp_violet'], vamp_purple_cape: ['cape', 'vamp_purple'], vamp_magenta_cape: ['cape', 'vamp_magenta'], vamp_pink_cape: ['cape', 'vamp_pink'], vamp_rose_cape: ['cape', 'vamp_rose'], vamp_white_cape: ['cape', 'vamp_white'], vamp_silver_cape: ['cape', 'vamp_silver'], vamp_crimson_wings: ['wing', 'vamp_crimson'], vamp_scarlet_wings: ['wing', 'vamp_scarlet'], vamp_orange_wings: ['wing', 'vamp_orange'], vamp_amber_wings: ['wing', 'vamp_amber'], vamp_gold_wings: ['wing', 'vamp_gold'], vamp_lime_wings: ['wing', 'vamp_lime'], vamp_green_wings: ['wing', 'vamp_green'], vamp_emerald_wings: ['wing', 'vamp_emerald'], vamp_teal_wings: ['wing', 'vamp_teal'], vamp_cyan_wings: ['wing', 'vamp_cyan'], vamp_sky_wings: ['wing', 'vamp_sky'], vamp_blue_wings: ['wing', 'vamp_blue'], vamp_indigo_wings: ['wing', 'vamp_indigo'], vamp_violet_wings: ['wing', 'vamp_violet'], vamp_purple_wings: ['wing', 'vamp_purple'], vamp_magenta_wings: ['wing', 'vamp_magenta'], vamp_pink_wings: ['wing', 'vamp_pink'], vamp_rose_wings: ['wing', 'vamp_rose'], vamp_white_wings: ['wing', 'vamp_white'], vamp_silver_wings: ['wing', 'vamp_silver'], puppy_pet: ['pet', 'puppy'], crown_bandana: ['bandana', 'crown'], halo_bandana: ['bandana', 'halo'], cat_bandana: ['bandana', 'cat'], rainbow_bandana: ['bandana', 'rainbow'], drake_pet: ['pet', 'drake'], bunny_pet: ['pet', 'bunny'], penguin_pet: ['pet', 'penguin'], panda_pet: ['pet', 'panda'], axolotl_pet: ['pet', 'axolotl'] };

  function Scene(canvas, opts) {
    opts = opts || {};
    var gl = canvas.getContext('webgl', { alpha: true, premultipliedAlpha: false, antialias: true, preserveDrawingBuffer: !!opts.preserve }) || canvas.getContext('experimental-webgl');
    if (!gl) { this.failed = true; return; }
    var self = this; this.gl = gl; this.canvas = canvas;
    function sh(type, src) { var s = gl.createShader(type); gl.shaderSource(s, src); gl.compileShader(s); return s; }
    var pr = gl.createProgram(); gl.attachShader(pr, sh(gl.VERTEX_SHADER, VS)); gl.attachShader(pr, sh(gl.FRAGMENT_SHADER, FS)); gl.linkProgram(pr); gl.useProgram(pr);
    this.pr = pr; this.loc = { p: gl.getAttribLocation(pr, 'p'), c: gl.getAttribLocation(pr, 'c'), t: gl.getAttribLocation(pr, 't'), m: gl.getUniformLocation(pr, 'm'), ut: gl.getUniformLocation(pr, 'ut'), s: gl.getUniformLocation(pr, 's') };
    this.meshes = {}; this.items = []; this.plus = false; this.yaw = opts.yaw == null ? .5 : opts.yaw; this.pitch = opts.pitch == null ? .12 : opts.pitch;
    this.dist = opts.dist || 4.4; this.target = opts.target || [0, 1.0, 0]; this.fov = (opts.fov || 30) * D; this.auto = opts.auto !== false; this.t0 = performance.now();
    this.man = this.mesh(mannequin());
    var disc = [], N = 48; for (var i = 0; i < N; i++) { var a0 = i / N * 6.2832, a1 = (i + 1) / N * 6.2832;
      [[0, 0], [Math.cos(a0), Math.sin(a0)], [Math.cos(a1), Math.sin(a1)]].forEach(function (p) { disc.push(p[0] * .85, .002, p[1] * .85, 0, 0, 0, p[0] === 0 && p[1] === 0 ? .5 : 0, 0, 0); }); }
    this.shadow = this.mesh(new Float32Array(disc));
    this.tex = gl.createTexture();
    loadLogo(function () { gl.bindTexture(gl.TEXTURE_2D, self.tex); gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true); gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, logoImg);
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE); self.texOk = true; });
    this.etex = {}; this.eok = {};
    ['qlux', 'sakura', 'shinobi', 'kitsune', 'santa', 'frost', 'clown', 'luck'].forEach(function (st) {
      var tx = gl.createTexture(); self.etex[st] = tx;
      var im = new Image(); im.onload = function () { gl.bindTexture(gl.TEXTURE_2D, tx); gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, false); gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, im);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, (st === 'qlux' || st === 'sakura') ? gl.LINEAR : gl.NEAREST); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, (st === 'qlux' || st === 'sakura') ? gl.LINEAR : gl.NEAREST); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE); gl.bindTexture(gl.TEXTURE_2D, self.tex); self.eok[st] = true; };
      im.src = st === 'qlux' ? '/qlux-wordmark.png' : '/anime-' + st + '.png';
    });
    // drag to rotate
    if (opts.drag !== false) {
      var down = false, lx = 0, ly = 0;
      canvas.style.touchAction = 'none'; canvas.style.cursor = 'grab';
      canvas.addEventListener('pointerdown', function (e) { down = true; lx = e.clientX; ly = e.clientY; self.pause = true; canvas.setPointerCapture(e.pointerId); canvas.style.cursor = 'grabbing'; });
      canvas.addEventListener('pointermove', function (e) { if (!down) return; self.yaw -= (e.clientX - lx) * .009; self.pitch = Math.max(-.3, Math.min(.6, self.pitch + (e.clientY - ly) * .005)); lx = e.clientX; ly = e.clientY; });
      var up = function () { down = false; canvas.style.cursor = 'grab'; clearTimeout(self.rt); self.rt = setTimeout(function () { self.pause = false; }, 2200); };
      canvas.addEventListener('pointerup', up); canvas.addEventListener('pointercancel', up);
    }
  }
  Scene.prototype.mesh = function (arr, dynamic) { var gl = this.gl, b = gl.createBuffer(); gl.bindBuffer(gl.ARRAY_BUFFER, b); gl.bufferData(gl.ARRAY_BUFFER, arr, dynamic ? gl.DYNAMIC_DRAW : gl.STATIC_DRAW); return { b: b, n: arr.length / 9 }; };
  Scene.prototype.get = function (key, bi) {   // cached static mesh for batch bi of a geometry key
    var id = key + '#' + bi; if (!this.meshes[id]) { var bt = batches(key)[bi]; this.meshes[id] = bt ? { m: this.mesh(quadsToTris(bt.v, bt.t ? 9 : 7)), tex: !!bt.t } : { m: null }; } return this.meshes[id];
  };
  Scene.prototype.set = function (items, plus) { this.items = items.slice(); this.plus = !!plus; };
  Scene.prototype.drawMesh = function (m, mat, tex) {
    var gl = this.gl, L = this.loc; if (!m || !m.n) return;
    gl.bindBuffer(gl.ARRAY_BUFFER, m.b);
    gl.enableVertexAttribArray(L.p); gl.vertexAttribPointer(L.p, 3, gl.FLOAT, false, 36, 0);
    gl.enableVertexAttribArray(L.c); gl.vertexAttribPointer(L.c, 4, gl.FLOAT, false, 36, 12);
    gl.enableVertexAttribArray(L.t); gl.vertexAttribPointer(L.t, 2, gl.FLOAT, false, 36, 28);
    gl.uniformMatrix4fv(L.m, false, new Float32Array(mat)); gl.uniform1f(L.ut, tex ? 1 : 0);
    gl.drawArrays(gl.TRIANGLES, 0, m.n);
  };
  // lerp the animated plus cape between exported frames
  Scene.prototype.plusCape = function (age) {
    var F = batches('cape_ya_plus'), n = F.length, f = ((age % 64) / 64) * n, i = Math.floor(f) % n, j = (i + 1) % n, k = f - Math.floor(f);
    var A = F[i][0], B = F[j][0], out = new Float32Array(A.v.length / 7 * 9);
    if (!this.pcBuf) this.pcBuf = this.mesh(out, true);
    var src = quadsToTris(A.v, 7), dst = quadsToTris(B.v, 7);
    for (var q = 0; q < src.length; q++) { if (q % 9 > 6) continue; src[q] = src[q] + (dst[q] - src[q]) * k; }
    var gl = this.gl; gl.bindBuffer(gl.ARRAY_BUFFER, this.pcBuf.b); gl.bufferData(gl.ARRAY_BUFFER, src, gl.DYNAMIC_DRAW); this.pcBuf.n = src.length / 9;
    var em = F[Math.round(f) % n][1];
    if (em) { var id = 'plusem#' + (Math.round(f) % n); if (!this.meshes[id]) this.meshes[id] = { m: this.mesh(quadsToTris(em.v, 9)) }; this.pcEm = this.meshes[id].m; } else this.pcEm = null;
  };
  Scene.prototype.frame = function (now) {
    var gl = this.gl, c = this.canvas, dpr = Math.min(window.devicePixelRatio || 1, 2);
    var w = Math.round(c.clientWidth * dpr) || c.width, h = Math.round(c.clientHeight * dpr) || c.height;
    if (c.width !== w || c.height !== h) { c.width = w; c.height = h; }
    gl.viewport(0, 0, c.width, c.height); gl.clearColor(0, 0, 0, 0); gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
    gl.enable(gl.DEPTH_TEST); gl.enable(gl.BLEND); gl.blendFuncSeparate(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA, gl.ONE, gl.ONE_MINUS_SRC_ALPHA); gl.disable(gl.CULL_FACE);
    var t = (now - this.t0) / 1000, age = t * 20;
    if (this.auto && !this.pause) this.yaw += .006;
    var ey = this.target[1] + Math.sin(this.pitch) * this.dist, hd = Math.cos(this.pitch) * this.dist;
    var eye = [this.target[0] + Math.sin(this.yaw) * hd, ey, this.target[2] + Math.cos(this.yaw) * hd];
    var VP = mul(persp(this.fov, c.width / c.height, .1, 50), look(eye, this.target));
    gl.useProgram(this.pr); gl.activeTexture(gl.TEXTURE0); gl.bindTexture(gl.TEXTURE_2D, this.tex); gl.uniform1i(this.loc.s, 0);
    this.drawMesh(this.shadow, VP, false);
    var base = mul(VP, mul(T(0, 1.5, 0), S(1, -1, 1)));   // model space (y down) -> world (y up), feet on the floor
    if (this.skinMan && this.skinTex) { gl.bindTexture(gl.TEXTURE_2D, this.skinTex); this.drawMesh(this.skinMan, base, true); gl.bindTexture(gl.TEXTURE_2D, this.tex); }
    else this.drawMesh(this.man, base, false);
    var items = this.items, self = this, has = function (id) { return items.indexOf(id) >= 0; };
    items.forEach(function (id) {
      var d = ITEM[id]; if (!d) return; var kind = d[0], sty = d[1], mono = sty === 'mono';
      if (kind === 'wing') {
        var flap = Math.sin(age * .10) * (mono ? 17 : 7), raise = (mono ? -6 : -2) + flap, spread = 40;
        var g = self.get('wing_' + sty, 0);
        [-1, 1].forEach(function (s) {
          var m = mul(base, mul(T(s * .07, .08, .19), mul(RZ(-s * raise * D), mul(RY(-s * spread * D), S(s, -1, 1)))));
          // the geometry may be split into several batches
          for (var b = 0; b < batches('wing_' + sty).length; b++) { var gg = self.get('wing_' + sty, b); self.drawMesh(gg.m, m, gg.tex); }
        });
      } else if (kind === 'cape') {
        var m0 = mul(base, mul(T(0, 0, .125), RX(6 * D))), m = mul(m0, T(0, 0, THK));
        var sh = self.meshes['shell_' + sty] || (self.meshes['shell_' + sty] = { m: self.mesh(quadsToTris(capeShell(sty), 7)) });
        self.drawMesh(sh.m, m0, false);
        if (sty === 'ya' && self.plus && GEO.cape_ya_plus) { self.plusCape(age); self.drawMesh(self.pcBuf, m, false); if (self.pcEm && self.texOk) self.drawMesh(self.pcEm, m, true); }
        else { var key = 'cape_' + sty; for (var b = 0; b < batches(key).length; b++) { var gg = self.get(key, b); if (gg.tex && !self.texOk) continue; if (gg.tex && self.etex[sty]) { if (!self.eok[sty]) continue; gl.bindTexture(gl.TEXTURE_2D, self.etex[sty]); self.drawMesh(gg.m, m, true); gl.bindTexture(gl.TEXTURE_2D, self.tex); continue; } self.drawMesh(gg.m, m, gg.tex); } }
      } else if (kind === 'bandana') {
        var key = 'bandana_' + sty; for (var b = 0; b < batches(key).length; b++) { var gg = self.get(key, b); self.drawMesh(gg.m, base, gg.tex); }
      } else if (kind === 'pet') {
        var bob = Math.sin(age * .10) * .035, sx = Math.sin(age * .05) * .04, sz = Math.cos(age * .04) * .05;
        var rot = mono ? age * 6 * D : Math.sin(age * .05) * 12 * D;
        var m = mul(base, mul(T(-.64 + sx, -.20 + bob, .04 + sz), RY(rot)));
        var key = 'pet_' + sty; for (var b = 0; b < batches(key).length; b++) { var gg = self.get(key, b); self.drawMesh(gg.m, m, gg.tex); }
      }
    });
  };
  Scene.prototype.start = function () {
    var self = this; if (this.running) return; this.running = true;
    (function loop(now) { if (!self.running) return; if (self.canvas.isConnected && (self.canvas.getBoundingClientRect().bottom > 0)) self.frame(now); requestAnimationFrame(loop); })(performance.now());
  };


  // ---- textured player model (the visitor's own Minecraft skin) ----
  function skinModel(slim, legacy) {
    var out = [], aw = slim ? 3 : 4;
    function quad(p, uv, tw, th, k, flipU) {   // p: 4 corners, uv: [u0,v0,u1,v1] in pixels
      var u0 = uv[0] / tw, v0 = uv[1] / th, u1 = uv[2] / tw, v1 = uv[3] / th; if (flipU) { var t = u0; u0 = u1; u1 = t; }
      var c = [[u0, v1], [u1, v1], [u1, v0], [u0, v0]], idx = [0, 1, 2, 0, 2, 3];
      idx.forEach(function (i) { out.push(-p[i][0], p[i][1], p[i][2], k, k, k, 1, c[i][0], c[i][1]); });
    }
    function box(x0, y0, z0, x1, y1, z1, u, v, grow, mirror) {
      var w = x1 - x0, h = y1 - y0, d = z1 - z0, g = grow || 0, tw = 64, th = 64;
      x0 -= g; y0 -= g; z0 -= g; x1 += g; y1 += g; z1 += g;
      var X0 = x0 / 16, X1 = x1 / 16, Y0 = y0 / 16, Y1 = y1 / 16, Z0 = z0 / 16, Z1 = z1 / 16, m = !!mirror;
      // front (-z): texture left->right = +x ; back (+z) mirrored ; sides ; top ; bottom
      quad([[X0, Y1, Z0], [X1, Y1, Z0], [X1, Y0, Z0], [X0, Y0, Z0]], [u + d, v + d, u + d + w, v + d + h], tw, th, .96, m);
      quad([[X1, Y1, Z1], [X0, Y1, Z1], [X0, Y0, Z1], [X1, Y0, Z1]], [u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h], tw, th, .74, m);
      quad([[X0, Y1, Z1], [X0, Y1, Z0], [X0, Y0, Z0], [X0, Y0, Z1]], m ? [u + d + w, v + d, u + 2 * d + w, v + d + h] : [u, v + d, u + d, v + d + h], tw, th, .84, m);
      quad([[X1, Y1, Z0], [X1, Y1, Z1], [X1, Y0, Z1], [X1, Y0, Z0]], m ? [u, v + d, u + d, v + d + h] : [u + d + w, v + d, u + 2 * d + w, v + d + h], tw, th, .84, m);
      quad([[X0, Y0, Z0], [X1, Y0, Z0], [X1, Y0, Z1], [X0, Y0, Z1]], [u + d, v + d, u + d + w, v], tw, th, 1, m);
      quad([[X0, Y1, Z1], [X1, Y1, Z1], [X1, Y1, Z0], [X0, Y1, Z0]], [u + d + w, v, u + d + 2 * w, v + d], tw, th, .6, m);
    }
    // base layer (model y goes down: head on top = negative y, like the exported cosmetics)
    box(-4, -8, -4, 4, 0, 4, 0, 0, 0);
    box(-4, 0, -2, 4, 12, 2, 16, 16, 0);
    box(-4 - aw, 0, -2, -4, 12, 2, 40, 16, 0);
    box(4, 0, -2, 4 + aw, 12, 2, legacy ? 40 : 32, legacy ? 16 : 48, 0, legacy);
    box(-4, 12, -2, 0, 24, 2, 0, 16, 0);
    box(0, 12, -2, 4, 24, 2, legacy ? 0 : 16, legacy ? 16 : 48, 0, legacy);
    // second layer (hat, jacket, sleeves, pants) a hair bigger
    box(-4, -8, -4, 4, 0, 4, 32, 0, .5);
    if (!legacy) {
      box(-4, 0, -2, 4, 12, 2, 16, 32, .25);
      box(-4 - aw, 0, -2, -4, 12, 2, 40, 32, .25);
      box(4, 0, -2, 4 + aw, 12, 2, 48, 48, .25);
      box(-4, 12, -2, 0, 24, 2, 0, 32, .25);
      box(0, 12, -2, 4, 24, 2, 0, 48, .25);
    }
    return new Float32Array(out);
  }
  Scene.prototype.setSkin = function (img) {
    var gl = this.gl; if (!gl) return;
    var tw = img.naturalWidth || img.width, th = img.naturalHeight || img.height, legacy = th <= tw / 2 + 1, slim = false;
    try {
      var c = document.createElement('canvas'); c.width = tw; c.height = th; var x = c.getContext('2d'); x.drawImage(img, 0, 0);
      var k = tw / 64; if (!legacy) slim = x.getImageData(Math.floor(55 * k), Math.floor(20 * k), 1, 1).data[3] === 0;
      if (legacy) { var c2 = document.createElement('canvas'); c2.width = 64; c2.height = 64; var y = c2.getContext('2d'); y.imageSmoothingEnabled = false; y.drawImage(img, 0, 0, tw, th, 0, 0, 64, 64 * th / tw); c = c2; }
      else if (tw !== 64) { var c3 = document.createElement('canvas'); c3.width = 64; c3.height = 64; var z = c3.getContext('2d'); z.imageSmoothingEnabled = false; z.drawImage(img, 0, 0, tw, th, 0, 0, 64, 64); c = c3; }
      var tex = gl.createTexture(); gl.bindTexture(gl.TEXTURE_2D, tex); gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, false);
      gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, c);
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
      gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE); gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
      this.skinTex = tex; this.skinMan = this.mesh(skinModel(slim, legacy)); return true;
    } catch (e) { return false; }
  };
  Scene.prototype.clearSkin = function () { this.skinTex = null; this.skinMan = null; };

  var SKIN_URLS = [function (n) { return 'https://mc-heads.net/skin/' + n; }, function (n) { return 'https://minotar.net/skin/' + n; }, function (n) { return 'https://crafatar.com/skins/' + n; }];
  function loadSkin(name, cb) {
    var i = 0;
    (function next() {
      if (i >= SKIN_URLS.length) return cb(null);
      var im = new Image(); im.crossOrigin = 'anonymous';
      im.onload = function () { cb(im); }; im.onerror = function () { next(); };
      im.src = SKIN_URLS[i++](encodeURIComponent(name));
    })();
  }
  // "Your skin" box: type your Minecraft name and the 3D character wears your skin
  function skinBox(stage, scene) {
    var key = 'ya_skin_name', saved = ''; try { saved = localStorage.getItem(key) || ''; } catch (e) {}
    var box = document.createElement('form'); box.className = 'skinbox'; box.autocomplete = 'off';
    box.innerHTML = '<input type="text" maxlength="16" placeholder="Your Minecraft name" spellcheck="false" aria-label="Minecraft name"><button class="btn" type="submit">Show my skin</button><span class="skmsg"></span>';
    stage.parentNode.insertBefore(box, stage.nextSibling);
    var inp = box.querySelector('input'), msg = box.querySelector('.skmsg'), btn = box.querySelector('button');
    function plusCheck(name, then) {
      try {
        fetch('/v1/plus?name=' + encodeURIComponent(name), { cache: 'no-store' }).then(function (r) { return r.json(); }).then(function (j) {
          var on = j && j.plus === '1'; window.YAplusOn = on; window.YAplusName = on ? name : '';
          document.dispatchEvent(new CustomEvent('yaplus', { detail: { on: on, name: name, known: j && j.known === '1' } })); then(on);
        }).catch(function () { then(false); });
      } catch (e) { then(false); }
    }
    function go(name) {
      name = (name || '').trim();
      if (!/^[A-Za-z0-9_]{3,16}$/.test(name)) { scene.clearSkin(); window.YAplusOn = false; document.dispatchEvent(new CustomEvent('yaplus', { detail: { on: false } })); msg.textContent = name ? 'Enter a valid Minecraft name.' : ''; try { localStorage.removeItem(key); } catch (e) {} return; }
      msg.textContent = 'Loading skin...'; btn.disabled = true;
      loadSkin(name, function (im) {
        btn.disabled = false;
        var okSkin = !!(im && scene.setSkin(im));
        if (okSkin) { try { localStorage.setItem(key, name); } catch (e) {} } else scene.clearSkin();
        plusCheck(name, function (on) { msg.textContent = (okSkin ? 'Wearing the skin of ' + name : 'Skin not found for "' + name + '"') + (on ? ' · YA Plus active: -50% on the shop' : ''); });
      });
    }
    box.addEventListener('submit', function (e) { e.preventDefault(); go(inp.value); });
    if (saved) { inp.value = saved; go(saved); }
  }

  // render one still image (used for the product cards)
  var snapScene = null, snapCanvas = null;
  function snapshot(items, o) {
    o = o || {};
    if (!snapCanvas) { snapCanvas = document.createElement('canvas'); snapCanvas.width = 560; snapCanvas.height = 420; snapCanvas.style.cssText = 'position:fixed;left:-9999px;width:560px;height:420px'; document.body.appendChild(snapCanvas); snapScene = new Scene(snapCanvas, { preserve: true, drag: false, auto: false }); }
    var s = snapScene; if (s.failed) return '';
    s.yaw = o.yaw == null ? .5 : o.yaw; s.pitch = o.pitch == null ? .1 : o.pitch; s.dist = o.dist || 4; s.target = o.target || [0, 1, 0]; s.fov = (o.fov || 30) * D;
    s.set(items, o.plus); s.t0 = performance.now() - (o.time || 1.2) * 1000; s.frame(performance.now());
    return snapCanvas.toDataURL('image/png');
  }

  window.YA3D = {
    ok: function () { try { var c = document.createElement('canvas'); return !!(c.getContext('webgl') || c.getContext('experimental-webgl')); } catch (e) { return false; } },
    viewer: function (canvas, opts) { var s = new Scene(canvas, opts); if (!s.failed) s.start(); return s; },
    snapshot: snapshot,
    skinBox: skinBox,
    whenReady: function (cb) { loadLogo(function () { setTimeout(cb, 30); }); }
  };
})();
