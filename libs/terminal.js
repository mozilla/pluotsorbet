var Terminal;
(function (Terminal) {
    function unexpected(message) {
        console.error(message);
    }
    function clamp(value, min, max) {
        return Math.max(min, Math.min(max, value));
    }
    function createProgramFromSource(gl, vertex, fragment) {
        var key = vertex + "-" + fragment;
        var program = createProgram(gl, [
            createShader(gl, gl.VERTEX_SHADER, vertex),
            createShader(gl, gl.FRAGMENT_SHADER, fragment)
        ]);
        queryProgramAttributesAndUniforms(gl, program);
        return program;
    }
    function createProgram(gl, shaders) {
        var program = gl.createProgram();
        shaders.forEach(function (shader) {
            gl.attachShader(program, shader);
        });
        gl.linkProgram(program);
        if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
            var lastError = gl.getProgramInfoLog(program);
            unexpected("Cannot link program: " + lastError);
            gl.deleteProgram(program);
        }
        return program;
    }
    function createShader(gl, shaderType, shaderSource) {
        var shader = gl.createShader(shaderType);
        gl.shaderSource(shader, shaderSource);
        gl.compileShader(shader);
        if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
            var lastError = gl.getShaderInfoLog(shader);
            unexpected("Cannot compile shader: " + lastError);
            gl.deleteShader(shader);
            return null;
        }
        return shader;
    }
    function queryProgramAttributesAndUniforms(gl, program) {
        program.uniforms = {};
        program.attributes = {};
        for (var i = 0, j = gl.getProgramParameter(program, gl.ACTIVE_ATTRIBUTES); i < j; i++) {
            var attribute = gl.getActiveAttrib(program, i);
            program.attributes[attribute.name] = attribute;
            attribute.location = gl.getAttribLocation(program, attribute.name);
        }
        for (var i = 0, j = gl.getProgramParameter(program, gl.ACTIVE_UNIFORMS); i < j; i++) {
            var uniform = gl.getActiveUniform(program, i);
            program.uniforms[uniform.name] = uniform;
            uniform.location = gl.getUniformLocation(program, uniform.name);
        }
    }
    function create2DProjection(width, height, depth) {
        // Note: This matrix flips the Y axis so 0 is at the top.
        return new Float32Array([
            2 / width,
            0,
            0,
            0,
            0,
            -2 / height,
            0,
            0,
            0,
            0,
            2 / depth,
            0,
            -1,
            1,
            0,
            1,
        ]);
    }
    (function (CharacterCode) {
        CharacterCode[CharacterCode["NewLine"] = 10] = "NewLine";
    })(Terminal.CharacterCode || (Terminal.CharacterCode = {}));
    var CharacterCode = Terminal.CharacterCode;
    var Cursor = (function () {
        function Cursor(x, y) {
            this.x = x;
            this.y = y;
            // ...
        }
        return Cursor;
    })();
    Terminal.Cursor = Cursor;
    var Buffer = (function () {
        function Buffer() {
            this.color = 0xFFFF;
            this.clear();
            this.starts = new Uint32Array(32);
            this.buffer = new Uint8Array(1024 * 1024);
            this.colors = new Uint16Array(1024 * 1024);
        }
        Object.defineProperty(Buffer.prototype, "w", {
            /**
             * Number of columns.
             */
            get: function () {
                return Math.max(this.previousMaxLineWidth, this.i - this.starts[this.h]);
            },
            enumerable: true,
            configurable: true
        });
        Buffer.prototype.clear = function () {
            this.h = 0;
            this.i = 0;
            this.version = 0;
            this.previousMaxLineWidth = 0;
        };
        Buffer.prototype.writeCharCode = function (x) {
            if (this.buffer.length === this.i) {
                var buffer = new Uint8Array(this.buffer.length * 2);
                buffer.set(this.buffer, 0);
                this.buffer = buffer;
                var colors = new Uint16Array(this.colors.length * 2);
                colors.set(this.colors, 0);
                this.colors = colors;
            }
            this.colors[this.i] = this.color;
            this.buffer[this.i] = x;
            this.i++;
            this.version++;
        };
        Buffer.prototype.writeString = function (s) {
            for (var i = 0; i < s.length; i++) {
                var c = s.charCodeAt(i);
                if (c === 10 /* NewLine */) {
                    this.writeLine();
                    continue;
                }
                this.writeCharCode(c);
            }
        };
        Buffer.prototype.writeLine = function () {
            if (this.starts.length === this.h + 1) {
                var starts = new Uint32Array(this.starts.length * 2);
                starts.set(this.starts, 0);
                this.starts = starts;
            }
            this.previousMaxLineWidth = Math.max(this.previousMaxLineWidth, this.i - this.starts[this.h]);
            this.starts[++this.h] = this.i;
            this.version++;
        };
        Buffer.prototype.getLine = function (l) {
            l = clamp(l, 0, this.h - 1);
            var s = this.starts[l];
            var e = i === this.h - 1 ? this.i : this.starts[l + 1];
            var c = [];
            for (var i = s; i < e; i++) {
                c.push(String.fromCharCode(this.buffer[i]));
            }
            return c.join("");
        };
        return Buffer;
    })();
    Terminal.Buffer = Buffer;
    var Screen = (function () {
        function Screen(container, fontSize) {
            if (fontSize === void 0) { fontSize = 12; }
            this.container = container;
            this.fontSize = fontSize;
            this.wrap = false;
            this.canvas = document.createElement("canvas");
            container.appendChild(this.canvas);
            var gl = this.gl = this.canvas.getContext("webgl", { alpha: false });
            gl.clearColor(0.0, 0.0, 0.0, 1);
            gl.clear(gl.COLOR_BUFFER_BIT);
            this.cursor = new Cursor(0, 0);
            this.color = 0xFFFF;
            this.initialize();
            this.initializeColorPaletteTexture();
            this.listenForContainerSizeChanges();
            this.enterRenderLoop();
        }
        Screen.prototype.initialize = function () {
            var gl = this.gl;
            this.program = createProgramFromSource(gl, Screen.vertexShader, Screen.fragmentShader);
            gl.useProgram(this.program);
            this.vertexBuffer = gl.createBuffer();
            this.tileTexture = gl.createTexture();
            this.tileMapTexture = gl.createTexture();
            this.colorPaletteTexture = gl.createTexture();
            this.invalidate();
        };
        Screen.prototype.initializeColorPaletteTexture = function () {
            var gl = this.gl;
            gl.bindTexture(gl.TEXTURE_2D, this.colorPaletteTexture);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
            var colorPalette = new Uint8Array(256 * 256 * 4);
            var j = 0;
            for (var i = 0; i < 256 * 256; i++) {
                var r = (i >> 11) & 0x1F;
                var g = (i >> 5) & 0x3F;
                var b = (i >> 0) & 0x1F;
                colorPalette[j++] = (r / 32) * 256;
                colorPalette[j++] = (g / 64) * 256;
                colorPalette[j++] = (b / 32) * 256;
                colorPalette[j++] = 255;
            }
            gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, 256, 256, 0, gl.RGBA, gl.UNSIGNED_BYTE, colorPalette);
        };
        Screen.prototype.listenForContainerSizeChanges = function () {
            var pollInterval = 10;
            var w = this.containerWidth;
            var h = this.containerHeight;
            this.onContainerSizeChanged();
            var self = this;
            setInterval(function () {
                if (w !== self.containerWidth || h !== self.containerHeight) {
                    self.onContainerSizeChanged();
                    w = self.containerWidth;
                    h = self.containerHeight;
                }
            }, pollInterval);
        };
        Screen.prototype.onContainerSizeChanged = function () {
            var cw = this.containerWidth;
            var ch = this.containerHeight;
            var devicePixelRatio = window.devicePixelRatio || 1;
            var backingStoreRatio = 1;
            if (devicePixelRatio !== backingStoreRatio) {
                this.ratio = devicePixelRatio / backingStoreRatio;
                this.canvas.width = cw * this.ratio;
                this.canvas.height = ch * this.ratio;
                this.canvas.style.width = cw + 'px';
                this.canvas.style.height = ch + 'px';
            }
            else {
                this.ratio = 1;
                this.canvas.width = cw;
                this.canvas.height = ch;
            }
            this.resize();
        };
        Object.defineProperty(Screen.prototype, "containerWidth", {
            get: function () {
                return this.container.clientWidth;
            },
            enumerable: true,
            configurable: true
        });
        Object.defineProperty(Screen.prototype, "containerHeight", {
            get: function () {
                return this.container.clientHeight;
            },
            enumerable: true,
            configurable: true
        });
        Screen.prototype.resize = function () {
            this.initializeSpriteSheet();
            var gl = this.gl;
            var program = this.program;
            var screenW = this.w = this.canvas.width / this.tileW | 0;
            var screenH = this.h = this.canvas.height / this.tileH | 0;
            gl.viewport(0, 0, this.canvas.width, this.canvas.height);
            this.screenBuffer = new Uint8Array(screenW * screenH * 4);
            this.screenBufferView = new Uint32Array(this.screenBuffer.buffer);
            gl.bindTexture(gl.TEXTURE_2D, this.tileMapTexture);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
            gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, screenW, screenH, 0, gl.RGBA, gl.UNSIGNED_BYTE, this.screenBuffer);
            var matrix = create2DProjection(this.canvas.width, this.canvas.height, 2000);
            gl.uniformMatrix4fv(this.program.uniforms.uTransformMatrix3D.location, false, matrix);
            var w = this.canvas.width;
            var h = this.canvas.height;
            var f32 = new Float32Array([
                0,
                0,
                0,
                0,
                w,
                0,
                1,
                0,
                w,
                h,
                1,
                1,
                0,
                0,
                0,
                0,
                w,
                h,
                1,
                1,
                0,
                h,
                0,
                1
            ]);
            gl.bindBuffer(gl.ARRAY_BUFFER, this.vertexBuffer);
            gl.bufferData(gl.ARRAY_BUFFER, f32, gl.DYNAMIC_DRAW);
            gl.enableVertexAttribArray(program.attributes.aPosition.location);
            gl.enableVertexAttribArray(program.attributes.aCoordinate.location);
            gl.vertexAttribPointer(program.attributes.aPosition.location, 2, gl.FLOAT, false, 16, 0);
            gl.vertexAttribPointer(program.attributes.aCoordinate.location, 2, gl.FLOAT, false, 16, 8);
            gl.uniform2f(program.uniforms.uTileSize.location, this.tileW / this.spriteCanvas.width, this.tileH / this.spriteCanvas.height);
            gl.uniform2f(program.uniforms.uScaledTileSize.location, 1 / screenW, 1 / screenH);
            this.cursor.x = this.cursor.y = 0;
        };
        Screen.prototype.initializeSpriteSheet = function () {
            var fontSize = this.fontSize * this.ratio;
            this.spriteCanvas = document.createElement("canvas");
            var context = this.spriteCanvas.getContext("2d");
            this.spriteCanvas.width = 1024;
            this.spriteCanvas.height = 256;
            context.fillStyle = "#000000";
            context.fillRect(0, 0, this.spriteCanvas.width, this.spriteCanvas.height);
            context.clearRect(0, 0, this.spriteCanvas.width, this.spriteCanvas.height);
            context.fillStyle = "white";
            context.font = fontSize + 'px Input Mono Condensed, Consolas, Courier, monospace';
            context.textBaseline = "bottom";
            var metrics = context.measureText("A");
            var tileW = this.tileW = Math.ceil(metrics.width);
            var hPadding = 4 * this.ratio;
            var tileH = this.tileH = fontSize + hPadding;
            var tileColumns = this.tileColumns = this.spriteCanvas.width / tileW | 0;
            var j = 0;
            for (var i = 0; i < 256; i++) {
                var x = (j % tileColumns) | 0;
                var y = (j / tileColumns) | 0;
                var c = String.fromCharCode(i);
                context.fillText(c, x * tileW, fontSize + hPadding + y * tileH);
                j++;
            }
            var gl = this.gl;
            gl.bindTexture(gl.TEXTURE_2D, this.tileTexture);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
            gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
            gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, this.spriteCanvas);
            // this.container.appendChild(this.spriteCanvas);
        };
        Screen.prototype.uploadScreenTexture = function () {
            var gl = this.gl;
            gl.bindTexture(gl.TEXTURE_2D, this.tileMapTexture);
            gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, this.w, this.h, 0, gl.RGBA, gl.UNSIGNED_BYTE, this.screenBuffer);
        };
        Screen.prototype.render = function () {
            var gl = this.gl;
            var program = this.program;
            gl.activeTexture(gl.TEXTURE0);
            gl.bindTexture(gl.TEXTURE_2D, this.tileTexture);
            gl.uniform1i(program.uniforms.uTileSampler.location, 0);
            gl.activeTexture(gl.TEXTURE0 + 1);
            gl.bindTexture(gl.TEXTURE_2D, this.tileMapTexture);
            gl.uniform1i(program.uniforms.uTileMapSampler.location, 1);
            if (program.uniforms.uColorPaletteSampler) {
                gl.activeTexture(gl.TEXTURE0 + 2);
                gl.bindTexture(gl.TEXTURE_2D, this.colorPaletteTexture);
                gl.uniform1i(program.uniforms.uColorPaletteSampler.location, 2);
            }
            gl.uniform1f(program.uniforms.uTime.location, performance.now() / 1000);
            gl.clearColor(0x33 / 256, 0x33 / 256, 0x33 / 256, 1.0);
            gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
            gl.disable(gl.DEPTH_TEST);
            gl.blendFunc(gl.ONE, gl.ONE_MINUS_SRC_ALPHA);
            gl.enable(gl.BLEND);
            gl.drawArrays(gl.TRIANGLES, 0, 6);
        };
        Screen.prototype.enterRenderLoop = function () {
            var self = this;
            var gl = this.gl;
            var program = this.program;
            function tick() {
                if (self.dirty) {
                    self.uploadScreenTexture();
                    self.render();
                    self.dirty = false;
                }
                requestAnimationFrame(tick);
            }
            requestAnimationFrame(tick);
        };
        Screen.prototype.move = function (x, y) {
            this.cursor.x = x;
            this.cursor.y = y;
        };
        Screen.prototype.next = function () {
            this.cursor.x++;
            if (this.cursor.x > this.w && this.wrap) {
                this.cursor.x = 0;
                this.cursor.y++;
            }
            if (this.cursor.y > this.h) {
                this.cursor.x = 0;
                this.cursor.y = 0;
                this.scroll(1);
            }
        };
        Screen.prototype.nextLine = function () {
            this.cursor.y++;
            this.cursor.x = 0;
            if (this.cursor.y >= this.h) {
                this.cursor.y--;
                this.scroll(1);
            }
        };
        Screen.prototype.invalidate = function () {
            this.dirty = true;
        };
        Screen.prototype.clear = function () {
            var view = this.screenBufferView;
            for (var i = 0; i < view.length; i++) {
                view[i] = 0;
            }
        };
        Screen.prototype.scroll = function (n) {
            var h = this.h;
            var w = this.w;
            var view = this.screenBufferView;
            for (var y = 0; y < h; y++) {
                if (y >= h - n) {
                    for (var x = 0; x < w; x++) {
                        view[y * w + x] = 0;
                    }
                }
                else {
                    for (var x = 0; x < w; x++) {
                        view[y * w + x] = view[(y + n) * w + x];
                    }
                }
            }
        };
        Screen.prototype.setColor = function (r, g, b) {
            r = clamp(r, 0, 255);
            g = clamp(g, 0, 255);
            b = clamp(b, 0, 255);
            this.color = ((r / 256 * 32) & 0x1F) << 11 | ((g / 256 * 64) & 0x3F) << 5 | ((b / 256 * 32) & 0x1F) << 0;
        };
        Screen.prototype.writeCharCode = function (c) {
            this.invalidate();
            if (c === 10 /* NewLine */) {
                this.nextLine();
                return;
            }
            var cursor = this.cursor;
            var w = this.w;
            var h = this.h;
            if (cursor.x > w || cursor.x < 0 || cursor.y > h || cursor.y < 0) {
                return;
            }
            var buffer = this.screenBuffer;
            var i = (this.w * cursor.y + cursor.x) * 4;
            var x = (c % this.tileColumns) | 0;
            var y = (c / this.tileColumns) | 0;
            buffer[i++] = x;
            buffer[i++] = y;
            buffer[i++] = this.color;
            buffer[i++] = this.color >> 8;
            this.next();
        };
        Screen.prototype.writeText = function (s) {
            for (var i = 0; i < s.length; i++) {
                this.writeCharCode(s.charCodeAt(i));
            }
        };
        Screen.prototype.putChar = function (c, x, y, color) {
            var i = (y * this.w + x) * 4;
            var buffer = this.screenBuffer;
            buffer[i++] = (c % this.tileColumns) | 0;
            buffer[i++] = (c / this.tileColumns) | 0;
            buffer[i++] = color;
            buffer[i++] = color >> 8;
        };
        Screen.prototype.writeBuffer = function (buffer, x, y) {
            var h = this.h, w = this.w;
            x = clamp(x, 0, buffer.w - 1);
            y = clamp(y, 0, buffer.h - 1);
            var r = Math.min(h, buffer.h - y);
            for (var j = 0; j < r; j++) {
                var s = buffer.starts[y + j];
                var e = buffer.starts[y + j + 1];
                var l = e - s - x;
                for (var i = 0; i < l; i++) {
                    var p = s + x + i;
                    var c = buffer.buffer[p];
                    var color = buffer.colors[p];
                    this.putChar(c, i, j, color);
                }
            }
            this.invalidate();
        };
        Screen.vertexShader = "uniform mat4 uTransformMatrix3D;                         " + "attribute vec4 aPosition;                                " + "attribute vec2 aCoordinate;                              " + "varying vec2 vCoordinate;                                " + "varying vec2 vCoordinate2;                               " + "void main() {                                            " + "  gl_Position = uTransformMatrix3D * aPosition;          " + "  vCoordinate = aCoordinate;                             " + "  vCoordinate2 = aCoordinate;                            " + "}";
        Screen.fragmentShader = "precision mediump float;                                 " + "uniform sampler2D uTileSampler;                          " + "uniform sampler2D uTileMapSampler;                       " + "uniform sampler2D uColorPaletteSampler;                  " + "varying vec2 vCoordinate;                                " + "varying vec2 vCoordinate2;                               " + "uniform float uTime;                                     " + "uniform vec2 uTileSize;                                  " + "uniform vec2 uScaledTileSize;                            " + "void main() {                                            " + "  float time = uTime;                                    " + "  vec4 tile = texture2D(uTileMapSampler, vCoordinate);   " + "  if (tile.x == 0.0 && tile.y == 0.0) { discard; }       " + "  vec2 tileOffset = floor(tile.xy * 256.0) * uTileSize;  " + "  vec2 tileCoordinate = tileOffset + mod(vCoordinate, uScaledTileSize) * (uTileSize / uScaledTileSize);" + "  vec4 color = texture2D(uTileSampler, tileCoordinate) * texture2D(uColorPaletteSampler, tile.zw);   " + "  color.rgb *= color.a;" + "  gl_FragColor = color;" + "}";
        return Screen;
    })();
    Terminal.Screen = Screen;
    function getTargetMousePos(event, target) {
        var rect = target.getBoundingClientRect();
        return {
            x: event.clientX - rect.left,
            y: event.clientY - rect.top
        };
    }
    var View = (function () {
        function View(screen, buffer) {
            this.x = 0;
            this.y = 0;
            this.screen = screen;
            this.buffer = buffer;
            this.version = buffer.version;
            this.enterRenderLoop();
            var boundOnMouseWheel = this.onMouseWheel.bind(this);
            screen.canvas.addEventListener(("onwheel" in document ? "wheel" : "mousewheel"), boundOnMouseWheel, false);
        }
        View.prototype.onMouseWheel = function (event) {
            if (!event.altKey && !event.ctrlKey && !event.shiftKey) {
                event.preventDefault();
                var deltaX = event.deltaX;
                var deltaY = event.deltaY;
                if (event.deltaMode === WheelEvent.DOM_DELTA_PIXEL) {
                    deltaX /= 40;
                    deltaY /= 40;
                }
                else if (event.deltaMode === WheelEvent.DOM_DELTA_LINE) {
                    deltaX *= 10;
                    deltaY *= 10;
                }
                var x = clamp(deltaX, -1, 1);
                var y = clamp(deltaY, -1, 1);
                if (event.metaKey) {
                    x *= 10;
                }
                if (event.metaKey) {
                    y *= 100;
                }
                this.scroll(x, y);
            }
        };
        View.prototype.scroll = function (x, y) {
            this.y = clamp(this.y + y, 0, this.buffer.h - this.screen.h);
            this.x = clamp(this.x + x, 0, this.buffer.w - this.screen.w);
            this.version = 0;
        };
        View.prototype.scrollToBottom = function () {
            this.x = 0;
            this.y = clamp(this.buffer.h - this.screen.h, 0, this.buffer.h);
            this.render();
        };
        View.prototype.render = function () {
            this.screen.clear();
            this.screen.writeBuffer(this.buffer, this.x | 0, this.y | 0);
        };
        View.prototype.enterRenderLoop = function () {
            var self = this;
            function tick() {
                if (self.version !== self.buffer.version) {
                    self.render();
                    self.version = self.buffer.version;
                }
                requestAnimationFrame(tick);
            }
            requestAnimationFrame(tick);
        };
        return View;
    })();
    Terminal.View = View;
})(Terminal || (Terminal = {}));
//# sourceMappingURL=terminal.js.map