/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'Use strict';

(function(Native) {
    Native["com/sun/midp/lcdui/DisplayDeviceContainer.getDisplayDevicesIds0.()[I"] = function(ctx, stack) {
        var _this = stack.pop(), ids = CLASSES.newPrimitiveArray("I", 1);
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
    }

    Native["com/sun/midp/lcdui/DisplayDevice.setFullScreen0.(IIZ)V"] = function(ctx, stack) {
        var mode = stack.pop(), displayId = stack.pop(), hardwareId = stack.pop(), _this = stack.pop();
    }

    Native["com/sun/midp/lcdui/DisplayDevice.gainedForeground0.(II)V"] = function(ctx, stack) {
        var displayId = stack.pop(), hardwareId = stack.pop(), _this = stack.pop();
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
    }

    var HCENTER = 1;
    var VCENTER = 2;
    var LEFT = 4;
    var RIGHT = 8;
    var TOP = 16;
    var BOTTOM = 32;
    var BASELINE = 64;

    function withClip(g, x, y, cb) {
        var clipX1 = g.class.getField("clipX1", "S").get(g),
            clipY1 = g.class.getField("clipY1", "S").get(g),
            clipX2 = g.class.getField("clipX2", "S").get(g),
            clipY2 = g.class.getField("clipY2", "S").get(g),
            clipped = g.class.getField("clipped", "Z").get(g),
            transX = g.class.getField("transX", "I").get(g),
            transY = g.class.getField("transY", "I").get(g);
        var ctx = MIDP.Context2D;
        if (clipped) {
            ctx.save();
            ctx.beginPath();
            ctx.rect(clipX1, clipY1, clipX2 - clipX1, clipY2 - clipY1);
            ctx.clip();
        }
        if (transX || transY)
            ctx.translate(transX, transY);
        cb(x, y);
        if (clipped) {
            ctx.restore();
        }
    }

    function withAnchor(g, anchor, x, y, w, h, cb) {
        withClip(g, x, y, function(x, y) {
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

    function withFont(font, str, cb) {
        var ctx = MIDP.Context2D;
        ctx.font = font.css;
        cb(ctx.measureText(str).width | 0);
    }

    function withTextAnchor(g, anchor, x, y, str, cb) {
        withClip(g, x, y, function(x, y) {
            withFont(g.class.getField("currentFont", "Ljavax/microedition/lcdui/Font;").get(g), str, function(w) {
                var ctx = MIDP.Context2D;
                ctx.textAlign = "left";
                ctx.textBaseline = "top";
                if (anchor & RIGHT)
                    x -= w;
                if (anchor & HCENTER)
                    x -= (w/2)|0;
                if (anchor & BOTTOM)
                    ctx.textBaseline = "bottom";
                if (anchor & VCENTER)
                    ctx.textBaseline = "middle";
                if (anchor & BASELINE)
                    ctx.textBaseline = "alphabetic";
                cb(x, y, w);
            });
        });
    }

    function withPixel(g, cb) {
        var pixel = g.class.getField("pixel", "I").get(g);
        var style = "#" + ("00000" + pixel.toString(16)).slice(-6);
        MIDP.Context2D.save();
        MIDP.Context2D.fillStyle = style;
        MIDP.Context2D.strokeStyle = style;
        cb();
        MIDP.Context2D.restore();
    }

    function withSize(dx, dy, cb) {
        if (!dx)
            dx = 1;
        if (!dy)
            dy = 1;
        cb(dx, dy);
    }

    Native["javax/microedition/lcdui/Graphics.getPixel.(IIZ)I"] = function(ctx, stack) {
        var isGray = stack.pop(), gray = stack.pop(), rgb = stack.pop(), _this = stack.pop();
        stack.push(rgb);
    }

    Native["javax/microedition/lcdui/Graphics.render.(Ljavax/microedition/lcdui/Image;III)Z"] = function(ctx, stack) {
        var anchor = stack.pop(), y = stack.pop(), x = stack.pop(), image = stack.pop(), _this = stack.pop(),
            img = image.class.getField("imageData", "Ljavax/microedition/lcdui/ImageData;").get(image),
            imgData = img.class.getField("nativeImageData", "I").get(img);
        withAnchor(_this, anchor, x, y, imgData.width, imgData.height, function(x, y) {
            MIDP.Context2D.drawImage(imgData, x, y);
        });
        stack.push(1);
    }

    Native["javax/microedition/lcdui/Graphics.drawString.(Ljava/lang/String;III)V"] = function(ctx, stack) {
        var anchor = stack.pop(), y = stack.pop(), x = stack.pop(), str = util.fromJavaString(stack.pop()), _this = stack.pop();
        withTextAnchor(_this, anchor, x, y, str, function(x, y) {
            withPixel(_this, function() {
                MIDP.Context2D.fillText(str, x, y);
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.drawChars.([CIIIII)V"] = function(ctx, stack) {
        var anchor = stack.pop(), y = stack.pop(), x = stack.pop(),
            len = stack.pop(), offset = stack.pop(), data = stack.pop(), _this = stack.pop(),
            str = util.fromJavaChars(data, offset, len);
        withTextAnchor(_this, anchor, x, y, str, function(x, y) {
            withPixel(_this, function() {
                MIDP.Context2D.fillText(str, x, y);
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.drawChar.(CIII)V"] = function(ctx, stack) {
        var anchor = stack.pop(), y = stack.pop(), x = stack.pop(), chr = String.fromCharCode(stack.pop()), _this = stack.pop();
        withTextAnchor(_this, anchor, x, y, chr, function(x, y) {
            withPixel(_this, function() {
                MIDP.Context2D.fillText(chr, x, y);
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.fillRect.(IIII)V"] = function(ctx, stack) {
        var h = stack.pop(), w = stack.pop(), y = stack.pop(), x = stack.pop(), _this = stack.pop();
        withClip(_this, x, y, function(x, y) {
            withPixel(_this, function() {
                withSize(w, h, function(w, h) {
                    MIDP.Context2D.fillRect(x, y, w, h);
                });
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.drawRect.(IIII)V"] = function(ctx, stack) {
        var h = stack.pop(), w = stack.pop(), y = stack.pop(), x = stack.pop(), _this = stack.pop();
        withClip(_this, x, y, function(x, y) {
            withPixel(_this, function() {
                withSize(w, h, function(w, h) {
                    MIDP.Context2D.strokeRect(x, y, w, h);
                });
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.fillRoundRect.(IIIIII)V"] = function(ctx, stack) {
        var arcHeight = stack.pop(), arcWidth = stack.pop(),
            h = stack.pop(), w = stack.pop(), y = stack.pop(), x = stack.pop(), _this = stack.pop();
        withClip(_this, x, y, function(x, y) {
            withPixel(_this, function() {
                withSize(w, h, function(w, h) {
                    // TODO implement rounding
                    MIDP.Context2D.fillRect(x, y, w, h);
                });
            });
        });
    }

    Native["javax/microedition/lcdui/Graphics.drawArc.(IIIIII)V"] = function(ctx, stack) {
        var arcAngle = stack.pop(), startAngle = stack.pop(),
            height = stack.pop(), width = stack.pop(), y = stack.pop(), x = stack.pop(),
            _this = stack.pop();

        withPixel(_this, function() {
            // TODO need to use bezierCurveTo to implement this properly,
            // but this works as a rough hack for now
            var radius = Math.ceil(Math.max(height, width) / 2);
            var startRad = startAngle * 0.0175;
            var arcRad = arcAngle * 0.0175;
            MIDP.Context2D.moveTo(x + radius, y);
            MIDP.Context2D.arc(x, y, radius, startRad, arcRad);
            MIDP.Context2D.stroke();
        })
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
        var defaultSize = Math.max(12, (MIDP.Context2D.canvas.height / 40) | 0);
        if (size & SIZE_SMALL)
            size = defaultSize / 1.5;
        else if (size & SIZE_LARGE)
            size = defaultSize * 1.5;
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
        var str = util.fromJavaString(stack.pop()), _this = stack.pop();
        withFont(_this, str, function(w) {
            stack.push(w);
        });
    }

    Native["javax/microedition/lcdui/Font.charWidth.(C)I"] = function(ctx, stack) {
        var str = String.fromCharCode(stack.pop()), _this = stack.pop();
        withFont(_this, str, function(w) {
            stack.push(w);
        });
    }

    Native["javax/microedition/lcdui/Font.charsWidth.([CII)I"] = function(ctx, stack) {
        var len = stack.pop(), offset = stack.pop(), str = util.fromJavaChars(stack.pop()), _this = stack.pop();
        withFont(_this, str.slice(offset, offset + len), function(w) {
            stack.push(w);
        });
    }

    Native["javax/microedition/lcdui/Font.substringWidth.(Ljava/lang/String;II)I"] = function(ctx, stack) {
        var len = stack.pop(), offset = stack.pop(), str = util.fromJavaString(stack.pop()), _this = stack.pop();
        withFont(_this, str.slice(offset, offset + len), function(w) {
            stack.push(w);
        });
    }

    Native["com/sun/midp/lcdui/DisplayDevice.refresh0.(IIIIII)V"] = function(ctx, stack) {
        var y2 = stack.pop(), x2 = stack.pop(), y1 = stack.pop(), x1 = stack.pop(),
            displayId = stack.pop(), hardwareId = stack.pop(), _this = stack.pop();
    }

    var TRANS_NONE = 0;
    var TRANS_MIRROR_ROT180 = 1;
    var TRANS_MIRROR = 2;
    var TRANS_MIRROR_ROT90 = 3;
    var TRANS_MIRROR_ROT270 = 4;
    var TRANS_MIRROR_ROT90 = 5;
    var TRANS_MIRROR_ROT270 = 6;
    var TRANS_MIRROR_ROT90 = 7;

    Native["javax/microedition/lcdui/Graphics.renderRegion.(Ljavax/microedition/lcdui/Image;IIIIIIII)Z"] = function(ctx, stack) {
        var anchor = stack.pop(), y = stack.pop(), x = stack.pop(),
            transform = stack.pop(), sh = stack.pop(), sw = stack.pop(), sy = stack.pop(), sx = stack.pop(), image = stack.pop(), _this = stack.pop(),
            img = image.class.getField("imageData", "Ljavax/microedition/lcdui/ImageData;").get(image),
            imgData = img.class.getField("nativeImageData", "I").get(img);
        var w = sw, h = sh;
        if (transform >= 4) {
            w = sh;
            h = sw;
        }
        withAnchor(_this, anchor, x, y, w, h, function(x, y) {
            var ctx = MIDP.Context2D;
            ctx.translate(w/2, h/2);
            if (transform === TRANS_MIRROR || transform === TRANS_MIRROR_ROT180)
                ctx.scale(-1, 1);
            if (transform === TRANS_MIRROR_ROT90 || transform === TRANS_MIRROR_ROT270)
                ctx.scale(1, -1);
            if (transform === TRANS_ROT90 || transform === TRANS_MIRROR_ROT90)
                ctx.rotate(Math.PI / 2);
            if (transform === TRANS_ROT180 || transform === TRANS_MIRROR_ROT180)
                ctx.rotate(Math.PI);
            if (transform === TRANS_ROT270 || transform === TRANS_MIRROR_ROT270)
                ctx.rotate(1.5 * Math.PI);
            MIDP.Context2D.drawImage(imgData, sx, sy, w, h, -w / 2, -h / 2, sw, sh);
        });
        stack.push(1);
    }

    Native["javax/microedition/lcdui/Graphics.drawLine.(IIII)V"] = function(ctx, stack) {
        var y2 = stack.pop(), x2 = stack.pop(), y1 = stack.pop(), x1 = stack.pop(), _this = stack.pop(),
            dx = x2 - x1, dy = y2 - y1;
        withClip(_this, x1, y1, function(x, y) {
            withSize(dx, dy, function(dx, dy) {
                var ctx = MIDP.Context2D;
                ctx.beginPath();
                ctx.moveTo(x, y);
                ctx.lineTo(x + dx, y + dy);
                ctx.stroke();
                ctx.closePath();
            });
        });
    }

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeImage.(Ljavax/microedition/lcdui/ImageData;[BII)V"] = function(ctx, stack) {
        var length = stack.pop(), offset = stack.pop(), bytes = stack.pop(), imageData = stack.pop(), _this = stack.pop();
        var blob = new Blob([bytes.buffer.slice(offset, offset + length)], { type: "image/png" });
        var img = new Image();
        img.src = URL.createObjectURL(blob);
        img.onload = function() {
            imageData.class.getField("width", "I").set(imageData, img.naturalWidth);
            imageData.class.getField("height", "I").set(imageData, img.naturalHeight);
            imageData.class.getField("nativeImageData", "I").set(imageData, img);
            ctx.resume();
        }
        img.onerror = function(e) {
            ctx.resume();
        }
        throw VM.Pause;
    }

    Native["javax/microedition/lcdui/ImageDataFactory.createMutableImageData.(Ljavax/microedition/lcdui/ImageData;II)V"] = function(ctx, stack) {
        var height = stack.pop(), width = stack.pop(), data = stack.pop(), _this = stack.pop();

        var img = new Image();
        // TODO actually implement.
        // Right now this just loads a 1x1 transparent pixel png.
        img.src = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVQYV2NgYAAAAAMAAWgmWQ0AAAAASUVORK5CYII=";

        data.class.getField("width", "I").set(data, width);
        data.class.getField("height", "I").set(data, height);
        data.class.getField("nativeImageData", "I").set(data, img);
    }
})(Native);
