/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var currentlyFocusedTextEditor;
(function(Native) {
    var screenContextInfo = new ContextInfo(MIDP.context2D);
    MIDP.context2D.canvas.addEventListener("canvasresize", function() {
        screenContextInfo.currentlyAppliedGraphicsInfo = null;
        MIDP.context2D.save();
    });

    var tempContext = document.createElement("canvas").getContext("2d");
    tempContext.canvas.width = 0;
    tempContext.canvas.height = 0;

    var NativeDisplay = function() {
        this.fullScreen = 1;
    };

    var NativeDisplays = {
        get: function(id) {
            var d = this._map.get(id);
            if (!d) {
                d = new NativeDisplay();
                this._map.set(id, d);
            }
            return d;
        },

        _map: new Map(),

        foreground: -1
    };

    Native["com/sun/midp/lcdui/DisplayDeviceContainer.getDisplayDevicesIds0.()[I"] = function() {
        var ids = J2ME.newIntArray( 1);
        ids[0] = 1;
        return ids;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getDisplayName0.(I)Ljava/lang/String;"] = function(id) {
        return null;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPrimary0.(I)Z"] = function(id) {
        console.warn("DisplayDevice.isDisplayPrimary0.(I)Z not implemented (" + id + ")");
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.isbuildInDisplay0.(I)Z"] = function(id) {
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getDisplayCapabilities0.(I)I"] = function(id) {
        return 0x3ff;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPenSupported0.(I)Z"] = function(id) {
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPenMotionSupported0.(I)Z"] = function(id) {
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.reverseOrientation0.(I)Z"] = function(id) {
        return 0;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getReverseOrientation0.(I)Z"] = function(id) {
        return 0;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getScreenWidth0.(I)I"] = function(id) {
        return MIDP.context2D.canvas.width;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getScreenHeight0.(I)I"] = function(id) {
        return MIDP.context2D.canvas.height;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.displayStateChanged0.(II)V"] = function(hardwareId, state) {
        console.warn("DisplayDevice.displayStateChanged0.(II)V not implemented (" + hardwareId + ", " + state + ")");
    };

    Native["com/sun/midp/lcdui/DisplayDevice.setFullScreen0.(IIZ)V"] = function(hardwareId, displayId, mode) {
        var d = NativeDisplays.get(displayId);
        d.fullScreen = mode;
        if (NativeDisplays.foreground === displayId) {
            MIDP.setFullScreen(mode);
        }
    };

    Native["com/sun/midp/lcdui/DisplayDevice.gainedForeground0.(II)V"] = function(hardwareId, displayId) {
        hideBackgroundScreen();
        hideSplashScreen();
        var d = NativeDisplays.get(displayId);
        NativeDisplays.foreground = displayId;
        MIDP.setFullScreen(d.fullScreen);

        asyncImpl("V", emoji.loadData());

        if (profile === 2) {
          // Use setTimeout to make sure our profiling enter/leave stack is not unpaired.
          setTimeout(function () {
            stopAndSaveTimeline();
          }, 0);
        }
    };

    Native["com/sun/midp/lcdui/DisplayDeviceAccess.vibrate0.(IZ)Z"] = function(displayId, on) {
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDeviceAccess.isBacklightSupported0.(I)Z"] = function(displayId) {
        return 1;
    };

    var hasNewFrame = true;
    var ctxs = [];
    function gotNewFrame(timestamp) {
        if (ctxs.length > 0) {
            var ctx = ctxs.pop();
            window.requestAnimationFrame(gotNewFrame);
            ctx.execute();
        } else {
            hasNewFrame = true;
        }
    }

    Native["com/sun/midp/lcdui/RepaintEventProducer.waitForAnimationFrame.()V"] = function() {
        if (hasNewFrame) {
            hasNewFrame = false;
            window.requestAnimationFrame(gotNewFrame);
        } else {
            ctxs.unshift($.ctx);
            $.pause(asyncImplStringAsync);
        }
    }

    Native["com/sun/midp/lcdui/DisplayDevice.refresh0.(IIIIII)V"] = function() {
        // This is a no-op: The foreground display gets drawn directly
        // to the screen.
    };

    function swapRB(pixel) {
        return (pixel & 0xff00ff00) | ((pixel >> 16) & 0xff) | ((pixel & 0xff) << 16);
    }

    function ABGRToARGB(abgrData, argbData, width, height, offset, scanlength) {
        var i = 0;
        for (var y = 0; y < height; y++) {
            var j = offset + y * scanlength;

            for (var x = 0; x < width; x++) {
                argbData[j++] = swapRB(abgrData[i++]);
            }
        }
    }

    function ABGRToARGB4444(abgrData, argbData, width, height, offset, scanlength) {
        var i = 0;
        for (var y = 0; y < height; y++) {
            var j = offset + y * scanlength;

            for (var x = 0; x < width; x++) {
                var abgr = abgrData[i++];
                argbData[j++] = (abgr & 0xF0000000) >>> 16 |
                                (abgr & 0x000000F0) << 4 |
                                (abgr & 0x0000F000) >> 8 |
                                (abgr & 0x00F00000) >>> 20;
            }
        }
    }

    var ABGRToRGB565_R_MASK = parseInt("000000000000000011111000", 2);
    var ABGRToRGB565_G_MASK = parseInt("000000001111110000000000", 2);
    var ABGRToRGB565_B_MASK = parseInt("111110000000000000000000", 2);

    function ABGRToRGB565(abgrData, rgbData, width, height, offset, scanlength) {
        var i = 0;
        for (var y = 0; y < height; y++) {
            var j = offset + y * scanlength;

            for (var x = 0; x < width; x++) {
                var abgr = abgrData[i++];
                rgbData[j++] = (abgr & ABGRToRGB565_R_MASK) << 8 |
                               (abgr & ABGRToRGB565_G_MASK) >>> 5 |
                               (abgr & ABGRToRGB565_B_MASK) >>> 19;
            }
        }
    }

    function ARGBToABGR(argbData, abgrData, width, height, offset, scanlength) {
        var i = 0;
        for (var y = 0; y < height; ++y) {
            var j = offset + y * scanlength;

            for (var x = 0; x < width; ++x) {
                abgrData[i++] = swapRB(argbData[j++]);
            }
        }
    }

    function ARGBTo1BGR(argbData, abgrData, width, height, offset, scanlength) {
        var i = 0;
        for (var y = 0; y < height; ++y) {
            var j = offset + y * scanlength;

            for (var x = 0; x < width; ++x) {
                abgrData[i++] = swapRB(argbData[j++]) | 0xFF000000;
            }
        }
    }

    function ARGB4444ToABGR(argbData, abgrData, width, height, offset, scanlength) {
        var i = 0;
        for (var y = 0; y < height; ++y) {
            var j = offset + y * scanlength;

            for (var x = 0; x < width; ++x) {
                var argb = argbData[j++];
                abgrData[i++] = (argb & 0xF000) << 16 |
                                (argb & 0x0F00) >>> 4 |
                                (argb & 0x00F0) << 8 |
                                (argb & 0x000F) << 20;
            }
        }
    }

    function initImageData(imageData, width, height, isMutable) {
        var canvas = document.createElement("canvas");
        canvas.width = width;
        canvas.height = height;

        imageData.contextInfo = new ContextInfo(canvas.getContext("2d"));

        imageData.width = width;
        imageData.height = height;

        imageData.isMutable = isMutable;

        return imageData.contextInfo.context;
    }

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeImage.(Ljavax/microedition/lcdui/ImageData;[BII)V"] =
    function(imageData, bytes, offset, length) {
        var ctx = $.ctx;
        asyncImpl("V", new Promise(function(resolve, reject) {
            var blob = new Blob([bytes.subarray(offset, offset + length)], { type: "image/png" });
            var img = new Image();
            img.src = URL.createObjectURL(blob);
            img.onload = function() {
                var context = initImageData(imageData, img.naturalWidth, img.naturalHeight, 0);
                context.drawImage(img, 0, 0);

                URL.revokeObjectURL(img.src);
                resolve();
            }
            img.onerror = function(e) {
               URL.revokeObjectURL(img.src);
               ctx.setAsCurrentContext();
               reject($.newIllegalArgumentException("error decoding image"));
            }
        }));
    };

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDataRegion.(Ljavax/microedition/lcdui/ImageData;Ljavax/microedition/lcdui/ImageData;IIIIIZ)V"] =
    function(dataDest, dataSource, x, y, width, height, transform, isMutable) {
        var context = initImageData(dataDest, width, height, isMutable);
        renderRegion(context, dataSource.contextInfo.context.canvas, x, y, width, height, transform, 0, 0, TOP|LEFT);
    };

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDataCopy.(Ljavax/microedition/lcdui/ImageData;Ljavax/microedition/lcdui/ImageData;)V"] =
    function(dest, source) {
        var srcCanvas = source.contextInfo.context.canvas;
        var context = initImageData(dest, srcCanvas.width, srcCanvas.height, 0);
        context.drawImage(srcCanvas, 0, 0);
    };

    Native["javax/microedition/lcdui/ImageDataFactory.createMutableImageData.(Ljavax/microedition/lcdui/ImageData;II)V"] =
    function(imageData, width, height) {
        var context = initImageData(imageData, width, height, 1);
        context.fillStyle = "rgb(255,255,255)"; // white
        context.fillRect(0, 0, width, height);
    };

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeRGBImage.(Ljavax/microedition/lcdui/ImageData;[IIIZ)V"] =
    function(imageData, rgbData, width, height, processAlpha) {
        var context = initImageData(imageData, width, height, 0);
        var ctxImageData = context.createImageData(width, height);
        var abgrData = new Int32Array(ctxImageData.data.buffer);

        if (1 === processAlpha) {
            ARGBToABGR(rgbData, abgrData, width, height, 0, width);
        } else {
            ARGBTo1BGR(rgbData, abgrData, width, height, 0, width);
        }

        context.putImageData(ctxImageData, 0, 0);
    };

    Native["javax/microedition/lcdui/ImageData.getRGB.([IIIIIII)V"] = function(rgbData, offset, scanlength, x, y, width, height) {
        var abgrData = new Int32Array(this.contextInfo.context.getImageData(x, y, width, height).data.buffer);
        ABGRToARGB(abgrData, rgbData, width, height, offset, scanlength);
    };

    Native["com/nokia/mid/ui/DirectUtils.makeMutable.(Ljavax/microedition/lcdui/Image;)V"] = function(image) {
        var imageData = image.imageData;
        imageData.isMutable = 1;
    };

    Native["com/nokia/mid/ui/DirectUtils.setPixels.(Ljavax/microedition/lcdui/Image;I)V"] = function(image, argb) {
        var width = image.width;
        var height = image.height;
        var imageData = image.imageData;

        // NOTE: This function will only ever be called by the variants
        // of `DirectUtils.createImage`. We don't have to worry about
        // the dimensions or the context info because this `Image` and
        // this `ImageData` were just created; nothing can be out of
        // sync yet.
        var ctx = imageData.contextInfo.context;

        var ctxImageData = ctx.createImageData(width, height);
        var pixels = new Int32Array(ctxImageData.data.buffer);

        var color = swapRB(argb);

        var i = 0;
        for (var y = 0; y < height; ++y) {
            for (var x = 0; x < width; ++x) {
                pixels[i++] = color;
            }
        }

        ctx.putImageData(ctxImageData, 0, 0);
    };

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

    Native["javax/microedition/lcdui/Font.init.(III)V"] = function(face, style, size) {
        var defaultSize = config.fontSize ? config.fontSize : Math.max(19, (MIDP.context2D.canvas.height / 35) | 0);
        if (size & SIZE_SMALL)
            size = defaultSize / 1.25;
        else if (size & SIZE_LARGE)
            size = defaultSize * 1.25;
        else
            size = defaultSize;
        size |= 0;

        if (style & STYLE_BOLD)
            style = "bold ";
        else if (style & STYLE_ITALIC)
            style = "italic ";
        else
            style = "";

        if (face & FACE_MONOSPACE)
            face = "monospace";
        else if (face & FACE_PROPORTIONAL)
            face = "sans-serif";
        else
            face = "Arial,Helvetica,sans-serif";

        this.baseline = size | 0;
        this.height = (size * 1.3) | 0;

        this.context = document.createElement("canvas").getContext("2d");
        this.context.canvas.width = 0;
        this.context.canvas.height = 0;
        this.context.font = style + size + "px " + face;
        this.size = size;
        this.style = style;
        this.face = face;
    };

    function calcStringWidth(font, str) {
        var emojiLen = 0;

        var len = font.context.measureText(str.replace(emoji.regEx, function() {
            emojiLen += font.size;
            return "";
        })).width | 0;

        return len + emojiLen;
    }

    var defaultFont;
    function getDefaultFont() {
        if (!defaultFont) {
            var classInfo = CLASSES.loadAndLinkClass("javax/microedition/lcdui/Font");
            defaultFont = new classInfo.klass();
            var methodInfo = classInfo.getMethodByNameString("<init>", "(III)V", false);
            J2ME.getLinkedMethod(methodInfo).call(defaultFont, 0, 0, 0);
        }
        return defaultFont;
    }

    Native["javax/microedition/lcdui/Font.getDefaultFont.()Ljavax/microedition/lcdui/Font;"] = function() {
        return getDefaultFont();
    };

    Native["javax/microedition/lcdui/Font.stringWidth.(Ljava/lang/String;)I"] = function(str) {
        return calcStringWidth(this, J2ME.fromJavaString(str));
    };

    Native["javax/microedition/lcdui/Font.charWidth.(C)I"] = function(char) {
        return this.context.measureText(String.fromCharCode(char)).width | 0;
    };

    Native["javax/microedition/lcdui/Font.charsWidth.([CII)I"] = function(str, offset, len) {
        return calcStringWidth(this, util.fromJavaChars(str).slice(offset, offset + len));
    };

    Native["javax/microedition/lcdui/Font.substringWidth.(Ljava/lang/String;II)I"] = function(str, offset, len) {
        return calcStringWidth(this, J2ME.fromJavaString(str).slice(offset, offset + len));
    };

    var HCENTER = 1;
    var VCENTER = 2;
    var LEFT = 4;
    var RIGHT = 8;
    var TOP = 16;
    var BOTTOM = 32;
    var BASELINE = 64;

    function withTextAnchor(c, font, anchor, x, str) {
        if (anchor & RIGHT || anchor & HCENTER) {
            var w = calcStringWidth(font, str);

            if (anchor & RIGHT) {
                x -= w;
            } else if (anchor & HCENTER) {
                x -= (w >>> 1) | 0;
            }
        }

        if (anchor & BOTTOM) {
            c.textBaseline = "bottom";
        } else if (anchor & BASELINE) {
            c.textBaseline = "alphabetic";
        } else if (anchor & VCENTER) {
            throw $.newIllegalArgumentException("VCENTER not allowed with text");
        } else {
            c.textBaseline = "top";
        }

        return x;
    }

    /**
     * create the outline of an elliptical arc
     * covering the specified rectangle.
     * @param x the x-coordinate of the center of the ellipse.
     * @param y y-coordinate of the center of the ellipse.
     * @param rw the horizontal radius of the arc.
     * @param rh the vertical radius of the arc.
     * @param arcStart the beginning angle
     * @param arcEnd the ending angle
     * @param closed if true, draw a closed arc sector.
     */
    function createEllipticalArc(c, x, y, rw, rh, arcStart, arcEnd, closed) {
          c.save();
          c.translate(x, y);
          if (closed) {
            c.moveTo(0, 0);
          }
          // draw circle arc which will be stretched into an oval arc
          c.scale(1, rh / rw);
          c.arc(0, 0, rw, arcStart, arcEnd, false);
          if (closed) {
            c.lineTo(0, 0);
          }
          c.restore();
    }

    /**
     * Create a round rectangle path.
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @param arcWidth the horizontal diameter of the arc at the four corners
     * @param arcHeight the vertical diameter of the arc at the four corners
     */
    function createRoundRect(c, x, y, width, height, arcWidth, arcHeight) {
        var rw = arcWidth / 2;
        var rh = arcHeight / 2;
        c.moveTo(x + rw, y);
        c.lineTo(x + width - rw, y);
        createEllipticalArc(c, x + width - rw, y + rh, rw, rh, 1.5 * Math.PI, 2 * Math.PI, false);
        c.lineTo(x + width, y + height - rh);
        createEllipticalArc(c, x + width - rw, y + height - rh, rw, rh, 0, 0.5 * Math.PI, false);
        c.lineTo(x + rw, y + height);
        createEllipticalArc(c, x + rw, y + height - rh, rw, rh, 0.5 * Math.PI, Math.PI, false);
        c.lineTo(x, y + rh);
        createEllipticalArc(c, x + rw, y + rh, rw, rh, Math.PI, 1.5 * Math.PI, false);
    }

    Native["javax/microedition/lcdui/Graphics.getDisplayColor.(I)I"] = function(color) {
        return color & 0x00FFFFFF;
    };

    Native["javax/microedition/lcdui/Graphics.resetGC.()V"] = function() {
        this.info.resetGC();
    };

    Native["javax/microedition/lcdui/Graphics.reset.(IIII)V"] = function(x1, y1, x2, y2) {
        this.info.reset(x1, y1, x2, y2);
    };

    Native["javax/microedition/lcdui/Graphics.reset.()V"] = function() {
        this.info.reset(0, 0, this.info.contextInfo.context.canvas.width, this.info.contextInfo.context.canvas.height);
    };

    Native["javax/microedition/lcdui/Graphics.copyArea.(IIIIIII)V"] = function(x_src, y_src, width, height, x_dest, y_dest, anchor) {
        if (isScreenGraphics(this)) {
            throw $.newIllegalStateException();
        }
        console.warn("javax/microedition/lcdui/Graphics.copyArea.(IIIIIII)V not implemented");
    };

    Native["javax/microedition/lcdui/Graphics.setDimensions.(II)V"] = function(w, h) {
        this.info.resetNonGC(0, 0, w, h);
    };

    Native["javax/microedition/lcdui/Graphics.translate.(II)V"] = function(x, y) {
        this.info.translate(x, y);
    };

    Native["javax/microedition/lcdui/Graphics.getTranslateX.()I"] = function() {
        return this.info.transX;
    };

    Native["javax/microedition/lcdui/Graphics.getTranslateY.()I"] = function() {
        return this.info.transY;
    };

    Native["javax/microedition/lcdui/Graphics.getMaxWidth.()S"] = function() {
        return this.info.contextInfo.context.canvas.width;
    };

    Native["javax/microedition/lcdui/Graphics.getMaxHeight.()S"] = function() {
        return this.info.contextInfo.context.canvas.height;
    };

    Native["javax/microedition/lcdui/Graphics.getCreator.()Ljava/lang/Object;"] = function() {
        return this.creator;
    };

    Native["javax/microedition/lcdui/Graphics.setCreator.(Ljava/lang/Object;)V"] = function(creator) {
        if (!this.creator) {
            this.creator = creator;
        }
    };

    Native["javax/microedition/lcdui/Graphics.getColor.()I"] = function() {
        return (this.info.red << 16) | (this.info.green << 8) | this.info.blue;
    };

    Native["javax/microedition/lcdui/Graphics.getRedComponent.()I"] = function() {
        return this.info.red;
    };

    Native["javax/microedition/lcdui/Graphics.getGreenComponent.()I"] = function() {
        return this.info.green;
    };

    Native["javax/microedition/lcdui/Graphics.getBlueComponent.()I"] = function() {
        return this.info.blue;
    };

    Native["javax/microedition/lcdui/Graphics.getGrayScale.()I"] = function() {
        return (this.info.red*76 + this.info.green*150 + this.info.blue*29) >>> 8;
    };

    Native["javax/microedition/lcdui/Graphics.setColor.(III)V"] = function(red, green, blue) {
        if ((red < 0)   || (red > 255)
            || (green < 0) || (green > 255)
            || (blue < 0)  || (blue > 255)) {
            throw $.newIllegalArgumentException("Value out of range");
        }

        this.info.setPixel(0xFF, red, green, blue);
    };

    Native["javax/microedition/lcdui/Graphics.setColor.(I)V"] = function(rgb) {
        var red = (rgb >>> 16) & 0xFF;
        var green = (rgb >>> 8) & 0xFF;
        var blue = rgb & 0xFF;

        // NOTE: One would probably expect that `Graphics.setColor`
        // would always set the alpha value to 0xFF but that is not
        // the case if the current RGB value is the same as
        // value being set. This is the behavior
        // of the reference implementation so we are copying
        // that behavior.
        if (red != this.info.red || green != this.info.green || blue != this.info.blue) {
            this.info.setPixel(0xFF, red, green, blue);
        }
    };

    Native["javax/microedition/lcdui/Graphics.setGrayScale.(I)V"] = function(value) {
        if ((value < 0) || (value > 255)) {
            throw $.newIllegalArgumentException("Gray value out of range");
        }

        // NOTE: One would probably expect that `Graphics.setGrayScale`
        // would always set the alpha value to 0xFF but that is not
        // the case if the red, green, and blue color values are
        // the same as the values being set. This is the behavior
        // of the reference implementation so we are copying
        // that behavior.
        if (value != this.info.red || value != this.info.green || value != this.info.blue) {
            this.info.setPixel(0xFF, value, value, value);
        }
    };

    Native["javax/microedition/lcdui/Graphics.getFont.()Ljavax/microedition/lcdui/Font;"] = function() {
        return this.info.currentFont;
    };

    Native["javax/microedition/lcdui/Graphics.setFont.(Ljavax/microedition/lcdui/Font;)V"] = function(font) {
        this.info.setFont(font);
    };

    var SOLID = 0;
    var DOTTED = 1;
    Native["javax/microedition/lcdui/Graphics.setStrokeStyle.(I)V"] = function(style) {
        if ((style !== SOLID) && (style !== DOTTED)) {
            throw $.newIllegalArgumentException("Invalid stroke style");
        }

        // We don't actually implement DOTTED style so this is a no-op
    };

    Native["javax/microedition/lcdui/Graphics.getStrokeStyle.()I"] = function() {
        return SOLID;
    };

    Native["javax/microedition/lcdui/Graphics.getClipX.()I"] = function() {
        return this.info.clipX1 - this.info.transX;
    };

    Native["javax/microedition/lcdui/Graphics.getClipY.()I"] = function() {
        return this.info.clipY1 - this.info.transY;
    };

    Native["javax/microedition/lcdui/Graphics.getClipWidth.()I"] = function() {
        return this.info.clipX2 - this.info.clipX1;
    };

    Native["javax/microedition/lcdui/Graphics.getClipHeight.()I"] = function() {
        return this.info.clipY2 - this.info.clipY1;
    };

    Native["javax/microedition/lcdui/Graphics.getClip.([I)V"] = function(region) {
        region[0] = this.info.clipX1 - this.info.transX;
        region[1] = this.info.clipY1 - this.info.transY;
        region[2] = this.info.clipX2 - this.info.transX;
        region[3] = this.info.clipY2 - this.info.transY;
    };

    Native["javax/microedition/lcdui/Graphics.clipRect.(IIII)V"] = function(x, y, width, height) {
        this.info.setClip(x, y, width, height, this.info.clipX1, this.info.clipY1, this.info.clipX2, this.info.clipY2);
    };

    // DirectGraphics constants
    var TYPE_USHORT_4444_ARGB = 4444;
    var TYPE_USHORT_565_RGB = 565;

    Native["com/nokia/mid/ui/DirectGraphicsImp.setARGBColor.(I)V"] = function(argb) {
        var alpha = (argb >>> 24);
        var red = (argb >>> 16) & 0xFF;
        var green = (argb >>> 8) & 0xFF;
        var blue = argb & 0xFF;
        this.graphics.info.setPixel(alpha, red, green, blue);
    };

    Native["com/nokia/mid/ui/DirectGraphicsImp.getAlphaComponent.()I"] = function() {
        return this.graphics.info.alpha;
    };

    Native["com/nokia/mid/ui/DirectGraphicsImp.getPixels.([SIIIIIII)V"] =
    function(pixels, offset, scanlength, x, y, width, height, format) {
        if (pixels === null) {
            throw $.newNullPointerException("Pixels array is null");
        }

        var converterFunc = null;
        if (format === TYPE_USHORT_4444_ARGB) {
            converterFunc = ABGRToARGB4444;
        } else if (format === TYPE_USHORT_565_RGB) {
            converterFunc = ABGRToRGB565;
        } else {
            throw $.newIllegalArgumentException("Format unsupported");
        }

        var context = this.graphics.info.contextInfo.context;
        var abgrData = new Int32Array(context.getImageData(x, y, width, height).data.buffer);
        converterFunc(abgrData, pixels, width, height, offset, scanlength);
    };

    Native["com/nokia/mid/ui/DirectGraphicsImp.drawPixels.([SZIIIIIIII)V"] =
    function(pixels, transparency, offset, scanlength, x, y, width, height, manipulation, format) {
        if (pixels === null) {
            throw $.newNullPointerException("Pixels array is null");
        }

        var converterFunc = null;
        if (format === TYPE_USHORT_4444_ARGB && transparency && !manipulation) {
            converterFunc = ARGB4444ToABGR;
        } else {
            throw $.newIllegalArgumentException("Format unsupported");
        }

        var graphics = this.graphics;

        tempContext.canvas.width = width;
        tempContext.canvas.height = height;
        var imageData = tempContext.createImageData(width, height);
        var abgrData = new Int32Array(imageData.data.buffer);

        converterFunc(pixels, abgrData, width, height, offset, scanlength);

        tempContext.putImageData(imageData, 0, 0);

        var c = graphics.info.getGraphicsContext();

        c.drawImage(tempContext.canvas, x, y);
        tempContext.canvas.width = 0;
        tempContext.canvas.height = 0;
    };

    Native["javax/microedition/lcdui/Graphics.render.(Ljavax/microedition/lcdui/Image;III)Z"] = function(image, x, y, anchor) {
        renderRegion(this.info.getGraphicsContext(), image.imageData.contextInfo.context.canvas, 0, 0, image.width, image.height, TRANS_NONE, x, y, anchor);
        return 1;
    };

    Native["javax/microedition/lcdui/Graphics.drawRegion.(Ljavax/microedition/lcdui/Image;IIIIIIII)V"] = function(src, x_src, y_src, width, height, transform, x_dest, y_dest, anchor) {
        if (null === src) {
            throw $.newNullPointerException("src image is null");
        }

        renderRegion(this.info.getGraphicsContext(), src.imageData.contextInfo.context.canvas, x_src, y_src, width, height,
                          transform, x_dest, y_dest, anchor);
    };

    Native["javax/microedition/lcdui/Graphics.drawImage.(Ljavax/microedition/lcdui/Image;III)V"] = function(image, x, y, anchor) {
        if (image === null) {
            throw $.newNullPointerException("image is null");
        }

        renderRegion(this.info.getGraphicsContext(), image.imageData.contextInfo.context.canvas, 0, 0, image.imageData.width, image.imageData.height, TRANS_NONE, x, y, anchor);
    };

    function GraphicsInfo(contextInfo) {
        this.contextInfo = contextInfo;

        // non-GC info
        this.transX = 0;
        this.transY = 0;
        this.clipX1 = 0;
        this.clipY1 = 0;
        this.clipX2 = contextInfo.context.canvas.width;
        this.clipY2 = contextInfo.context.canvas.height;

        // GC info
        this.currentFont = getDefaultFont();
        this.alpha = 0xFF;
        this.red = 0x00;
        this.green = 0x00;
        this.blue = 0x00;
    }

    GraphicsInfo.prototype.setFont = function(font) {
        if (null === font) {
            font = getDefaultFont();
        }

        if (this.currentFont !== font) {
            this.currentFont = font;
            if (this.contextInfo.currentlyAppliedGraphicsInfo === this) {
                this.contextInfo.currentlyAppliedGraphicsInfo = null;
            }
        }
    }

    GraphicsInfo.prototype.setPixel = function(alpha, red, green, blue) {
        if (this.alpha !== alpha || this.red !== red || this.green !== green || this.blue !== blue) {
            this.alpha = alpha;
            this.red = red;
            this.green = green;
            this.blue = blue;

            if (this.contextInfo.currentlyAppliedGraphicsInfo === this) {
                this.contextInfo.currentlyAppliedGraphicsInfo = null;
            }
        }
    }

    GraphicsInfo.prototype.resetGC = function() {
        this.setFont(null);
        this.setPixel(0xFF, 0x00, 0x00, 0x00);
    }

    GraphicsInfo.prototype.reset = function(x1, y1, x2, y2) {
        this.resetGC();
        this.resetNonGC(x1, y1, x2, y2);
    }

    GraphicsInfo.prototype.resetNonGC = function(x1, y1, x2, y2) {
        this.translate(-this.transX, -this.transY);
        this.setClip(x1, y1, x2 - x1, y2 - y1, 0, 0, this.contextInfo.context.canvas.width, this.contextInfo.context.canvas.height);
    }

    GraphicsInfo.prototype.translate = function(x, y) {
        x = x | 0;
        y = y | 0;
        if (x !== 0 || y !== 0) {
            this.transX += x;
            this.transY += y;

            if (this.contextInfo.currentlyAppliedGraphicsInfo === this) {
                this.contextInfo.currentlyAppliedGraphicsInfo = null;
            }
        }
    }

    GraphicsInfo.prototype.setClip = function(x, y, width, height, minX, minY, maxX, maxY) {
        var newX1 = x + this.transX;
        var newY1 = y + this.transY;
        var newX2 = newX1 + width;
        var newY2 = newY1 + height;

        newX1 = Math.max(minX, newX1) & 0x7fff;
        newY1 = Math.max(minY, newY1) & 0x7fff;
        newX2 = Math.min(maxX, newX2) & 0x7fff;
        newY2 = Math.min(maxY, newY2) & 0x7fff;

        if (width <= 0 || height <= 0 || newX2 <= newX1 || newY2 <= newY1) {
            newX1 = newY1 = newX2 = newY2 = 0;
        }

        if (this.clipX1 === newX1 && this.clipY1 === newY1 && this.clipX2 === newX2 && this.clipY2 === newY2) {
            return;
        }

        if (this.contextInfo.currentlyAppliedGraphicsInfo === this) {
            this.contextInfo.currentlyAppliedGraphicsInfo = null;
        }

        this.clipX1 = newX1;
        this.clipY1 = newY1;
        this.clipX2 = newX2;
        this.clipY2 = newY2;
    }

    GraphicsInfo.prototype.getGraphicsContext = function() {
        if (this.contextInfo.currentlyAppliedGraphicsInfo !== this) {
            this.contextInfo.applyGraphics(this);
        }

        return this.contextInfo.context;
    }

    function ContextInfo(ctx) {
        this.currentlyAppliedGraphicsInfo = null;
        this.context = ctx;
        ctx.save();
    }

    ContextInfo.prototype.applyGraphics = function(graphicsInfo) {
        this.context.restore();
        this.context.save();

        this.context.textAlign = "left";

        this.context.fillStyle = this.context.strokeStyle = util.rgbaToCSS(graphicsInfo.red, graphicsInfo.green, graphicsInfo.blue, graphicsInfo.alpha / 255);
        this.context.font = graphicsInfo.currentFont.context.font;

        this.context.beginPath();
        this.context.rect(graphicsInfo.clipX1, graphicsInfo.clipY1, graphicsInfo.clipX2 - graphicsInfo.clipX1, graphicsInfo.clipY2 - graphicsInfo.clipY1);
        this.context.clip();
        this.context.translate(graphicsInfo.transX, graphicsInfo.transY);

        this.currentlyAppliedGraphicsInfo = graphicsInfo;
    };

    Native["javax/microedition/lcdui/Graphics.initScreen0.(I)V"] = function(displayId) {
        this.displayId = displayId;
        this.info = new GraphicsInfo(screenContextInfo);
        this.creator = null;
    };

    Native["javax/microedition/lcdui/Graphics.initImage0.(Ljavax/microedition/lcdui/Image;)V"] = function(img) {
        this.displayId = -1;
        this.info = new GraphicsInfo(img.imageData.contextInfo);
        this.creator = null;
    };

    function isScreenGraphics(g) {
        return g.displayId !== -1;
    }

    Native["javax/microedition/lcdui/Graphics.setClip.(IIII)V"] = function(x, y, w, h) {
        this.info.setClip(x, y, w, h, 0, 0, this.info.contextInfo.context.canvas.width, this.info.contextInfo.context.canvas.height);
    };

    function drawString(g, str, x, y, anchor) {
        var c = g.info.getGraphicsContext();

        var finalText;
        if (!emoji.regEx.test(str)) {
            // No emojis are present.
            finalText = str;
        } else {
            // Emojis are present. Handle all the text up to the last emoji.
            var font = g.info.currentFont;
            var match;
            var lastIndex = 0;
            emoji.regEx.lastIndex = 0;
            while (match = emoji.regEx.exec(str)) {
                var text = str.substring(lastIndex, match.index);
                var match0 = match[0];
                lastIndex = match.index + match0.length;

                var textX = withTextAnchor(c, g.info.currentFont, anchor, x, text);

                c.fillText(text, textX, y);

                // Calculate the string width.
                x += c.measureText(text).width | 0;

                var emojiData = emoji.getData(match0, font.size);
                c.drawImage(emojiData.img, emojiData.x, 0, emoji.squareSize, emoji.squareSize, x, y, font.size, font.size);
                x += font.size;
            }
            finalText = str.substring(lastIndex);
        }

        // Now handle all the text after the final emoji. If there were no
        // emojis present, this is the entire string.
        if (finalText) {
            var textX = withTextAnchor(c, g.info.currentFont, anchor, x, finalText);
            c.fillText(finalText, textX, y);
        }
    }

    Native["javax/microedition/lcdui/Graphics.drawString.(Ljava/lang/String;III)V"] = function(str, x, y, anchor) {
        drawString(this, J2ME.fromJavaString(str), x, y, anchor);
    };

    Native["javax/microedition/lcdui/Graphics.drawSubstring.(Ljava/lang/String;IIIII)V"] = 
    function(str, offset, len, x, y, anchor) {
        drawString(this, J2ME.fromJavaString(str).substr(offset, len), x, y, anchor);
    };

    Native["javax/microedition/lcdui/Graphics.drawChars.([CIIIII)V"] = function(data, offset, len, x, y, anchor) {
        drawString(this, util.fromJavaChars(data, offset, len), x, y, anchor);
    };

    Native["javax/microedition/lcdui/Graphics.drawChar.(CIII)V"] = function(jChr, x, y, anchor) {
        var chr = String.fromCharCode(jChr);

        var c = this.info.getGraphicsContext();

        x = withTextAnchor(c, this.info.currentFont, anchor, x, chr);

        c.fillText(chr, x, y);
    };

    Native["javax/microedition/lcdui/Graphics.fillTriangle.(IIIIII)V"] = function(x1, y1, x2, y2, x3, y3) {
        var c = this.info.getGraphicsContext();

        var dx1 = (x2 - x1) || 1;
        var dy1 = (y2 - y1) || 1;
        var dx2 = (x3 - x1) || 1;
        var dy2 = (y3 - y1) || 1;

        c.beginPath();
        c.moveTo(x1, y1);
        c.lineTo(x1 + dx1, y1 + dy1);
        c.lineTo(x1 + dx2, y1 + dy2);
        c.closePath();
        c.fill();
    };

    Native["javax/microedition/lcdui/Graphics.drawRect.(IIII)V"] = function(x, y, w, h) {
        if (w < 0 || h < 0) {
            return;
        }

        var c = this.info.getGraphicsContext();

        w = w || 1;
        h = h || 1;

        c.strokeRect(x, y, w, h);
    };

    Native["javax/microedition/lcdui/Graphics.drawRoundRect.(IIIIII)V"] = function(x, y, w, h, arcWidth, arcHeight) {
        if (w < 0 || h < 0) {
            return;
        }

        var c = this.info.getGraphicsContext();

        w = w || 1;
        h = h || 1;

        c.beginPath();
        createRoundRect(c, x, y, w, h, arcWidth, arcHeight);
        c.stroke();
    };

    Native["javax/microedition/lcdui/Graphics.fillRect.(IIII)V"] = function(x, y, w, h) {
        if (w <= 0 || h <= 0) {
            return;
        }

        var c = this.info.getGraphicsContext();

        w = w || 1;
        h = h || 1;

        c.fillRect(x, y, w, h);
    };

    Native["javax/microedition/lcdui/Graphics.fillRoundRect.(IIIIII)V"] = function(x, y, w, h, arcWidth, arcHeight) {
        if (w <= 0 || h <= 0) {
            return;
        }

        var c = this.info.getGraphicsContext();

        w = w || 1;
        h = h || 1;

        c.beginPath();
        createRoundRect(c, x, y, w, h, arcWidth, arcHeight);
        c.fill();
    };

    Native["javax/microedition/lcdui/Graphics.drawArc.(IIIIII)V"] = function(x, y, width, height, startAngle, arcAngle) {
        if (width < 0 || height < 0) {
            return;
        }

        var c = this.info.getGraphicsContext();

        var endRad = -startAngle * 0.0175;
        var startRad = endRad - arcAngle * 0.0175;
        c.beginPath();
        createEllipticalArc(c, x, y, width / 2, height / 2, startRad, endRad, false);
        c.stroke();
    };

    Native["javax/microedition/lcdui/Graphics.fillArc.(IIIIII)V"] = function(x, y, width, height, startAngle, arcAngle) {
        if (width <= 0 || height <= 0) {
            return;
        }

        var c = this.info.getGraphicsContext();

        var endRad = -startAngle * 0.0175;
        var startRad = endRad - arcAngle * 0.0175;
        c.beginPath();
        c.moveTo(x, y);
        createEllipticalArc(c, x, y, width / 2, height / 2, startRad, endRad, true);
        c.moveTo(x, y);
        c.fill();
    };

    var TRANS_NONE = 0;
    var TRANS_MIRROR_ROT180 = 1;
    var TRANS_MIRROR = 2;
    var TRANS_ROT180 = 3;
    var TRANS_MIRROR_ROT270 = 4;
    var TRANS_ROT90 = 5;
    var TRANS_ROT270 = 6;
    var TRANS_MIRROR_ROT90 = 7;

    function renderRegion(dstContext, srcCanvas, sx, sy, sw, sh, transform, absX, absY, anchor) {
        var w, h;
        switch (transform) {
            case TRANS_NONE:
            case TRANS_ROT180:
            case TRANS_MIRROR:
            case TRANS_MIRROR_ROT180:
                w = sw;
                h = sh;
                break;
            case TRANS_ROT90:
            case TRANS_ROT270:
            case TRANS_MIRROR_ROT90:
            case TRANS_MIRROR_ROT270:
                w = sh;
                h = sw;
                break;
        }

        // Make `absX` and `absY` the top-left coordinates where we will
        // place the image in absolute coordinates
        if (0 !== (anchor & HCENTER)) {
            absX -= ((w >>> 1) | 0);
        } else if (0 !== (anchor & RIGHT)) {
            absX -= w;
        }
        if (0 !== (anchor & VCENTER)) {
            absY -= ((h >>> 1) | 0);
        } else if (0 !== (anchor & BOTTOM)) {
            absY -= h;
        }

        var x, y;
        switch (transform) {
            case TRANS_NONE:
                x = absX;
                y = absY;
                break;
            case TRANS_ROT90:
                dstContext.rotate(Math.PI / 2);
                x = absY;
                y = -absX - w;
                break;
            case TRANS_ROT180:
                dstContext.rotate(Math.PI);
                x = -absX - w;
                y = -absY - h;
                break;
            case TRANS_ROT270:
                dstContext.rotate(Math.PI * 1.5);
                x = -absY - h;
                y = absX;
                break;
            case TRANS_MIRROR:
                dstContext.scale(-1, 1);
                x = -absX - w;
                y = absY;
                break;
            case TRANS_MIRROR_ROT90:
                dstContext.rotate(Math.PI / 2);
                dstContext.scale(-1, 1);
                x = -absY - h;
                y = -absX - w;
                break;
            case TRANS_MIRROR_ROT180:
                dstContext.scale(1, -1);
                x = absX;
                y = -absY - h;
                break;
            case TRANS_MIRROR_ROT270:
                dstContext.rotate(Math.PI * 1.5);
                dstContext.scale(-1, 1);
                x = absY;
                y = absX;
                break;
        }

        dstContext.drawImage(srcCanvas, sx, sy, sw, sh, x, y, sw, sh);

        switch (transform) {
            case TRANS_NONE:
                break;
            case TRANS_ROT90:
                dstContext.rotate(Math.PI * 1.5);
                break;
            case TRANS_ROT180:
                dstContext.rotate(Math.PI);
                break;
            case TRANS_ROT270:
                dstContext.rotate(Math.PI / 2);
                break;
            case TRANS_MIRROR:
                dstContext.scale(-1, 1);
                break;
            case TRANS_MIRROR_ROT90:
                dstContext.scale(-1, 1);
                dstContext.rotate(Math.PI * 1.5);
                break;
            case TRANS_MIRROR_ROT180:
                dstContext.scale(1, -1);
                break;
            case TRANS_MIRROR_ROT270:
                dstContext.scale(-1, 1);
                dstContext.rotate(Math.PI / 2);
                break;
        }
    };

    Native["javax/microedition/lcdui/Graphics.drawLine.(IIII)V"] = function(x1, y1, x2, y2) {
        var c = this.info.getGraphicsContext();

        // If we're drawing a completely vertical line that is
        // 1 pixel thick, we should draw it at half-pixel offsets.
        // Otherwise, half of the line's thickness lies to the left
        // of the pixel and half to the right.
        if (x1 === x2) {
            x1 += 0.5;
            x2 += 0.5;
        }

        // If we're drawing a completely horizontal line that is
        // 1 pixel thick, we should draw it at half-pixel offsets.
        // Otherwise, half of the line's thickness lies above
        // the pixel and half below.
        if (y1 === y2) {
            y1 += 0.5;
            y2 += 0.5;
        }

        c.beginPath();
        c.moveTo(x1, y1);
        c.lineTo(x2, y2);
        c.stroke();
        c.closePath();
    };

    Native["javax/microedition/lcdui/Graphics.drawRGB.([IIIIIIIZ)V"] =
    function(rgbData, offset, scanlength, x, y, width, height, processAlpha) {
        tempContext.canvas.height = height;
        tempContext.canvas.width = width;
        var imageData = tempContext.createImageData(width, height);
        var abgrData = new Int32Array(imageData.data.buffer);

        if (1 === processAlpha) {
            ARGBToABGR(rgbData, abgrData, width, height, offset, scanlength);
        } else {
            ARGBTo1BGR(rgbData, abgrData, width, height, offset, scanlength);
        }

        tempContext.putImageData(imageData, 0, 0);

        var c = this.info.getGraphicsContext();

        c.drawImage(tempContext.canvas, x, y);
        tempContext.canvas.width = 0;
        tempContext.canvas.height = 0;
    };

    var textEditorId = 0,
        textEditorResolve = null,
        dirtyEditors = [];

    function wakeTextEditorThread(textEditor) {
        dirtyEditors.push(textEditor);
        if (textEditorResolve) {
            textEditorResolve();
            textEditorResolve = null;
        }
    }

    Native["com/nokia/mid/ui/TextEditor.init.(Ljava/lang/String;IIII)V"] =
    function(text, maxSize, constraints, width, height) {
        if (constraints !== 0) {
            console.warn("TextEditor.constraints not implemented");
        }

        this.textEditorId = ++textEditorId;
        this.textEditor = TextEditorProvider.getEditor(constraints, null, this.textEditorId);
        this.visible = false;
        this.textEditor.setBackgroundColor(0xFFFFFFFF | 0); // opaque white
        this.textEditor.setForegroundColor(0xFF000000 | 0); // opaque black

        this.getCaretPosition = function() {
            if (this.textEditor.isAttached()) {
                return this.textEditor.getSelectionStart();
            }
            if (this.caretPosition !== null) {
                return this.caretPosition;
            }
            return 0;
        };

        this.setCaretPosition = function(index) {
            if (this.textEditor.isAttached()) {
                this.textEditor.setSelectionRange(index, index);
            } else {
                this.caretPosition = index;
            }
        };

        this.textEditor.setAttribute("maxlength", maxSize);
        this.textEditor.setSize(width, height);
        this.textEditor.setVisible(false);
        var font = this.font;
        this.textEditor.setFont(font);

        this.textEditor.setContent(J2ME.fromJavaString(text));
        this.setCaretPosition(this.textEditor.getContentSize());

        this.textEditor.oninput(function(e) {
            wakeTextEditorThread(this);
        }.bind(this));
    };

    Native["com/nokia/mid/ui/CanvasItem.attachNativeImpl.()V"] = function() {
        this.textEditor.attach();
        if (this.caretPosition !== null) {
            this.textEditor.setSelectionRange(this.caretPosition, this.caretPosition);
            this.caretPosition = null;
        }
    };

    Native["com/nokia/mid/ui/CanvasItem.detachNativeImpl.()V"] = function() {
        this.caretPosition = this.textEditor.getSelectionStart();
        this.textEditor.detach();
    };

    Native["javax/microedition/lcdui/Display.setTitle.(Ljava/lang/String;)V"] = function(title) {
        document.getElementById("display_title").textContent = J2ME.fromJavaString(title);
    };

    Native["com/nokia/mid/ui/CanvasItem.setSize.(II)V"] = function(width, height) {
        this.textEditor.setSize(width, height);
    };

    Native["com/nokia/mid/ui/CanvasItem.setVisible.(Z)V"] = function(visible) {
        this.textEditor.setVisible(visible ? true : false);
        this.visible = visible;
    };

    Native["com/nokia/mid/ui/CanvasItem.getWidth.()I"] = function() {
        return this.textEditor.getWidth();
    };

    Native["com/nokia/mid/ui/CanvasItem.getHeight.()I"] = function() {
        return this.textEditor.getHeight();
    };

    Native["com/nokia/mid/ui/CanvasItem.setPosition0.(II)V"] = function(x, y) {
        this.textEditor.setPosition(x, y);
    };

    Native["com/nokia/mid/ui/CanvasItem.getPositionX.()I"] = function() {
        return this.textEditor.getLeft();
    };

    Native["com/nokia/mid/ui/CanvasItem.getPositionY.()I"] = function() {
        return this.textEditor.getTop();
    };

    Native["com/nokia/mid/ui/CanvasItem.isVisible.()Z"] = function() {
        return this.visible ? 1 : 0;
    };

    Native["com/nokia/mid/ui/TextEditor.setConstraints.(I)V"] = function(constraints) {
        this.textEditor = TextEditorProvider.getEditor(constraints, this.textEditor, this.textEditorId);
    };

    Native["com/nokia/mid/ui/TextEditor.getConstraints.()I"] = function() {
        return this.textEditor.constraints;
    };

    Native["com/nokia/mid/ui/TextEditor.setFocus.(Z)V"] = function(shouldFocus) {
        var promise;
        if (shouldFocus && (currentlyFocusedTextEditor !== this.textEditor)) {
            promise = this.textEditor.focus();
            currentlyFocusedTextEditor = this.textEditor;
        } else if (!shouldFocus && (currentlyFocusedTextEditor === this.textEditor)) {
            promise = this.textEditor.blur();
            currentlyFocusedTextEditor = null;
        } else {
            return;
        }
        asyncImpl("V", promise);
    };

    Native["com/nokia/mid/ui/TextEditor.hasFocus.()Z"] = function() {
        return (this.textEditor === currentlyFocusedTextEditor) ? 1 : 0;
    };

    Native["com/nokia/mid/ui/TextEditor.setCaret.(I)V"] = function(index) {
        if (index < 0 || index > this.textEditor.getContentSize()) {
            throw $.newStringIndexOutOfBoundsException();
        }

        this.setCaretPosition(index);
    };

    Native["com/nokia/mid/ui/TextEditor.getCaretPosition.()I"] = function() {
        return this.getCaretPosition();
    };

    Native["com/nokia/mid/ui/TextEditor.getBackgroundColor.()I"] = function() {
        return this.textEditor.getBackgroundColor();
    };
    Native["com/nokia/mid/ui/TextEditor.getForegroundColor.()I"] = function() {
        return this.textEditor.getForegroundColor();
    };
    Native["com/nokia/mid/ui/TextEditor.setBackgroundColor.(I)V"] = function(backgroundColor) {
        this.textEditor.setBackgroundColor(backgroundColor);
    };
    Native["com/nokia/mid/ui/TextEditor.setForegroundColor.(I)V"] = function(foregroundColor) {
        this.textEditor.setForegroundColor(foregroundColor);
    };

    Native["com/nokia/mid/ui/TextEditor.getContent.()Ljava/lang/String;"] = function() {
        return J2ME.newString(this.textEditor.getContent());
    };

    Native["com/nokia/mid/ui/TextEditor.setContent.(Ljava/lang/String;)V"] = function(jStr) {
        var str = J2ME.fromJavaString(jStr);
        this.textEditor.setContent(str);
        this.setCaretPosition(this.textEditor.getContentSize());
    };

    addUnimplementedNative("com/nokia/mid/ui/TextEditor.getLineMarginHeight.()I", 0);
    addUnimplementedNative("com/nokia/mid/ui/TextEditor.getVisibleContentPosition.()I", 0);

    Native["com/nokia/mid/ui/TextEditor.getContentHeight.()I"] = function() {
        return this.textEditor.getContentHeight();
    };

    Native["com/nokia/mid/ui/TextEditor.insert.(Ljava/lang/String;I)V"] = function(jStr, pos) {
        var str = J2ME.fromJavaString(jStr);
        var len = util.toCodePointArray(str).length;
        if (this.textEditor.getContentSize() + len > this.textEditor.getAttribute("maxlength")) {
            throw $.newIllegalArgumentException();
        }
        this.textEditor.setContent(this.textEditor.getSlice(0, pos) + str + this.textEditor.getSlice(pos));
        this.setCaretPosition(pos + len);
    };

    Native["com/nokia/mid/ui/TextEditor.delete.(II)V"] = function(offset, length) {
        var old = this.textEditor.getContent();

        var size = this.textEditor.getContentSize();
        if (offset < 0 || offset > size || length < 0 || offset + length > size) {
            throw $.newStringIndexOutOfBoundsException("offset/length invalid");
        }

        this.textEditor.setContent(this.textEditor.getSlice(0, offset) + this.textEditor.getSlice(offset + length));
        this.setCaretPosition(offset);
    };

    Native["com/nokia/mid/ui/TextEditor.getMaxSize.()I"] = function() {
        return parseInt(this.textEditor.getAttribute("maxlength"));
    };

    Native["com/nokia/mid/ui/TextEditor.setMaxSize.(I)I"] = function(maxSize) {
        if (this.textEditor.getContentSize() > maxSize) {
            var oldCaretPosition = this.getCaretPosition();

            this.textEditor.setContent(this.textEditor.getSlice(0, maxSize));

            if (oldCaretPosition > maxSize) {
                this.setCaretPosition(maxSize);
            }
        }

        this.textEditor.setAttribute("maxlength", maxSize);

        // The return value is the assigned size, which could be less than
        // the size that was requested, although in this case we always set it
        // to the requested size.
        return maxSize;
    };

    Native["com/nokia/mid/ui/TextEditor.size.()I"] = function() {
        return this.textEditor.getContentSize();
    };

    Native["com/nokia/mid/ui/TextEditor.setFont.(Ljavax/microedition/lcdui/Font;)V"] = function(font) {
        this.font = font;
        this.textEditor.setFont(font);
    };

    Native["com/nokia/mid/ui/TextEditorThread.getNextDirtyEditor.()Lcom/nokia/mid/ui/TextEditor;"] = function() {
        if (dirtyEditors.length) {
            return dirtyEditors.shift();
        }

        asyncImpl("Lcom/nokia/mid/ui/TextEditor;", new Promise(function(resolve, reject) {
            textEditorResolve = function() {
                resolve(dirtyEditors.shift());
            }
        }));
    };

    var curDisplayableId = 0;
    var nextMidpDisplayableId = 1;
    var PLAIN = 0;

    Native["javax/microedition/lcdui/DisplayableLFImpl.initialize0.()V"] = function() {
    };

    Native["javax/microedition/lcdui/DisplayableLFImpl.deleteNativeResource0.(I)V"] = function(nativeId) {
        var el = document.getElementById("displayable-" + nativeId);
        if (el) {
            el.parentElement.removeChild(el);
            if (currentlyFocusedTextEditor) {
                currentlyFocusedTextEditor.focus();
            }
        } else if (currentlyFocusedTextEditor) {
            currentlyFocusedTextEditor.blur();
        }
    };

    Native["javax/microedition/lcdui/DisplayableLFImpl.setTitle0.(ILjava/lang/String;)V"] = function(nativeId, title) {
        document.getElementById("display_title").textContent = J2ME.fromJavaString(title);
    };

    Native["javax/microedition/lcdui/CanvasLFImpl.createNativeResource0.(Ljava/lang/String;Ljava/lang/String;)I"] = function(title, ticker) {
        console.warn("javax/microedition/lcdui/CanvasLFImpl.createNativeResource0.(Ljava/lang/String;Ljava/lang/String;)I not implemented");
        curDisplayableId = nextMidpDisplayableId++;
        return curDisplayableId;
    };

    Native["javax/microedition/lcdui/AlertLFImpl.createNativeResource0.(Ljava/lang/String;Ljava/lang/String;I)I"] = function(title, ticker, type) {
        var nativeId = nextMidpDisplayableId++;
        var alertTemplateNode = document.getElementById("lcdui-alert");
        var el = alertTemplateNode.cloneNode(true);
        el.id = "displayable-" + nativeId;
        el.querySelector('h1.title').textContent = J2ME.fromJavaString(title);
        alertTemplateNode.parentNode.appendChild(el);

        return nativeId;
    };

    Native["javax/microedition/lcdui/AlertLFImpl.setNativeContents0.(ILjavax/microedition/lcdui/ImageData;[ILjava/lang/String;)Z"] =
    function(nativeId, imgId, indicatorBounds, text) {
        var el = document.getElementById("displayable-" + nativeId);
        el.querySelector('p.text').textContent = J2ME.fromJavaString(text);

        return 0;
    };

    Native["javax/microedition/lcdui/AlertLFImpl.showNativeResource0.(I)V"] = function(nativeId) {
        var el = document.getElementById("displayable-" + nativeId);
        el.style.display = 'block';
        el.classList.add('visible');
        if (currentlyFocusedTextEditor) {
            currentlyFocusedTextEditor.blur();
        }

        curDisplayableId = nativeId;
    };

    var INDEFINITE = -1;
    var CONTINUOUS_RUNNING = 2;

    Native["javax/microedition/lcdui/GaugeLFImpl.createNativeResource0.(ILjava/lang/String;IZII)I"] =
    function(ownerId, label, layout, interactive, maxValue, initialValue) {
        if (label !== null) {
            console.error("Expected null label");
        }

        if (layout !== PLAIN) {
            console.error("Expected PLAIN layout");
        }

        if (interactive) {
            console.error("Expected not interactive gauge");
        }

        if (maxValue !== INDEFINITE) {
            console.error("Expected INDEFINITE maxValue");
        }

        if (initialValue !== CONTINUOUS_RUNNING) {
            console.error("Expected CONTINUOUS_RUNNING initialValue")
        }

        var el = document.getElementById("displayable-" + ownerId);
        el.querySelector("progress").style.display = "inline";

        return nextMidpDisplayableId++;
    };

    Native["javax/microedition/lcdui/TextFieldLFImpl.createNativeResource0.(ILjava/lang/String;ILcom/sun/midp/lcdui/DynamicCharacterArray;ILjava/lang/String;)I"] =
    function(ownerId, label, layout, buffer, constraints, initialInputMode) {
        console.warn("javax/microedition/lcdui/TextFieldLFImpl.createNativeResource0.(ILjava/lang/String;ILcom/sun/midp/lcdui/DynamicCharacterArray;ILjava/lang/String;)I not implemented");
        return nextMidpDisplayableId++;
    };

    Native["javax/microedition/lcdui/ImageItemLFImpl.createNativeResource0.(ILjava/lang/String;ILjavax/microedition/lcdui/ImageData;Ljava/lang/String;I)I"] =
    function(ownerId, label, layout, imageData, altText, appearanceMode) {
        console.warn("javax/microedition/lcdui/ImageItemLFImpl.createNativeResource0.(ILjava/lang/String;ILjavax/microedition/lcdui/ImageData;Ljava/lang/String;I)I not implemented");
        return nextMidpDisplayableId++;
    };

    addUnimplementedNative("javax/microedition/lcdui/FormLFImpl.setScrollPosition0.(I)V");
    addUnimplementedNative("javax/microedition/lcdui/FormLFImpl.getScrollPosition0.()I", 0);

    addUnimplementedNative(
        "javax/microedition/lcdui/FormLFImpl.createNativeResource0.(Ljava/lang/String;Ljava/lang/String;)I",
        function() { return nextMidpDisplayableId++ }
    );

    addUnimplementedNative("javax/microedition/lcdui/FormLFImpl.showNativeResource0.(IIII)V");
    addUnimplementedNative("javax/microedition/lcdui/FormLFImpl.getViewportHeight0.()I", 0);

    addUnimplementedNative(
        "javax/microedition/lcdui/StringItemLFImpl.createNativeResource0.(ILjava/lang/String;ILjava/lang/String;ILjavax/microedition/lcdui/Font;)I",
        function() { return nextMidpDisplayableId++ }
    );

    Native["javax/microedition/lcdui/ItemLFImpl.setSize0.(III)V"] = function(nativeId, w, h) {
        console.warn("javax/microedition/lcdui/ItemLFImpl.setSize0.(III)V not implemented");
    };

    Native["javax/microedition/lcdui/ItemLFImpl.setLocation0.(III)V"] = function(nativeId, x, y) {
        console.warn("javax/microedition/lcdui/ItemLFImpl.setLocation0.(III)V not implemented");
    };

    Native["javax/microedition/lcdui/ItemLFImpl.show0.(I)V"] = function(nativeId) {
        console.warn("javax/microedition/lcdui/ItemLFImpl.show0.(I)V not implemented");
    };

    Native["javax/microedition/lcdui/ItemLFImpl.hide0.(I)V"] = function(nativeId) {
        console.warn("javax/microedition/lcdui/ItemLFImpl.hide0.(I)V not implemented");
    };

    addUnimplementedNative("javax/microedition/lcdui/ItemLFImpl.getMinimumWidth0.(I)I", 10);
    addUnimplementedNative("javax/microedition/lcdui/ItemLFImpl.getMinimumHeight0.(I)I", 10);
    addUnimplementedNative("javax/microedition/lcdui/ItemLFImpl.getPreferredWidth0.(II)I", 10);
    addUnimplementedNative("javax/microedition/lcdui/ItemLFImpl.getPreferredHeight0.(II)I", 10);
    addUnimplementedNative("javax/microedition/lcdui/ItemLFImpl.delete0.(I)V");

    var BACK = 2;
    var CANCEL = 3;
    var OK = 4;
    var STOP = 6;

    Native["javax/microedition/lcdui/NativeMenu.updateCommands.([Ljavax/microedition/lcdui/Command;I[Ljavax/microedition/lcdui/Command;I)V"] =
    function(itemCommands, numItemCommands, commands, numCommands) {
        if (numItemCommands !== 0) {
            console.error("NativeMenu.updateCommands: item commands not yet supported");
        }

        var el = document.getElementById("displayable-" + curDisplayableId);

        if (!el) {
            document.getElementById("sidebar").querySelector("nav ul").innerHTML = "";
        }

        if (!commands) {
            return;
        }

        var validCommands = commands.filter(function(command) {
            return !!command;
        }).sort(function(a, b) {
            return a.priority - b.priority;
        });

        function sendEvent(command) {
            MIDP.sendCommandEvent(command.id);
        }

        if (el) {
            if (numCommands > 2 && validCommands.length > 2) {
                console.error("NativeMenu.updateCommands: max two commands supported");
            }

            validCommands.slice(0, 2).forEach(function(command, i) {
                var button = el.querySelector(".button" + i);
                button.style.display = 'inline';
                button.textContent = J2ME.fromJavaString(command.shortLabel);

                var commandType = command.commandType;
                if (numCommands === 1 || commandType === OK) {
                    button.classList.add('recommend');
                    button.classList.remove('cancel');
                } else if (commandType === CANCEL || commandType === BACK || commandType === STOP) {
                    button.classList.add('cancel');
                    button.classList.remove('recommend');
                }

                button.onclick = function(e) {
                    e.preventDefault();
                    sendEvent(command);
                };
            });
        } else {
            var menu = document.getElementById("sidebar").querySelector("nav ul");

            var okCommand = null;
            var backCommand = null;

            var isSidebarEmpty = true;
            validCommands.forEach(function(command) {
                var commandType = command.commandType;
                // Skip the OK command which will shown in the header.
                if (commandType === OK) {
                    okCommand = command;
                    return;
                }
                // Skip the BACK command which will shown in the footer.
                if (commandType === BACK) {
                    backCommand = command;
                    return;
                }
                var li = document.createElement("li");
                var text = J2ME.fromJavaString(command.shortLabel);
                var a = document.createElement("a");
                a.textContent = text;
                li.appendChild(a);

                li.onclick = function(e) {
                    e.preventDefault();

                    window.location.hash = "";

                    sendEvent(command);
                };

                menu.appendChild(li);
                isSidebarEmpty = false;
            });

            document.getElementById("header-drawer-button").style.display =
                isSidebarEmpty ? "none" : "block";

            // If existing, the OK command will be shown in the header.
            var headerBtn = document.getElementById("header-ok-button");
            if (okCommand) {
                headerBtn.style.display = "block";
                headerBtn.onclick = sendEvent.bind(headerBtn, okCommand);
            } else {
                headerBtn.style.display = "none";
            }

            // If existing, the BACK command will be shown in the footer.
            var backBtn = document.getElementById("back-button");
            if (backCommand) {
                backBtn.style.display = "block";
                backBtn.onclick = sendEvent.bind(backBtn, backCommand);
            } else {
                backBtn.style.display = "none";
            }
        }
    };
})(Native);
