/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

(function(Native) {
    Native["com/sun/midp/lcdui/DisplayDeviceContainer.getDisplayDevicesIds0.()[I"] = function(ctx, stack) {
        var _this = stack.pop(), ids = ctx.newPrimitiveArray("I", 1);
        ids[0] = 1;
        stack.push(ids);
    }

    Native["com/sun/midp/lcdui/DisplayDevice.getDisplayName0.(I)Ljava/lang/String;"] = function(ctx, stack) {
        var id = stack.pop(), _this = stack.pop();
        stack.push(null);
    }

    Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPrimary0.(I)Z"] = function(ctx, stack) {
        var id = stack.pop(), _this = stack.pop();
        stack.push(1);
        console.warn("DisplayDevice.isDisplayPrimary0.(I)Z not implemented (" + id + ")");
    }

    Native["com/sun/midp/lcdui/DisplayDevice.isbuildInDisplay0.(I)Z"] = function(ctx, stack) {
        var id = stack.pop(), _this = stack.pop();
        stack.push(1);
    }

    Native["com/sun/midp/lcdui/DisplayDevice.getDisplayCapabilities0.(I)I"] = function(ctx, stack) {
        var id = stack.pop(), _this = stack.pop();
        stack.push(0x3ff);
    }

    Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPenSupported0.(I)Z"] = function(ctx, stack) {
        var id = stack.pop(), _this = stack.pop();
        stack.push(1);
    }

    Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPenMotionSupported0.(I)Z"] = function(ctx, stack) {
        var id = stack.pop(), _this = stack.pop();
        stack.push(1);
    }

    Native["com/sun/midp/lcdui/DisplayDevice.getReverseOrientation0.(I)Z"] = function(ctx, stack) {
        var id = stack.pop(), _this = stack.pop();
        stack.push(0);
    }

    Native["com/sun/midp/lcdui/DisplayDevice.getScreenWidth0.(I)I"] = function(ctx, stack) {
        var id = stack.pop(), _this = stack.pop();
        stack.push(MIDP.Context2D.canvas.width);
    }

    Native["com/sun/midp/lcdui/DisplayDevice.getScreenHeight0.(I)I"] = function(ctx, stack) {
        var id = stack.pop(), _this = stack.pop();
        stack.push(MIDP.Context2D.canvas.height);
    }

    Native["com/sun/midp/lcdui/DisplayDevice.displayStateChanged0.(II)V"] = function(ctx, stack) {
        var state = stack.pop(), hardwareId = stack.pop(), _this = stack.pop();
        console.warn("DisplayDevice.displayStateChanged0.(II)V not implemented (" + hardwareId + ", " + state + ")");
    }

    Native["com/sun/midp/lcdui/DisplayDevice.setFullScreen0.(IIZ)V"] = function(ctx, stack) {
        var mode = stack.pop(), displayId = stack.pop(), hardwareId = stack.pop(), _this = stack.pop();
        console.warn("DisplayDevice.setFullScreen0.(IIZ)V not implemented (" +
                     hardwareId + ", " + displayId + ", " + mode + ")");
    }

    Native["com/sun/midp/lcdui/DisplayDevice.gainedForeground0.(II)V"] = function(ctx, stack) {
        var displayId = stack.pop(), hardwareId = stack.pop(), _this = stack.pop();
        console.warn("DisplayDevice.gainedForeground0.(II)V not implemented (" + hardwareId + ", " + displayId + ")");
    }

    Native["com/sun/midp/lcdui/DisplayDeviceAccess.vibrate0.(IZ)Z"] = function(ctx, stack) {
        var on = stack.pop(), displayId = stack.pop(), _this = stack.pop();
        stack.push(1);
    }

    Native["com/sun/midp/lcdui/DisplayDeviceAccess.isBacklightSupported0.(I)Z"] = function(ctx, stack) {
        var displayId = stack.pop(), _this = stack.pop();
        stack.push(1);
    }

    Native["com/sun/midp/lcdui/DisplayDevice.refresh0.(IIIIII)V"] = function(ctx, stack) {
        var y2 = stack.pop(), x2 = stack.pop(), y1 = stack.pop(), x1 = stack.pop(),
            displayId = stack.pop(), hardwareId = stack.pop(), _this = stack.pop();
        console.warn("DisplayDevice.refresh0.(IIIIII)V not implemented (" +
                     hardwareId + ", " + displayId + ", " + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")");
    }

    function swapRB(pixel) {
        return (pixel & 0xff00ff00) | ((pixel >> 16) & 0xff) | ((pixel & 0xff) << 16);
    }

    function swapRBAndSetAlpha(pixel) {
        return swapRB(pixel) | 0xff000000;
    }

    /**
     * Extract the image data from `context` and place it in `rgbData`.
     */
    function contextToRgbData(context, rgbData, offset, scanlength, x, y, width, height, converterFunc) {
        var pixels = new Uint32Array(context.getImageData(x, y, width, height).data.buffer);

        var i = 0;
        for (var y1 = y; y1 < y + height; y1++) {
            for (var x1 = x; x1 < x + width; x1++) {
                rgbData[offset + (x1 - x) + (y1 - y) * scanlength] = converterFunc(pixels[i++]);
            }
        }
    }

    /**
     * Insert `rgbData` into `context`.
     */
    function rgbDataToContext(context, rgbData, offset, scanlength, converterFunc) {
        var width = context.canvas.width;
        var height = context.canvas.height;
        var imageData = context.createImageData(width, height);
        var pixels = new Uint32Array(imageData.data.buffer);

        var i = 0;
        for (var y = 0; y < height; ++y) {
            var j = offset + y * scanlength;

            for (var x = 0; x < width; ++x) {
                pixels[i++] = converterFunc(rgbData[j++]);
            }
        }

        context.putImageData(imageData, 0, 0);
    }

    function createContext2d(width, height) {
        var canvas = document.createElement("canvas");
        canvas.width = width;
        canvas.height = height;
        return canvas.getContext("2d");
    }

    function setImageData(imageData, width, height, data) {
        imageData.class.getField("I.width.I").set(imageData, width);
        imageData.class.getField("I.height.I").set(imageData, height);
        imageData.nativeImageData = data;
    }

    /**
     * Ensure the nativeImageData of the given image points to a
     * Canvas Context2D, converting (and saving) it if necessary.
     *
     * @return {CanvasRenderingContext2D} context
     */
    function convertNativeImageData(imageData) {
        var data = imageData.nativeImageData;

        if (!(data instanceof CanvasRenderingContext2D)) {
            // Assume it's an image.
            var context = createContext2d(data.width, data.height);
            context.drawImage(data, 0, 0);
            imageData.nativeImageData = data = context;
        }

        return data;
    }

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeImage.(Ljavax/microedition/lcdui/ImageData;[BII)V"] = function(ctx, stack) {
        var length = stack.pop(), offset = stack.pop(), bytes = stack.pop(), imageData = stack.pop(), _this = stack.pop();
        var blob = new Blob([bytes.subarray(offset, offset + length)], { type: "image/png" });
        var img = new Image();
        img.src = URL.createObjectURL(blob);
        img.onload = function() {
            setImageData(imageData, img.naturalWidth, img.naturalHeight, img);
            ctx.resume();
        }
        img.onerror = function(e) {
            ctx.raiseException("java/lang/IllegalArgumentException", "error decoding image");
            ctx.resume();
        }
        throw VM.Pause;
    }

    Native["javax/microedition/lcdui/ImageDataFactory.createMutableImageData.(Ljavax/microedition/lcdui/ImageData;II)V"] = function(ctx, stack) {
        var height = stack.pop(), width = stack.pop(), imageData = stack.pop(), _this = stack.pop();
        var context = createContext2d(width, height);
        context.fillStyle = "rgb(255,255,255)"; // white
        context.fillRect(0, 0, width, height);
        setImageData(imageData, width, height, context);
    }

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeRGBImage.(Ljavax/microedition/lcdui/ImageData;[IIIZ)V"] = function(ctx, stack) {
        var processAlpha = stack.pop(), height = stack.pop(), width = stack.pop(), rgbData = stack.pop(),
            imageData = stack.pop(), _this = stack.pop();
        var context = createContext2d(width, height);
        rgbDataToContext(context, rgbData, 0, width, processAlpha ? swapRB : swapRBAndSetAlpha);
        setImageData(imageData, width, height, context);
    }

    Native["javax/microedition/lcdui/ImageData.getRGB.([IIIIIII)V"] = function(ctx, stack) {
        var height = stack.pop(), width = stack.pop(), y = stack.pop(), x = stack.pop(), scanlength = stack.pop(), offset = stack.pop(),
            rgbData = stack.pop(), _this = stack.pop();
        contextToRgbData(convertNativeImageData(_this), rgbData, offset, scanlength, x, y, width, height, swapRB);
    }

    Native["com/nokia/mid/ui/DirectUtils.makeMutable.(Ljavax/microedition/lcdui/Image;)V"] = function(ctx, stack) {
        var image = stack.pop();

        var imageData = image.class.getField("imageData", "Ljavax/microedition/lcdui/ImageData;").get(image);
        imageData.class.getField("I.isMutable.Z").set(imageData, 1);
    }

    Native["com/nokia/mid/ui/DirectUtils.setPixels.(Ljavax/microedition/lcdui/Image;I)V"] = function(ctx, stack) {
        var argb = stack.pop(), image = stack.pop();

        var width = image.class.getField("I.width.I").get(image);
        var height = image.class.getField("I.height.I").get(image);
        var imageData = image.class.getField("I.imageData.Ljavax/microedition/lcdui/ImageData;").get(image);

        var ctx = createContext2d(width, height);
        setImageData(imageData, width, height, ctx);

        var ctxImageData = ctx.createImageData(width, height);
        var pixels = new Uint32Array(ctxImageData.data.buffer);

        var color = swapRB(argb);

        var i = 0;
        for (var y = 0; y < height; ++y) {
            for (var x = 0; x < width; ++x) {
                pixels[i++] = color;
            }
        }

        ctx.putImageData(ctxImageData, 0, 0);
    }

    var FACE_SYSTEM = 0;
    var FACE_MONOSPACE = 32;
    var FACE_PROPORTIONAL = 64;
    var STYLE_PLAIN = 0;
    var STYLE_BOLD = 1;
    var STYLE_ITALIC = 2;
    var STYLE_UNDERLINED = 4;
    var SIZE_SMALL = 8;
    var SIZE_MEDIUM = 0;
    var SIZE_LARGE = 16;

    Native["javax/microedition/lcdui/Font.init.(III)V"] = function(ctx, stack) {
        var size = stack.pop(), style = stack.pop(), face = stack.pop(), _this = stack.pop();
        var defaultSize = Math.max(10, (MIDP.Context2D.canvas.height / 48) | 0);
        if (size & SIZE_SMALL)
            size = defaultSize / 1.25;
        else if (size & SIZE_LARGE)
            size = defaultSize * 1.25;
        else
            size = defaultSize;
        size |= 0;

        if (style & STYLE_BOLD)
            style = "bold";
        else if (style & STYLE_ITALIC)
            style = "italic";
        else
            style = "";

        if (face & FACE_MONOSPACE)
            face = "monospace";
        else if (face & FACE_PROPORTIONAL)
            face = "sans-serif";
        else
            face = "Arial, Helvetica, sans-serif";

        _this.class.getField("I.baseline.I").set(_this, (size/2)|0);
        _this.class.getField("I.height.I").set(_this, (size * 1.3)|0);
        _this.css = style + " " + size + "pt " + face;
    }

    Native["javax/microedition/lcdui/Font.stringWidth.(Ljava/lang/String;)I"] = function(ctx, stack) {
        var str = util.fromJavaString(stack.pop()), _this = stack.pop(),
            c = MIDP.Context2D;
        withFont(_this, c, str, function(w) {
            stack.push(w);
        });
    }

    Native["javax/microedition/lcdui/Font.charWidth.(C)I"] = function(ctx, stack) {
        var str = String.fromCharCode(stack.pop()), _this = stack.pop(),
            c = MIDP.Context2D;

        withFont(_this, c, str, function(w) {
            stack.push(w);
        });
    }

    Native["javax/microedition/lcdui/Font.charsWidth.([CII)I"] = function(ctx, stack) {
        var len = stack.pop(), offset = stack.pop(), str = util.fromJavaChars(stack.pop()), _this = stack.pop(),
            c = MIDP.Context2D;

        withFont(_this, c, str.slice(offset, offset + len), function(w) {
            stack.push(w);
        });
    }

    Native["javax/microedition/lcdui/Font.substringWidth.(Ljava/lang/String;II)I"] = function(ctx, stack) {
        var len = stack.pop(), offset = stack.pop(), str = util.fromJavaString(stack.pop()), _this = stack.pop(),
            c = MIDP.Context2D;
        withFont(_this, c, str.slice(offset, offset + len), function(w) {
            stack.push(w);
        });
    }

    var HCENTER = 1;
    var VCENTER = 2;
    var LEFT = 4;
    var RIGHT = 8;
    var TOP = 16;
    var BOTTOM = 32;
    var BASELINE = 64;

    function withGraphics(g, cb) {
        var img = g.class.getField("I.img.Ljavax/microedition/lcdui/Image;").get(g),
            c = null;

        if (img === null) {
            c = MIDP.Context2D;
        } else {
            var imgData = img.class.getField("I.imageData.Ljavax/microedition/lcdui/ImageData;").get(img),
                c = imgData.nativeImageData;
        }
        cb(c);
    }

    function withClip(g, c, x, y, cb) {
        var clipX1 = g.class.getField("I.clipX1.S").get(g),
            clipY1 = g.class.getField("I.clipY1.S").get(g),
            clipX2 = g.class.getField("I.clipX2.S").get(g),
            clipY2 = g.class.getField("I.clipY2.S").get(g),
            clipped = g.class.getField("I.clipped.Z").get(g),
            transX = g.class.getField("I.transX.I").get(g),
            transY = g.class.getField("I.transY.I").get(g);
        c.save();
        if (clipped) {
            c.beginPath();
            c.rect(clipX1, clipY1, clipX2 - clipX1, clipY2 - clipY1);
            c.clip();
        }
        c.translate(transX, transY);
        cb(x, y);
        c.restore();
    }

    function withAnchor(g, c, anchor, x, y, w, h, cb) {
        withClip(g, c, x, y, function(x, y) {
            if (anchor & RIGHT)
                x -= w;
            if (anchor & HCENTER)
                x -= (w/2)|0;
            if (anchor & BOTTOM)
                y -= h;
            if (anchor & VCENTER)
                y -= (h/2)|0;
            cb(x, y);
        });
    }

    function withFont(font, c, str, cb) {
        c.font = font.css;
        cb(c.measureText(str).width | 0, c);
    }

    function withTextAnchor(g, c, anchor, x, y, str, cb) {
        withClip(g, c, x, y, function(x, y) {
            withFont(g.class.getField("I.currentFont.Ljavax/microedition/lcdui/Font;").get(g), c, str, function(w, c) {
                c.textAlign = "left";
                c.textBaseline = "top";
                if (anchor & RIGHT)
                    x -= w;
                if (anchor & HCENTER)
                    x -= (w/2)|0;
                if (anchor & BOTTOM)
                    c.textBaseline = "bottom";
                if (anchor & VCENTER)
                    c.textBaseline = "middle";
                if (anchor & BASELINE)
                    c.textBaseline = "alphabetic";
                cb(x, y, w);
            });
        });
    }

    function abgrIntToCSS(pixel) {
        var a = (pixel >> 24) & 0xff;
        var b = (pixel >> 16) & 0xff;
        var g = (pixel >> 8) & 0xff;
        var r = pixel & 0xff;
        return "rgba(" + r + "," + g + "," + b + "," + (a/255) + ")";
    };

    function withPixel(g, c, cb) {
        var pixel = g.class.getField("I.pixel.I").get(g);
        c.save();
        c.fillStyle = c.strokeStyle = abgrIntToCSS(pixel);
        cb();
        c.restore();
    }

    /**
     * Like withPixel, but ignores alpha channel, setting the alpha value to 1.
     * Useful when you suspect that the caller is specifying the alpha channel
     * incorrectly, although we should actually figure out why that's happening.
     */
    function withOpaquePixel(g, c, cb) {
        var pixel = g.class.getField("I.pixel.I").get(g);
        c.save();
        var b = (pixel >> 16) & 0xff;
        var g = (pixel >> 8) & 0xff;
        var r = pixel & 0xff;
        var style = "rgba(" + r + "," + g + "," + b + "," + 1 + ")";
        c.fillStyle = c.strokeStyle = style;
        cb();
        c.restore();
    }

    function withSize(dx, dy, cb) {
        if (!dx)
            dx = 1;
        if (!dy)
            dy = 1;
        cb(dx, dy);
    }

    Native["javax/microedition/lcdui/Graphics.getDisplayColor.(I)I"] = function(ctx, stack) {
        var color = stack.pop(), _this = stack.pop();
        stack.push(color);
    }

    Native["javax/microedition/lcdui/Graphics.getPixel.(IIZ)I"] = function(ctx, stack) {
        var isGray = stack.pop(), gray = stack.pop(), rgb = stack.pop(), _this = stack.pop();
        stack.push(swapRB(rgb) | 0xff000000);
    }

    Native["com/nokia/mid/ui/DirectGraphicsImp.setARGBColor.(I)V"] = function(ctx, stack) {
        var rgba = stack.pop(), _this = stack.pop();
        var g = _this.class.getField("I.graphics.Ljavax/microedition/lcdui/Graphics;").get(_this);
        var red = (rgba >> 16) & 0xff;
        var green = (rgba >> 8) & 0xff;
        var blue = rgba & 0xff;
        g.class.getField("I.pixel.I").set(g, swapRB(rgba));
        g.class.getField("I.rgbColor.I").set(g, rgba & 0x00ffffff);
        // Conversion matches Graphics#grayVal(int, int, int).
        g.class.getField("I.gray.I").set(g, (red * 76 + green * 150 + blue * 29) >> 8);
    }

    Native["com/nokia/mid/ui/DirectGraphicsImp.getAlphaComponent.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        var g = _this.class.getField("I.graphics.Ljavax/microedition/lcdui/Graphics;").get(_this);
        var pixel = g.class.getField("I.pixel.I").get(g);
        stack.push((pixel >> 24) & 0xff);
    }

    Native["com/nokia/mid/ui/DirectGraphicsImp.getPixels.([SIIIIIII)V"] = function(ctx, stack) {
        var format = stack.pop(), height = stack.pop(), width = stack.pop(), y = stack.pop(), x = stack.pop(),
            scanlength = stack.pop(), offset = stack.pop(), pixels = stack.pop(), _this = stack.pop();

        if (pixels == null) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException", "Pixels array is null");
        }

        var converterFunc = null;
        if (format == 4444) { // TYPE_USHORT_4444_ARGB
            converterFunc = function(rgba) {
                var r = (rgba & 0xF0000000) >>> 20;
                var g = (rgba & 0x00F00000) >> 16;
                var b = (rgba & 0x0000F000) >> 12;
                var a = (rgba & 0x000000F0) << 8;
                return (a | r | g | b);
            };
        } else if (format == 565) { // TYPE_USHORT_565_RGB
            converterFunc = function(rgba) {
                var r = (rgba & 0b11111000000000000000000000000000) >>> 16;
                var g = (rgba & 0b00000000111111000000000000000000) >>> 13;
                var b = (rgba & 0b00000000000000001111100000000000) >>> 11;
                return (r | g | b);
            };
        } else {
            ctx.raiseExceptionAndYield("java/lang/IllegalArgumentException", "Format unsupported");
        }

        var graphics = _this.class.getField("I.graphics.Ljavax/microedition/lcdui/Graphics;").get(_this);
        var image = graphics.class.getField("I.img.Ljavax/microedition/lcdui/Image;").get(graphics);
        var imageData = image.class.getField("I.imageData.Ljavax/microedition/lcdui/ImageData;").get(image);

        contextToRgbData(convertNativeImageData(imageData), pixels, offset, scanlength, x, y, width, height, converterFunc);
    }

    Native["com/nokia/mid/ui/DirectGraphicsImp.drawPixels.([SZIIIIIIII)V"] = function(ctx, stack) {
        var format = stack.pop(), manipulation = stack.pop(), height = stack.pop(), width = stack.pop(), y = stack.pop(),
            x = stack.pop(), scanlength = stack.pop(), offset = stack.pop(), transparency = stack.pop(),
            pixels = stack.pop(), _this = stack.pop();

        if (pixels == null) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException", "Pixels array is null");
        }

        var converterFunc = null;
        if (format == 4444 && transparency && !manipulation) {
            converterFunc = function(argb) {
                var a = (argb & 0xF000) >>> 8;
                var r = (argb & 0x0F00) << 20;
                var g = (argb & 0x00F0) << 16;
                var b = (argb & 0x000F) << 12;
                return (r | g | b | a);
            };
        } else {
            ctx.raiseExceptionAndYield("java/lang/IllegalArgumentException", "Format unsupported");
        }

        var graphics = _this.class.getField("I.graphics.Ljavax/microedition/lcdui/Graphics;").get(_this);

        var context = createContext2d(width, height);
        rgbDataToContext(context, pixels, offset, scanlength, converterFunc);
        withGraphics(graphics, function(c) {
            withClip(graphics, c, x, y, function(x, y) {
                c.drawImage(context.canvas, x, y);
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.render.(Ljavax/microedition/lcdui/Image;III)Z"] = function(ctx, stack) {
        var anchor = stack.pop(), y = stack.pop(), x = stack.pop(), image = stack.pop(), _this = stack.pop(),
            imgData = image.class.getField("I.imageData.Ljavax/microedition/lcdui/ImageData;").get(image),
            texture = imgData.nativeImageData;

        if (!texture) {
            console.warn("Graphics.render: image missing native data");
            stack.push(1);
            return;
        }

        if (texture instanceof CanvasRenderingContext2D) {
            texture = texture.canvas; // Render the canvas, not the context.
        }

        withGraphics(_this, function(c) {
            withAnchor(_this, c, anchor, x, y, texture.width, texture.height, function(x, y) {
                c.drawImage(texture, x, y);
            });
        });
        stack.push(1);
    }

    Native["javax/microedition/lcdui/Graphics.drawString.(Ljava/lang/String;III)V"] = function(ctx, stack) {
        var anchor = stack.pop(), y = stack.pop(), x = stack.pop(), str = util.fromJavaString(stack.pop()), _this = stack.pop();
        withGraphics(_this, function(c) {
            withTextAnchor(_this, c, anchor, x, y, str, function(x, y) {
                withOpaquePixel(_this, c, function() {
                    c.fillText(str, x, y);
                });
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.drawChars.([CIIIII)V"] = function(ctx, stack) {
        var anchor = stack.pop(), y = stack.pop(), x = stack.pop(),
            len = stack.pop(), offset = stack.pop(), data = stack.pop(), _this = stack.pop(),
            str = util.fromJavaChars(data, offset, len);
        withGraphics(_this, function(c) {
            withTextAnchor(_this, c, anchor, x, y, str, function(x, y) {
                withPixel(_this, c, function() {
                    c.fillText(str, x, y);
                });
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.drawChar.(CIII)V"] = function(ctx, stack) {
        var anchor = stack.pop(), y = stack.pop(), x = stack.pop(), chr = String.fromCharCode(stack.pop()), _this = stack.pop();
        withGraphics(_this, function(c) {
            withTextAnchor(_this, c, anchor, x, y, chr, function(x, y) {
                withPixel(_this, c, function() {
                    c.fillText(chr, x, y);
                });
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.fillTriangle.(IIIIII)V"] = function(ctx, stack) {
        var y3 = stack.pop(), x3 = stack.pop(), y2 = stack.pop(), x2 = stack.pop(), y1 = stack.pop(), x1 = stack.pop(), _this = stack.pop();
        withGraphics(_this, function(c) {
            withClip(_this, c, x1, y1, function(x, y) {
                withPixel(_this, c, function() {
                    withSize(x2 - x1, y2 - y1, function(dx1, dy1) {
                        withSize(x3 - x1, y3 - y1, function(dx2, dy2) {
                            c.beginPath();
                            c.moveTo(x, y);
                            c.lineTo(x + dx1, y + dy1);
                            c.lineTo(x + dx2, y + dy2);
                            c.closePath();
                            c.fill();
                        });
                    });
                });
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.drawRect.(IIII)V"] = function(ctx, stack) {
        var h = stack.pop(), w = stack.pop(), y = stack.pop(), x = stack.pop(), _this = stack.pop();
        withGraphics(_this, function(c) {
            withClip(_this, c, x, y, function(x, y) {
                withPixel(_this, c, function() {
                    withSize(w, h, function(w, h) {
                        c.strokeRect(x, y, w, h);
                    });
                });
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.fillRect.(IIII)V"] = function(ctx, stack) {
        var h = stack.pop(), w = stack.pop(), y = stack.pop(), x = stack.pop(), _this = stack.pop();
        withGraphics(_this, function(c) {
            withClip(_this, c, x, y, function(x, y) {
                withPixel(_this, c, function() {
                    withSize(w, h, function(w, h) {
                        c.fillRect(x, y, w, h);
                    });
                });
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.fillRoundRect.(IIIIII)V"] = function(ctx, stack) {
        var arcHeight = stack.pop(), arcWidth = stack.pop(),
            h = stack.pop(), w = stack.pop(), y = stack.pop(), x = stack.pop(), _this = stack.pop();
        withGraphics(_this, function(c) {
            withClip(_this, c, x, y, function(x, y) {
                withPixel(_this, c, function() {
                    withSize(w, h, function(w, h) {
                        // TODO implement rounding
                        c.fillRect(x, y, w, h);
                    });
                });
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.drawArc.(IIIIII)V"] = function(ctx, stack) {
        var arcAngle = stack.pop(), startAngle = stack.pop(),
            height = stack.pop(), width = stack.pop(), y = stack.pop(), x = stack.pop(),
            _this = stack.pop();

        withGraphics(_this, function(c) {
            withPixel(_this, c, function() {
                // TODO need to use bezierCurveTo to implement this properly,
                // but this works as a rough hack for now
                var radius = Math.ceil(Math.max(height, width) / 2);
                var startRad = startAngle * 0.0175;
                var arcRad = arcAngle * 0.0175;
                c.beginPath();
                c.moveTo(x + radius, y);
                c.arc(x, y, radius, startRad, arcRad);
                c.stroke();
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.fillArc.(IIIIII)V"] = function(ctx, stack) {
        var arcAngle = stack.pop(), startAngle = stack.pop(),
            height = stack.pop(), width = stack.pop(), y = stack.pop(), x = stack.pop(),
            _this = stack.pop();
        withGraphics(_this, function(c) {
            withPixel(_this, c, function() {
                // TODO need to use bezierCurveTo to implement this properly,
                // but this works as a rough hack for now
                var radius = Math.ceil(Math.max(height, width) / 2);
                var startRad = startAngle * 0.0175;
                var arcRad = arcAngle * 0.0175;
                c.beginPath();
                c.moveTo(x + radius, y);
                c.arc(x, y, radius, startRad, arcRad);
                c.fill();
            });
        });
    }

    var TRANS_NONE = 0;
    var TRANS_MIRROR_ROT180 = 1;
    var TRANS_MIRROR = 2;
    var TRANS_ROT180 = 3;
    var TRANS_MIRROR_ROT270 = 4;
    var TRANS_ROT90 = 5;
    var TRANS_ROT270 = 6;
    var TRANS_MIRROR_ROT90 = 7;

    Override["javax/microedition/lcdui/Graphics.drawRegion.(Ljavax/microedition/lcdui/Image;IIIIIIII)V"] = function(ctx, stack) {
        var anchor = stack.pop(), y = stack.pop(), x = stack.pop(),
            transform = stack.pop(), sh = stack.pop(), sw = stack.pop(), sy = stack.pop(), sx = stack.pop(), image = stack.pop(), _this = stack.pop();

        if (!image) {
            ctx.raiseExceptionAndYield("java/lang/NullPointerException", "src image is null");
        }

        var imgData = image.class.getField("I.imageData.Ljavax/microedition/lcdui/ImageData;").get(image),
            texture = imgData.nativeImageData;

        if (texture instanceof CanvasRenderingContext2D) {
            texture = texture.canvas; // Render the canvas, not the context.
        }

        var w = sw, h = sh;
        withGraphics(_this, function(c) {
            withAnchor(_this, c, anchor, x, y, w, h, function(x, y) {
                c.translate(x, y);
                if (transform === TRANS_MIRROR || transform === TRANS_MIRROR_ROT180)
                    c.scale(-1, 1);
                if (transform === TRANS_MIRROR_ROT90 || transform === TRANS_MIRROR_ROT270)
                    c.scale(1, -1);
                if (transform === TRANS_ROT90 || transform === TRANS_MIRROR_ROT90)
                    c.rotate(Math.PI / 2);
                if (transform === TRANS_ROT180 || transform === TRANS_MIRROR_ROT180)
                    c.rotate(Math.PI);
                if (transform === TRANS_ROT270 || transform === TRANS_MIRROR_ROT270)
                    c.rotate(1.5 * Math.PI);
                c.drawImage(texture, sx, sy, w, h, 0, 0, sw, sh);
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.drawLine.(IIII)V"] = function(ctx, stack) {
        var y2 = stack.pop(), x2 = stack.pop(), y1 = stack.pop(), x1 = stack.pop(), _this = stack.pop(),
            dx = x2 - x1, dy = y2 - y1;
        withGraphics(_this, function(c) {
            withClip(_this, c, x1, y1, function(x, y) {
                withSize(dx, dy, function(dx, dy) {
                    withPixel(_this, c, function() {
                        var ctx = MIDP.Context2D;
                        ctx.beginPath();
                        ctx.moveTo(x, y);
                        ctx.lineTo(x + dx, y + dy);
                        ctx.stroke();
                        ctx.closePath();
                    });
                });
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.drawRGB.([IIIIIIIZ)V"] = function(ctx, stack) {
        var processAlpha = stack.pop(), height = stack.pop(), width = stack.pop(), y = stack.pop(), x = stack.pop(),
            scanlength = stack.pop(), offset = stack.pop(), rgbData = stack.pop(), _this = stack.pop();
        var context = createContext2d(width, height);
        rgbDataToContext(context, rgbData, offset, scanlength, processAlpha ? swapRB : swapRBAndSetAlpha);
        withGraphics(_this, function(c) {
            withClip(_this, c, x, y, function(x, y) {
                c.drawImage(context.canvas, x, y);
            });
        });
    }

    var textEditorId = 0,
        textEditorContext = null,
        dirtyEditors = [];

    function wakeTextEditorThread(id) {
        dirtyEditors.push(id);
        if (textEditorContext) {
            var ctx = textEditorContext;
            textEditorContext = null;
            ctx.resume();
        }
    }

    Native["com/nokia/mid/ui/TextEditor.init.(Ljava/lang/String;IIII)I"] = function(ctx, stack) {
        var height = stack.pop(), width = stack.pop(), constraints = stack.pop(), maxSize = stack.pop(),
            text = stack.pop(), _this = stack.pop();

        if (constraints != 0) {
            console.warn("TextEditor.constraints not implemented");
        }

        stack.push(++textEditorId);
        _this.textEditorId = textEditorId;
        _this.textEditor = document.createElement("textarea");
        _this.visible = false;
        _this.focused = false;
        _this.backgroundColor = 0xFFFFFFFF | 0; // opaque white
        _this.foregroundColor = 0xFF000000 | 0; // opaque black
        _this.textEditor.style.border = "none";
        _this.textEditor.style.resize = "none";
        _this.textEditor.style.backgroundColor = abgrIntToCSS(_this.backgroundColor);
        _this.textEditor.style.color = abgrIntToCSS(_this.foregroundColor);
        _this.textEditor.value = util.fromJavaString(text);
        _this.textEditor.setAttribute("maxlength", maxSize);
        _this.textEditor.style.width = width + "px";
        _this.textEditor.style.height = height + "px";
        _this.textEditor.style.position = "absolute";
        _this.textEditor.oninput = function(e) {
            wakeTextEditorThread(_this.textEditorId);
        }
    }

    Native["com/nokia/mid/ui/CanvasItem.setSize.(II)V"] = function(ctx, stack) {
        var height = stack.pop(), width = stack.pop(), _this = stack.pop();
        _this.textEditor.style.width = width + "px";
        _this.textEditor.style.height = height + "px";
    }

    Native["com/nokia/mid/ui/CanvasItem.setVisible.(Z)V"] = function(ctx, stack) {
        var visible = stack.pop(), _this = stack.pop();
        if (visible) {
            document.getElementById("display").appendChild(_this.textEditor);
        } else if (_this.visible) {
            document.getElementById("display").removeChild(_this.textEditor);
        }
        _this.visible = visible;
    }

    Native["com/nokia/mid/ui/CanvasItem.getWidth.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(parseInt(_this.textEditor.style.width));
    }

    Native["com/nokia/mid/ui/CanvasItem.getHeight.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(parseInt(_this.textEditor.style.height));
    }

    Native["com/nokia/mid/ui/CanvasItem.setPosition.(II)V"] = function(ctx, stack) {
        var y = stack.pop(), x = stack.pop(), _this = stack.pop();
        _this.textEditor.style.left = x + "px";
        _this.textEditor.style.top = y + "px";
    }

    Native["com/nokia/mid/ui/CanvasItem.getPositionX.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(parseInt(_this.textEditor.style.left));
    }

    Native["com/nokia/mid/ui/CanvasItem.getPositionY.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(parseInt(_this.textEditor.style.top));
    }

    Native["com/nokia/mid/ui/CanvasItem.isVisible.()Z"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(_this.visible ? 1 : 0);
    }

    Native["com/nokia/mid/ui/TextEditor.setFocus.(Z)V"] = function(ctx, stack) {
        var focused = stack.pop(), _this = stack.pop();
        if (focused) {
            _this.textEditor.focus();
        } else {
            _this.textEditor.blur();
        }
        _this.focused = focused;
    }

    Native["com/nokia/mid/ui/TextEditor.hasFocus.()Z"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(_this.focused);
    }

    Native["com/nokia/mid/ui/TextEditor.setCaret.(I)V"] = function(ctx, stack) {
        var index = stack.pop(), _this = stack.pop();

        if (!_this.visible) {
            console.warn("setCaret ignored when TextEditor is invisible");
            return;
        }

        _this.textEditor.setSelectionRange(index, index);
    }

    Native["com/nokia/mid/ui/TextEditor.getCaretPosition.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(_this.textEditor.selectionStart);
    }

    Native["com/nokia/mid/ui/TextEditor.getBackgroundColor.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(_this.backgroundColor);
    }
    Native["com/nokia/mid/ui/TextEditor.getForegroundColor.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(_this.foregroundColor);
    }
    Native["com/nokia/mid/ui/TextEditor.setBackgroundColor.(I)V"] = function(ctx, stack) {
        var backgroundColor = stack.pop(), _this = stack.pop();
        _this.backgroundColor = backgroundColor;
        _this.textEditor.style.backgroundColor = abgrIntToCSS(backgroundColor);
    }
    Native["com/nokia/mid/ui/TextEditor.setForegroundColor.(I)V"] = function(ctx, stack) {
        var foregroundColor = stack.pop(), _this = stack.pop();
        _this.foregroundColor = foregroundColor;
        _this.textEditor.style.color = abgrIntToCSS(foregroundColor);
    }

    Native["com/nokia/mid/ui/TextEditor.getContent.()Ljava/lang/String;"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(ctx.newString(_this.textEditor.value));
    }

    Native["com/nokia/mid/ui/TextEditor.setContent.(Ljava/lang/String;)V"] = function(ctx, stack) {
        var str = stack.pop(), _this = stack.pop();
        _this.textEditor.value = util.fromJavaString(str);
    }

    Native["com/nokia/mid/ui/TextEditor.insert.(Ljava/lang/String;I)V"] = function(ctx, stack) {
        var pos = stack.pop(), str = stack.pop(), _this = stack.pop(),
            old = _this.textEditor.value;
        _this.textEditor.value = old.slice(0, pos) + util.fromJavaString(str) + old.slice(pos);
    }

    Native["com/nokia/mid/ui/TextEditor.delete.(II)V"] = function(ctx, stack) {
        var length = stack.pop(), offset = stack.pop(), _this = stack.pop(),
            old = _this.textEditor.value;

        if (offset < 0 || offset > old.length || length < 0 || offset + length > old.length) {
            ctx.raiseExceptionAndYield("java.lang.StringIndexOutOfBoundsException", "offset/length invalid");
        }

        _this.textEditor.value = old.slice(0, offset) + old.slice(offset + length);
    }

    Native["com/nokia/mid/ui/TextEditor.getMaxSize.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(parseInt(_this.textEditor.getAttribute("maxlength")));
    }

    Native["com/nokia/mid/ui/TextEditor.setMaxSize.(I)I"] = function(ctx, stack) {
        var maxSize = stack.pop(), _this = stack.pop();
        _this.textEditor.setAttribute("maxlength", maxSize);
        _this.textEditor.value = _this.textEditor.value.substring(0, maxSize);

        // The return value is the assigned size, which could be less than
        // the size that was requested, although in this case we always set it
        // to the requested size.
        stack.push(maxSize);
    }

    Native["com/nokia/mid/ui/TextEditor.size.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(_this.textEditor.value.length);
    }

    Native["com/nokia/mid/ui/TextEditorThread.sleep.()V"] = function(ctx, stack) {
        var _this = stack.pop();
        if (!dirtyEditors.length) {
            textEditorContext = ctx;
            throw VM.Pause;
        }
    }

    Native["com/nokia/mid/ui/TextEditorThread.getNextDirtyEditor.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        if (!dirtyEditors.length) {
            console.error("ERROR: getNextDirtyEditor called but no dirty editors");
            stack.push(0);
        } else {
            stack.push(dirtyEditors.shift());
        }
    }
})(Native);
