/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'Use strict';

(function(Native) {
    Native["com/sun/midp/lcdui/DisplayDeviceContainer.getDisplayDevicesIds0.()[I"] = function(ctx, stack) {
        var _this = stack.pop(), ids = ctx.newPrimitiveArray("I", 1);
        ids[0] = 1;
        stack.push(ids);
    }

    Native["com/sun/midp/lcdui/DisplayDevice.getDisplayName0.(I)Ljava/lang/String;"] = function(ctx, stack) {
        var id = stack.pop(), _this = stack.pop();
        stack.push(null);
        console.warn("DisplayDevice.getDisplayName0.(I)L...String; not implemented (" + id + ")");
    }

    Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPrimary0.(I)Z"] = function(ctx, stack) {
        var id = stack.pop(), _this = stack.pop();
        stack.push(1);
        console.warn("DisplayDevice.isDisplayPrimary0.(I)Z not implemented (" + id + ")");
    }

    Native["com/sun/midp/lcdui/DisplayDevice.isbuildInDisplay0.(I)Z"] = function(ctx, stack) {
        var id = stack.pop(), _this = stack.pop();
        stack.push(1);
        console.warn("DisplayDevice.isbuildInDisplay0.(I)Z not implemented (" + id + ")");
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

    function textureToRGBA(texture, rgbData, offset, scanlength) {
        var width = texture.width;
        var height = texture.height;
        var data = texture.getContext("2d").getImageData(0, 0, width, height).data;
        var pixels = new Int32Array(data.buffer);
        var i = 0;
        for (var y = 0; y < height; ++y) {
            var j = offset + y * scanlength;
            for (var x = 0; x < width; ++x)
                rgbData[j++] = swapRB(pixels[i++]);
        }
    }

    function textureFromRGBA(texture, rgbData, offset, scanlength) {
        var width = texture.width;
        var height = texture.height;
        var ctx = texture.getContext("2d");
        var imageData = ctx.createImageData(width, height);
        var data = imageData.data;
        var pixels = new Int32Array(data.buffer);
        var i = 0;
        for (var y = 0; y < height; ++y) {
            var j = offset + y * scanlength;
            for (var x = 0; x < width; ++x)
                pixels[i++] = swapRB(rgbData[j++]);
        }
        ctx.putImageData(imageData, 0, 0);
    }

    function textureFromRGB(texture, rgbData, offset, scanlength) {
        var width = texture.width;
        var height = texture.height;
        var ctx = texture.getContext("2d");
        var imageData = ctx.createImageData(width, height);
        var data = imageData.data;
        var pixels = new Int32Array(data.buffer);
        var i = 0;
        for (var y = 0; y < height; ++y) {
            var j = offset + y * scanlength;
            for (var x = 0; x < width; ++x)
                pixels[i++] = swapRB(rgbData[j++]) | 0xff000000;
        }
        ctx.putImageData(imageData, 0, 0);
    }

    function createTexture(width, height) {
        var texture = document.createElement("canvas");
        texture.width = width;
        texture.height = height;
        return texture;
    }

    function setImageData(imageData, width, height, data) {
        imageData.class.getField("width", "I").set(imageData, width);
        imageData.class.getField("height", "I").set(imageData, height);
        imageData.class.getField("nativeImageData", "I").set(imageData, data);
    }

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeImage.(Ljavax/microedition/lcdui/ImageData;[BII)V"] = function(ctx, stack) {
        var length = stack.pop(), offset = stack.pop(), bytes = stack.pop(), imageData = stack.pop(), _this = stack.pop();
        var blob = new Blob([bytes.buffer.slice(offset, offset + length)], { type: "image/png" });
        var img = new Image();
        img.src = URL.createObjectURL(blob);
        img.onload = function() {
            setImageData(imageData, img.naturalWidth, img.naturalHeight, img);
            ctx.resume();
        }
        img.onerror = function(e) {
            ctx.resume();
        }
        throw VM.Pause;
    }

    Native["javax/microedition/lcdui/ImageDataFactory.createMutableImageData.(Ljavax/microedition/lcdui/ImageData;II)V"] = function(ctx, stack) {
        var height = stack.pop(), width = stack.pop(), imageData = stack.pop(), _this = stack.pop();
        var texture = createTexture(width, height);
        var ctx = texture.getContext("2d");
        ctx.fillStyle = "rgb(255,255,255)"; // white
        ctx.fillRect(0, 0, texture.width, texture.height);
        setImageData(imageData, width, height, texture);
    }

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeRGBImage.(Ljavax/microedition/lcdui/ImageData;[IIIZ)V"] = function(ctx, stack) {
        var processAlpha = stack.pop(), height = stack.pop(), width = stack.pop(), rgbData = stack.pop(),
            imageData = stack.pop(), _this = stack.pop();
        var texture = createTexture(width, height);
        (processAlpha ? textureFromRGBA : textureFromRGB)(texture, rgbData, 0, width);
        setImageData(imageData, width, height, texture);

    }

    Native["javax/microedition/lcdui/ImageData.getRGB.([IIIIIII)V"] = function(ctx, stack) {
        var height = stack.pop(), width = stack.pop(), y = stack.pop(), x = stack.pop(), scanlength = stack.pop(), offset = stack.pop(),
            rgbData = stack.pop(), _this = stack.pop();
        var texture = _this.class.getField("nativeImageData", "I").get(_this);
        // If nativeImageData is not a canvas texture already, then convert it now.
        if (!(texture instanceof HTMLCanvasElement)) {
            var canvas = document.createElement("canvas");
            canvas.width = texture.width;
            canvas.height = texture.height;
            var ctx = canvas.getContext("2d");
            ctx.drawImage(texture, 0, 0);
            texture = canvas;
            _this.class.getField("nativeImageData", "I").set(_this, texture);
        }
        textureToRGBA(texture, rgbData, offset, scanlength);
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
            face = "san-serif";
        else
            face = "arial";

        _this.class.getField("baseline", "I").set(_this, (size/2)|0);
        _this.class.getField("height", "I").set(_this, (size * 1.3)|0);
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
        var img = g.class.getField("img", "Ljavax/microedition/lcdui/Image;").get(g),
            c = null;

        if (img === null) {
            c = MIDP.Context2D;
        } else {
            var imgData = img.class.getField("imageData", "Ljavax/microedition/lcdui/ImageData;").get(img),
                canvas = imgData.class.getField("nativeImageData", "I").get(imgData);
            c = canvas.getContext("2d");
            c.globalCompositeOperation = "copy";
        }
        cb(c);
    }

    function withClip(g, c, x, y, cb) {
        var clipX1 = g.class.getField("clipX1", "S").get(g),
            clipY1 = g.class.getField("clipY1", "S").get(g),
            clipX2 = g.class.getField("clipX2", "S").get(g),
            clipY2 = g.class.getField("clipY2", "S").get(g),
            clipped = g.class.getField("clipped", "Z").get(g),
            transX = g.class.getField("transX", "I").get(g),
            transY = g.class.getField("transY", "I").get(g);
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
            withFont(g.class.getField("currentFont", "Ljavax/microedition/lcdui/Font;").get(g), c, str, function(w, c) {
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

    function withPixel(g, c, cb) {
        var pixel = g.class.getField("pixel", "I").get(g);
        c.save();
        var a = (pixel >> 24) & 0xff;
        var b = (pixel >> 16) & 0xff;
        var g = (pixel >> 8) & 0xff;
        var r = pixel & 0xff;
        var style = "rgba(" + r + "," + g + "," + b + "," + (a/255) + ")";
        c.fillStyle = c.strokeStyle = style;
        cb();
        c.restore();
    }

    /**
     * Like withPixel, but ignores alpha channel, setting the alpha value to 1.
     * Useful when you suspect that the caller is specifying the alpha channel
     * incorrectly, although we should actually figure out why that's happening.
     */
    function withOpaquePixel(g, c, cb) {
        var pixel = g.class.getField("pixel", "I").get(g);
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
        var g = _this.class.getField("graphics", "Ljavax/microedition/lcdui/Graphics;").get(_this);
        g.class.getField("pixel", "I").set(g, swapRB(rgba));
    }

    Native["com/nokia/mid/ui/DirectGraphicsImp.getAlphaComponent.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        var g = _this.class.getField("graphics", "Ljavax/microedition/lcdui/Graphics;").get(_this);
        var pixel = g.class.getField("pixel", "I").get(g);
        stack.push((pixel >> 24) & 0xff);
    }

    Native["javax/microedition/lcdui/Graphics.render.(Ljavax/microedition/lcdui/Image;III)Z"] = function(ctx, stack) {
        var anchor = stack.pop(), y = stack.pop(), x = stack.pop(), image = stack.pop(), _this = stack.pop(),
            imgData = image.class.getField("imageData", "Ljavax/microedition/lcdui/ImageData;").get(image),
            texture = imgData.class.getField("nativeImageData", "I").get(imgData);
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

    Native["javax/microedition/lcdui/Graphics.renderRegion.(Ljavax/microedition/lcdui/Image;IIIIIIII)Z"] = function(ctx, stack) {
        var anchor = stack.pop(), y = stack.pop(), x = stack.pop(),
            transform = stack.pop(), sh = stack.pop(), sw = stack.pop(), sy = stack.pop(), sx = stack.pop(), image = stack.pop(), _this = stack.pop(),
            imgData = image.class.getField("imageData", "Ljavax/microedition/lcdui/ImageData;").get(image),
            texture = imgData.class.getField("nativeImageData", "I").get(imgData);
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
        stack.push(1);
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
        var texture = document.createElement("canvas");
        texture.width = width;
        texture.height = height;
        (processAlpha ? textureFromRGBA : textureFromRGB)(texture, rgbData, offset, scanlength);
        withGraphics(_this, function(c) {
            withClip(_this, c, x, y, function(x, y) {
                c.drawImage(texture, x, y);
            });
        });
    }

    var _textEditorId = 0,
        _textEditorContext = null,
        _dirtyEditors = [];

    function wakeTextEditorThread(id) {
        _dirtyEditors.push(id);
        if (_textEditorContext) {
            var ctx = _textEditorContext;
            _textEditorContext = null;
            ctx.resume();
        }
    }

    Native["com/nokia/mid/ui/TextEditor.TextEditor0.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(++_textEditorId);
        _this._textEditorId = _textEditorId;
        _this._textEditor = document.createElement("textarea");
        _this._textEditor.style.border = "none";
        _this._textEditor.style.resize = "none";
        _this._textEditor.style.backgroundColor = "transparent";
        _this._textEditor.style.color = "transparent";
        _this._textEditor.oninput = function(e) {
            wakeTextEditorThread(_this._textEditorId);
        }
    }

    Native["com/nokia/mid/ui/TextEditor.setParent0.(Ljava/lang/Object;)V"] = function(ctx, stack) {
        var parent = stack.pop(), _this = stack.pop();
        document.body.appendChild(_this._textEditor);
    }

    Native["com/nokia/mid/ui/TextEditor.setSize0.(II)V"] = function(ctx, stack) {
        var height = stack.pop(), width = stack.pop(), _this = stack.pop();
        _this._textEditor.style.height = "" + height + "px";
        _this._textEditor.style.width = "" + width + "px";
        // hack
        var top = 0, right = 0;
        if (width === 35) {
            top = 155;
            right = 177;
        } else if (width === 145) {
            top = 155;
            right = 28;
        }
        _this._textEditor.style.position = "absolute";
        _this._textEditor.style.top = "" + top + "px";
        _this._textEditor.style.right = "" + right + "px";
    }

    Native["com/nokia/mid/ui/TextEditor.getContent0.()Ljava/lang/String;"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(ctx.newString(_this._textEditor.value));
    }

    Native["com/nokia/mid/ui/TextEditor.setContent0.(Ljava/lang/String;)V"] = function(ctx, stack) {
        var str = stack.pop(), _this = stack.pop();
        _this._textEditor.value = util.fromJavaString(str);
    }

    Native["com/nokia/mid/ui/TextEditor.insert0.(Ljava/lang/String;I)V"] = function(ctx, stack) {
        var pos = stack.pop(), str = stack.pop(), _this = stack.pop(),
            old = _this._textEditor.value;
        _this._textEditor.value = old.slice(0, pos) + util.fromJavaString(str) + old.slice(pos);
    }

    Native["com/nokia/mid/ui/TextEditor.size0.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        stack.push(_this._textEditor.value.length);
    }

    Native["com/nokia/mid/ui/TextEditorThread.sleep.()V"] = function(ctx, stack) {
        var _this = stack.pop();
        if (!_dirtyEditors.length) {
            _textEditorContext = ctx;
            throw VM.Pause;
        }
    }

    Native["com/nokia/mid/ui/TextEditorThread.getNextDirtyEditor.()I"] = function(ctx, stack) {
        var _this = stack.pop();
        if (!_dirtyEditors.length) {
            console.error("ERROR: getNextDirtyEditor called but no dirty editors");
            stack.push(0);
        } else {
            stack.push(_dirtyEditors.shift());
        }
    }
})(Native);
