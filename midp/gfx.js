/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var currentlyFocusedTextEditor;
(function(Native) {
    var offscreenCanvas = document.createElement("canvas");
    offscreenCanvas.width = MIDP.deviceContext.canvas.width;
    offscreenCanvas.height = MIDP.deviceContext.canvas.height;
    var offscreenContext2D = offscreenCanvas.getContext("2d");
    var screenContextInfo = new ContextInfo(offscreenContext2D);

    MIDP.deviceContext.canvas.addEventListener("canvasresize", function() {
        offscreenCanvas.width = MIDP.deviceContext.canvas.width;
        offscreenCanvas.height = MIDP.deviceContext.canvas.height;
        screenContextInfo.currentlyAppliedGraphicsInfo = null;
        offscreenContext2D.save();
    });

    var tempContext = document.createElement("canvas").getContext("2d");
    tempContext.canvas.width = 0;
    tempContext.canvas.height = 0;

    Native["com/sun/midp/lcdui/DisplayDeviceContainer.getDisplayDevicesIds0.()[I"] = function(addr) {
        var idsAddr = J2ME.newIntArray(1);
        var ids = J2ME.getArrayFromAddr(idsAddr);
        ids[0] = 1;
        return idsAddr;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getDisplayName0.(I)Ljava/lang/String;"] = function(addr, id) {
        return J2ME.Constants.NULL;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPrimary0.(I)Z"] = function(addr, id) {
        console.warn("DisplayDevice.isDisplayPrimary0.(I)Z not implemented (" + id + ")");
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.isbuildInDisplay0.(I)Z"] = function(addr, id) {
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getDisplayCapabilities0.(I)I"] = function(addr, id) {
        return 0x3ff;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPenSupported0.(I)Z"] = function(addr, id) {
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPenMotionSupported0.(I)Z"] = function(addr, id) {
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.reverseOrientation0.(I)Z"] = function(addr, id) {
        return 0;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getReverseOrientation0.(I)Z"] = function(addr, id) {
        return 0;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getScreenWidth0.(I)I"] = function(addr, id) {
        return offscreenCanvas.width;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getScreenHeight0.(I)I"] = function(addr, id) {
        return offscreenCanvas.height;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.displayStateChanged0.(II)V"] = function(addr, hardwareId, state) {
        console.warn("DisplayDevice.displayStateChanged0.(II)V not implemented (" + hardwareId + ", " + state + ")");
    };

    Native["com/sun/midp/lcdui/DisplayDevice.gainedForeground0.(II)V"] = function(addr, hardwareId, displayId) {
        hideSplashScreen();

        if (!emoji.loaded) {
          asyncImpl("V", Promise.all(loadingFGPromises));
        }

        if (profile === 2 || profile === 3) {
          // Use setTimeout to make sure our profiling enter/leave stack is not unpaired.
          setTimeout(function () {
            stopAndSaveTimeline();
          }, 0);
        }
    };

    Native["com/sun/midp/lcdui/DisplayDeviceAccess.vibrate0.(IZ)Z"] = function(addr, displayId, on) {
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDeviceAccess.isBacklightSupported0.(I)Z"] = function(addr, displayId) {
        return 1;
    };

    var refreshStr = "refresh";
    Native["com/sun/midp/lcdui/DisplayDevice.refresh0.(IIIIII)V"] = function(addr, hardwareId, displayId, x1, y1, x2, y2) {
        x1 = Math.max(0, x1);
        y1 = Math.max(0, y1);
        x2 = Math.max(0, x2);
        y2 = Math.max(0, y2);

        var maxX = Math.min(offscreenCanvas.width, MIDP.deviceContext.canvas.width);
        x1 = Math.min(maxX, x1);
        x2 = Math.min(maxX, x2);

        var maxY = Math.min(offscreenCanvas.height, MIDP.deviceContext.canvas.height);
        y1 = Math.min(maxY, y1);
        y2 = Math.min(maxY, y2);

        var width = x2 - x1;
        var height = y2 - y1;
        if (width <= 0 || height <= 0) {
            return;
        }

        var ctx = $.ctx;
        window.requestAnimationFrame(function() {
            MIDP.deviceContext.drawImage(offscreenCanvas, x1, y1, width, height, x1, y1, width, height);
            var thread = ctx.nativeThread;
            // The caller's |pc| is currently at the invoke bytecode, we need to skip over the invoke when resuming.
            thread.advancePastInvokeBytecode();
            J2ME.Scheduler.enqueue(ctx);
        });
        $.pause(refreshStr);
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

    function initImageData(imageDataAddr, width, height, isMutable) {
        var canvas = document.createElement("canvas");
        canvas.width = width;
        canvas.height = height;

        var contextInfo = new ContextInfo(canvas.getContext("2d"))
        setNative(imageDataAddr, contextInfo);

        var imageData = getHandle(imageDataAddr);

        imageData.width = width;
        imageData.height = height;

        imageData.isMutable = isMutable;

        return contextInfo.context;
    }

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeImage.(Ljavax/microedition/lcdui/ImageData;[BII)V"] =
    function(addr, imageDataAddr, bytesAddr, offset, length) {
        var bytes = J2ME.getArrayFromAddr(bytesAddr);
        var ctx = $.ctx;
        asyncImpl("V", new Promise(function(resolve, reject) {
            var blob = new Blob([bytes.subarray(offset, offset + length)], { type: "image/png" });
            var img = new Image();
            img.src = URL.createObjectURL(blob);
            img.onload = function() {
                var context = initImageData(imageDataAddr, img.naturalWidth, img.naturalHeight, 0);
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
    function(addr, dataDestAddr, dataSourceAddr, x, y, width, height, transform, isMutable) {
        var context = initImageData(dataDestAddr, width, height, isMutable);
        renderRegion(context, NativeMap.get(dataSourceAddr).context.canvas, x, y, width, height, transform, 0, 0, TOP|LEFT);
    };

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDataCopy.(Ljavax/microedition/lcdui/ImageData;Ljavax/microedition/lcdui/ImageData;)V"] =
    function(addr, destAddr, sourceAddr) {
        var sourceCanvas = NativeMap.get(sourceAddr).context.canvas;
        var context = initImageData(destAddr, sourceCanvas.width, sourceCanvas.height, 0);
        context.drawImage(sourceCanvas, 0, 0);
    };

    Native["javax/microedition/lcdui/ImageDataFactory.createMutableImageData.(Ljavax/microedition/lcdui/ImageData;II)V"] =
    function(addr, imageDataAddr, width, height) {
        var context = initImageData(imageDataAddr, width, height, 1);
        context.fillStyle = "rgb(255,255,255)"; // white
        context.fillRect(0, 0, width, height);
    };

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeRGBImage.(Ljavax/microedition/lcdui/ImageData;[IIIZ)V"] =
    function(addr, imageDataAddr, rgbDataAddr, width, height, processAlpha) {
        var rgbData = J2ME.getArrayFromAddr(rgbDataAddr);
        var context = initImageData(imageDataAddr, width, height, 0);
        var ctxImageData = context.createImageData(width, height);
        var abgrData = new Int32Array(ctxImageData.data.buffer);

        if (1 === processAlpha) {
            ARGBToABGR(rgbData, abgrData, width, height, 0, width);
        } else {
            ARGBTo1BGR(rgbData, abgrData, width, height, 0, width);
        }

        context.putImageData(ctxImageData, 0, 0);
    };

    Native["javax/microedition/lcdui/ImageData.getRGB.([IIIIIII)V"] =
    function(addr, rgbDataAddr, offset, scanlength, x, y, width, height) {
        var rgbData = J2ME.getArrayFromAddr(rgbDataAddr);
        var abgrData = new Int32Array(NativeMap.get(addr).context.getImageData(x, y, width, height).data.buffer);
        ABGRToARGB(abgrData, rgbData, width, height, offset, scanlength);
    };

    Native["com/nokia/mid/ui/DirectUtils.makeMutable.(Ljavax/microedition/lcdui/Image;)V"] = function(addr, imageAddr) {
        var imageData = getHandle(getHandle(imageAddr).imageData);
        imageData.isMutable = 1;
    };

    Native["com/nokia/mid/ui/DirectUtils.setPixels.(Ljavax/microedition/lcdui/Image;I)V"] = function(addr, imageAddr, argb) {
        var image = getHandle(imageAddr);
        var width = image.width;
        var height = image.height;

        // NOTE: This function will only ever be called by the variants
        // of `DirectUtils.createImage`. We don't have to worry about
        // the dimensions or the context info because this `Image` and
        // this `ImageData` were just created; nothing can be out of
        // sync yet.
        var ctx = NativeMap.get(image.imageData).context;

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

    Native["javax/microedition/lcdui/Font.init.(III)V"] = function(addr, face, style, size) {
        var self = getHandle(addr);
        var defaultSize = config.fontSize ? config.fontSize : Math.max(19, (offscreenCanvas.height / 35) | 0);
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

        self.baseline = size | 0;
        self.height = (size * 1.3) | 0;

        var context = document.createElement("canvas").getContext("2d");
        setNative(addr, context);
        context.canvas.width = 0;
        context.canvas.height = 0;
        context.font = style + size + "px " + face;

        // This is a custom property that we set on the context so we can
        // access it in natives.  Note the difference between this value,
        // which represents the size of the font in pixels, and the Font.size
        // field, which stores one of the three font size bit mask constants
        // (SIZE_SMALL, SIZE_MEDIUM, SIZE_LARGE).
        context.fontSize = size;
    };

    function calcStringWidth(fontContext, str) {
        var emojiLen = 0;

        var len = fontContext.measureText(str.replace(emoji.regEx, function() {
            emojiLen += fontContext.fontSize;
            return "";
        })).width | 0;

        return len + emojiLen;
    }

    var defaultFontAddress;
    function getDefaultFontAddress() {
        if (!defaultFontAddress) {
            var classInfo = CLASSES.loadClass("javax/microedition/lcdui/Font");
            defaultFontAddress = J2ME.allocUncollectableObject(classInfo);
            var methodInfo = classInfo.getMethodByNameString("<init>", "(III)V", false);
            J2ME.preemptionLockLevel++;
            J2ME.getLinkedMethod(methodInfo)(defaultFontAddress, 0, 0, 0);
            release || J2ME.Debug.assert(!U, "Unexpected unwind during createException.");
            J2ME.preemptionLockLevel--;
        }
        return defaultFontAddress;
    }

    Native["javax/microedition/lcdui/Font.getDefaultFont.()Ljavax/microedition/lcdui/Font;"] = function(addr) {
        return getDefaultFontAddress();
    };

    Native["javax/microedition/lcdui/Font.stringWidth.(Ljava/lang/String;)I"] = function(addr, strAddr) {
        var fontContext = NativeMap.get(addr);
        return calcStringWidth(fontContext, J2ME.fromStringAddr(strAddr));
    };

    Native["javax/microedition/lcdui/Font.charWidth.(C)I"] = function(addr, char) {
        var fontContext = NativeMap.get(addr);
        return fontContext.measureText(String.fromCharCode(char)).width | 0;
    };

    Native["javax/microedition/lcdui/Font.charsWidth.([CII)I"] = function(addr, charsAddr, offset, len) {
        var fontContext = NativeMap.get(addr);
        return calcStringWidth(fontContext, J2ME.fromJavaChars(charsAddr, offset, len));
    };

    Native["javax/microedition/lcdui/Font.substringWidth.(Ljava/lang/String;II)I"] = function(addr, strAddr, offset, len) {
        var fontContext = NativeMap.get(addr);
        return calcStringWidth(fontContext, J2ME.fromStringAddr(strAddr).slice(offset, offset + len));
    };

    var HCENTER = 1;
    var VCENTER = 2;
    var LEFT = 4;
    var RIGHT = 8;
    var TOP = 16;
    var BOTTOM = 32;
    var BASELINE = 64;

    function withTextAnchor(c, fontContext, anchor, x, str) {
        if (anchor & RIGHT || anchor & HCENTER) {
            var w = calcStringWidth(fontContext, str);

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

    Native["javax/microedition/lcdui/Graphics.getDisplayColor.(I)I"] = function(addr, color) {
        return color & 0x00FFFFFF;
    };

    Native["javax/microedition/lcdui/Graphics.resetGC.()V"] = function(addr) {
        NativeMap.get(addr).resetGC();
    };

    Native["javax/microedition/lcdui/Graphics.reset.(IIII)V"] = function(addr, x1, y1, x2, y2) {
        NativeMap.get(addr).reset(x1, y1, x2, y2);
    };

    Native["javax/microedition/lcdui/Graphics.reset.()V"] = function(addr) {
        var info = NativeMap.get(addr);
        info.reset(0, 0, info.contextInfo.context.canvas.width, info.contextInfo.context.canvas.height);
    };

    Native["javax/microedition/lcdui/Graphics.copyArea.(IIIIIII)V"] = function(addr, x_src, y_src, width, height, x_dest, y_dest, anchor) {
        var self = getHandle(addr);
        if (isScreenGraphics(self)) {
            throw $.newIllegalStateException();
        }
        console.warn("javax/microedition/lcdui/Graphics.copyArea.(IIIIIII)V not implemented");
    };

    Native["javax/microedition/lcdui/Graphics.setDimensions.(II)V"] = function(addr, w, h) {
        NativeMap.get(addr).resetNonGC(0, 0, w, h);
    };

    Native["javax/microedition/lcdui/Graphics.translate.(II)V"] = function(addr, x, y) {
        NativeMap.get(addr).translate(x, y);
    };

    Native["javax/microedition/lcdui/Graphics.getTranslateX.()I"] = function(addr) {
        return NativeMap.get(addr).transX;
    };

    Native["javax/microedition/lcdui/Graphics.getTranslateY.()I"] = function(addr) {
        return NativeMap.get(addr).transY;
    };

    Native["javax/microedition/lcdui/Graphics.getMaxWidth.()S"] = function(addr) {
        return NativeMap.get(addr).contextInfo.context.canvas.width;
    };

    Native["javax/microedition/lcdui/Graphics.getMaxHeight.()S"] = function(addr) {
        return NativeMap.get(addr).contextInfo.context.canvas.height;
    };

    Native["javax/microedition/lcdui/Graphics.getCreator.()Ljava/lang/Object;"] = function(addr) {
        var self = getHandle(addr);
        return self.creator;
    };

    Native["javax/microedition/lcdui/Graphics.setCreator.(Ljava/lang/Object;)V"] = function(addr, creatorAddr) {
        var self = getHandle(addr);
        // Per the original, non-native implementation of this method,
        // ignore repeated attempts to set creator.
        if (self.creator === J2ME.Constants.NULL) {
            self.creator = creatorAddr;
        }
    };

    Native["javax/microedition/lcdui/Graphics.getColor.()I"] = function(addr) {
        var info = NativeMap.get(addr);
        return (info.red << 16) | (info.green << 8) | info.blue;
    };

    Native["javax/microedition/lcdui/Graphics.getRedComponent.()I"] = function(addr) {
        return NativeMap.get(addr).red;
    };

    Native["javax/microedition/lcdui/Graphics.getGreenComponent.()I"] = function(addr) {
        return NativeMap.get(addr).green;
    };

    Native["javax/microedition/lcdui/Graphics.getBlueComponent.()I"] = function(addr) {
        return NativeMap.get(addr).blue;
    };

    Native["javax/microedition/lcdui/Graphics.getGrayScale.()I"] = function(addr) {
        var info = NativeMap.get(addr);
        return (info.red*76 + info.green*150 + info.blue*29) >>> 8;
    };

    Native["javax/microedition/lcdui/Graphics.setColor.(III)V"] = function(addr, red, green, blue) {
        if ((red < 0)   || (red > 255)
            || (green < 0) || (green > 255)
            || (blue < 0)  || (blue > 255)) {
            throw $.newIllegalArgumentException("Value out of range");
        }

        NativeMap.get(addr).setPixel(0xFF, red, green, blue);
    };

    Native["javax/microedition/lcdui/Graphics.setColor.(I)V"] = function(addr, rgb) {
        var red = (rgb >>> 16) & 0xFF;
        var green = (rgb >>> 8) & 0xFF;
        var blue = rgb & 0xFF;

        // NOTE: One would probably expect that `Graphics.setColor`
        // would always set the alpha value to 0xFF but that is not
        // the case if the current RGB value is the same as
        // value being set. This is the behavior
        // of the reference implementation so we are copying
        // that behavior.
        var info = NativeMap.get(addr);
        if (red != info.red || green != info.green || blue != info.blue) {
            info.setPixel(0xFF, red, green, blue);
        }
    };

    Native["javax/microedition/lcdui/Graphics.setGrayScale.(I)V"] = function(addr, value) {
        if ((value < 0) || (value > 255)) {
            throw $.newIllegalArgumentException("Gray value out of range");
        }

        // NOTE: One would probably expect that `Graphics.setGrayScale`
        // would always set the alpha value to 0xFF but that is not
        // the case if the red, green, and blue color values are
        // the same as the values being set. This is the behavior
        // of the reference implementation so we are copying
        // that behavior.
        var info = NativeMap.get(addr);
        if (value != info.red || value != info.green || value != info.blue) {
            info.setPixel(0xFF, value, value, value);
        }
    };

    Native["javax/microedition/lcdui/Graphics.getFont.()Ljavax/microedition/lcdui/Font;"] = function(addr) {
        return NativeMap.get(addr).currentFont;
    };

    Native["javax/microedition/lcdui/Graphics.setFont.(Ljavax/microedition/lcdui/Font;)V"] = function(addr, fontAddr) {
        NativeMap.get(addr).setFont(fontAddr);
    };

    var SOLID = 0;
    var DOTTED = 1;
    Native["javax/microedition/lcdui/Graphics.setStrokeStyle.(I)V"] = function(addr, style) {
        if ((style !== SOLID) && (style !== DOTTED)) {
            throw $.newIllegalArgumentException("Invalid stroke style");
        }

        // We don't actually implement DOTTED style so this is a no-op
    };

    Native["javax/microedition/lcdui/Graphics.getStrokeStyle.()I"] = function(addr) {
        return SOLID;
    };

    Native["javax/microedition/lcdui/Graphics.getClipX.()I"] = function(addr) {
        var info = NativeMap.get(addr);
        return info.clipX1 - info.transX;
    };

    Native["javax/microedition/lcdui/Graphics.getClipY.()I"] = function(addr) {
        var info = NativeMap.get(addr);
        return info.clipY1 - info.transY;
    };

    Native["javax/microedition/lcdui/Graphics.getClipWidth.()I"] = function(addr) {
        var info = NativeMap.get(addr);
        return info.clipX2 - info.clipX1;
    };

    Native["javax/microedition/lcdui/Graphics.getClipHeight.()I"] = function(addr) {
        var info = NativeMap.get(addr);
        return info.clipY2 - info.clipY1;
    };

    Native["javax/microedition/lcdui/Graphics.getClip.([I)V"] = function(addr, regionAddr) {
        var region = J2ME.getArrayFromAddr(regionAddr);
        var info = NativeMap.get(addr);
        region[0] = info.clipX1 - info.transX;
        region[1] = info.clipY1 - info.transY;
        region[2] = info.clipX2 - info.transX;
        region[3] = info.clipY2 - info.transY;
    };

    Native["javax/microedition/lcdui/Graphics.clipRect.(IIII)V"] = function(addr, x, y, width, height) {
        var info = NativeMap.get(addr);
        info.setClip(x, y, width, height, info.clipX1, info.clipY1, info.clipX2, info.clipY2);
    };

    // DirectGraphics constants
    var TYPE_USHORT_4444_ARGB = 4444;
    var TYPE_USHORT_565_RGB = 565;

    Native["com/nokia/mid/ui/DirectGraphicsImp.setARGBColor.(I)V"] = function(addr, argb) {
        var self = getHandle(addr);
        var alpha = (argb >>> 24);
        var red = (argb >>> 16) & 0xFF;
        var green = (argb >>> 8) & 0xFF;
        var blue = argb & 0xFF;
        NativeMap.get(self.graphics).setPixel(alpha, red, green, blue);
    };

    Native["com/nokia/mid/ui/DirectGraphicsImp.getAlphaComponent.()I"] = function(addr) {
        var self = getHandle(addr);
        return NativeMap.get(self.graphics).alpha;
    };

    Native["com/nokia/mid/ui/DirectGraphicsImp.getPixels.([SIIIIIII)V"] =
    function(addr, pixelsAddr, offset, scanlength, x, y, width, height, format) {
        var self = getHandle(addr);
        var pixels = J2ME.getArrayFromAddr(pixelsAddr);

        if (!pixels) {
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

        var context = NativeMap.get(self.graphics).contextInfo.context;
        var abgrData = new Int32Array(context.getImageData(x, y, width, height).data.buffer);
        converterFunc(abgrData, pixels, width, height, offset, scanlength);
    };

    Native["com/nokia/mid/ui/DirectGraphicsImp.drawPixels.([SZIIIIIIII)V"] =
    function(addr, pixelsAddr, transparency, offset, scanlength, x, y, width, height, manipulation, format) {
        var self = getHandle(addr);
        var pixels = J2ME.getArrayFromAddr(pixelsAddr);

        if (!pixels) {
            throw $.newNullPointerException("Pixels array is null");
        }

        var converterFunc = null;
        if (format === TYPE_USHORT_4444_ARGB && transparency && !manipulation) {
            converterFunc = ARGB4444ToABGR;
        } else {
            throw $.newIllegalArgumentException("Format unsupported");
        }

        tempContext.canvas.width = width;
        tempContext.canvas.height = height;
        var imageData = tempContext.createImageData(width, height);
        var abgrData = new Int32Array(imageData.data.buffer);

        converterFunc(pixels, abgrData, width, height, offset, scanlength);

        tempContext.putImageData(imageData, 0, 0);

        var c = NativeMap.get(self.graphics).getGraphicsContext();

        c.drawImage(tempContext.canvas, x, y);
        tempContext.canvas.width = 0;
        tempContext.canvas.height = 0;
    };

    Native["javax/microedition/lcdui/Graphics.render.(Ljavax/microedition/lcdui/Image;III)Z"] =
    function(addr, imageAddr, x, y, anchor) {
        var image = getHandle(imageAddr);
        renderRegion(NativeMap.get(addr).getGraphicsContext(), NativeMap.get(image.imageData).context.canvas,
                     0, 0, image.width, image.height, TRANS_NONE, x, y, anchor);
        return 1;
    };

    Native["javax/microedition/lcdui/Graphics.drawRegion.(Ljavax/microedition/lcdui/Image;IIIIIIII)V"] =
    function(addr, srcAddr, x_src, y_src, width, height, transform, x_dest, y_dest, anchor) {
        if (srcAddr === J2ME.Constants.NULL) {
            throw $.newNullPointerException("src image is null");
        }

        var src = getHandle(srcAddr);
        renderRegion(NativeMap.get(addr).getGraphicsContext(), NativeMap.get(src.imageData).context.canvas,
                     x_src, y_src, width, height, transform, x_dest, y_dest, anchor);
    };

    Native["javax/microedition/lcdui/Graphics.drawImage.(Ljavax/microedition/lcdui/Image;III)V"] =
    function(addr, imageAddr, x, y, anchor) {
        if (imageAddr === J2ME.Constants.NULL) {
            throw $.newNullPointerException("image is null");
        }

        var image = getHandle(imageAddr);
        var imageData = getHandle(image.imageData);
        renderRegion(NativeMap.get(addr).getGraphicsContext(), NativeMap.get(image.imageData).context.canvas,
                     0, 0, imageData.width, imageData.height, TRANS_NONE, x, y, anchor);
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
        this.currentFont = getDefaultFontAddress();
        this.alpha = 0xFF;
        this.red = 0x00;
        this.green = 0x00;
        this.blue = 0x00;
    }

    GraphicsInfo.prototype.setFont = function(font) {
        if (J2ME.Constants.NULL === font) {
            font = getDefaultFontAddress();
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
        this.setFont(J2ME.Constants.NULL);
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
        this.context.font = NativeMap.get(graphicsInfo.currentFont).font;

        this.context.beginPath();
        this.context.rect(graphicsInfo.clipX1, graphicsInfo.clipY1, graphicsInfo.clipX2 - graphicsInfo.clipX1, graphicsInfo.clipY2 - graphicsInfo.clipY1);
        this.context.clip();
        this.context.translate(graphicsInfo.transX, graphicsInfo.transY);

        this.currentlyAppliedGraphicsInfo = graphicsInfo;
    };

    Native["javax/microedition/lcdui/Graphics.initScreen0.(I)V"] = function(addr, displayId) {
        var self = getHandle(addr);
        self.displayId = displayId;
        setNative(addr, new GraphicsInfo(screenContextInfo));
        self.creator = J2ME.Constants.NULL;
    };

    Native["javax/microedition/lcdui/Graphics.initImage0.(Ljavax/microedition/lcdui/Image;)V"] =
    function(addr, imgAddr) {
        var self = getHandle(addr);
        var img = getHandle(imgAddr);
        self.displayId = -1;
        setNative(addr, new GraphicsInfo(NativeMap.get(img.imageData)));
        self.creator = J2ME.Constants.NULL;
    };

    function isScreenGraphics(g) {
        return g.displayId !== -1;
    }

    Native["javax/microedition/lcdui/Graphics.setClip.(IIII)V"] = function(addr, x, y, w, h) {
        var info = NativeMap.get(addr);
        info.setClip(x, y, w, h, 0, 0, info.contextInfo.context.canvas.width, info.contextInfo.context.canvas.height);
    };

    function drawString(info, str, x, y, anchor) {
        var c = info.getGraphicsContext();
        var fontContext = NativeMap.get(info.currentFont);
        var fontSize = fontContext.fontSize;

        var finalText;
        if (!emoji.regEx.test(str)) {
            // No emojis are present.
            finalText = str;
        } else {
            // Emojis are present. Handle all the text up to the last emoji.
            var match;
            var lastIndex = 0;
            emoji.regEx.lastIndex = 0;
            while (match = emoji.regEx.exec(str)) {
                var text = str.substring(lastIndex, match.index);
                var match0 = match[0];
                lastIndex = match.index + match0.length;

                var textX = withTextAnchor(c, fontContext, anchor, x, text);

                c.fillText(text, textX, y);

                // Calculate the string width.
                x += c.measureText(text).width | 0;

                var emojiData = emoji.getData(match0, fontSize);
                c.drawImage(emojiData.img, emojiData.x, 0, emoji.squareSize, emoji.squareSize, x, y, fontSize, fontSize);
                x += fontSize;
            }
            finalText = str.substring(lastIndex);
        }

        // Now handle all the text after the final emoji. If there were no
        // emojis present, this is the entire string.
        if (finalText) {
            var textX = withTextAnchor(c, fontContext, anchor, x, finalText);
            c.fillText(finalText, textX, y);
        }
    }

    Native["javax/microedition/lcdui/Graphics.drawString.(Ljava/lang/String;III)V"] =
    function(addr, strAddr, x, y, anchor) {
        drawString(NativeMap.get(addr), J2ME.fromStringAddr(strAddr), x, y, anchor);
    };

    Native["javax/microedition/lcdui/Graphics.drawSubstring.(Ljava/lang/String;IIIII)V"] =
    function(addr, strAddr, offset, len, x, y, anchor) {
        drawString(NativeMap.get(addr), J2ME.fromStringAddr(strAddr).substr(offset, len), x, y, anchor);
    };

    Native["javax/microedition/lcdui/Graphics.drawChars.([CIIIII)V"] =
    function(addr, dataAddr, offset, len, x, y, anchor) {
        drawString(NativeMap.get(addr), J2ME.fromJavaChars(dataAddr, offset, len), x, y, anchor);
    };

    Native["javax/microedition/lcdui/Graphics.drawChar.(CIII)V"] = function(addr, jChr, x, y, anchor) {
        var chr = String.fromCharCode(jChr);
        var info = NativeMap.get(addr);

        var c = info.getGraphicsContext();

        x = withTextAnchor(c, NativeMap.get(info.currentFont), anchor, x, chr);

        c.fillText(chr, x, y);
    };

    Native["javax/microedition/lcdui/Graphics.fillTriangle.(IIIIII)V"] = function(addr, x1, y1, x2, y2, x3, y3) {
        var c = NativeMap.get(addr).getGraphicsContext();

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

    Native["javax/microedition/lcdui/Graphics.drawRect.(IIII)V"] = function(addr, x, y, w, h) {
        if (w < 0 || h < 0) {
            return;
        }

        var c = NativeMap.get(addr).getGraphicsContext();

        w = w || 1;
        h = h || 1;

        c.strokeRect(x, y, w, h);
    };

    Native["javax/microedition/lcdui/Graphics.drawRoundRect.(IIIIII)V"] = function(addr, x, y, w, h, arcWidth, arcHeight) {
        if (w < 0 || h < 0) {
            return;
        }

        var c = NativeMap.get(addr).getGraphicsContext();

        w = w || 1;
        h = h || 1;

        c.beginPath();
        createRoundRect(c, x, y, w, h, arcWidth, arcHeight);
        c.stroke();
    };

    Native["javax/microedition/lcdui/Graphics.fillRect.(IIII)V"] = function(addr, x, y, w, h) {
        if (w <= 0 || h <= 0) {
            return;
        }

        var c = NativeMap.get(addr).getGraphicsContext();

        w = w || 1;
        h = h || 1;

        c.fillRect(x, y, w, h);
    };

    Native["javax/microedition/lcdui/Graphics.fillRoundRect.(IIIIII)V"] = function(addr, x, y, w, h, arcWidth, arcHeight) {
        if (w <= 0 || h <= 0) {
            return;
        }

        var c = NativeMap.get(addr).getGraphicsContext();

        w = w || 1;
        h = h || 1;

        c.beginPath();
        createRoundRect(c, x, y, w, h, arcWidth, arcHeight);
        c.fill();
    };

    Native["javax/microedition/lcdui/Graphics.drawArc.(IIIIII)V"] = function(addr, x, y, width, height, startAngle, arcAngle) {
        if (width < 0 || height < 0) {
            return;
        }

        var c = NativeMap.get(addr).getGraphicsContext();

        var endRad = -startAngle * 0.0175;
        var startRad = endRad - arcAngle * 0.0175;
        c.beginPath();
        createEllipticalArc(c, x, y, width / 2, height / 2, startRad, endRad, false);
        c.stroke();
    };

    Native["javax/microedition/lcdui/Graphics.fillArc.(IIIIII)V"] = function(addr, x, y, width, height, startAngle, arcAngle) {
        if (width <= 0 || height <= 0) {
            return;
        }

        var c = NativeMap.get(addr).getGraphicsContext();

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

    Native["javax/microedition/lcdui/Graphics.drawLine.(IIII)V"] = function(addr, x1, y1, x2, y2) {
        var c = NativeMap.get(addr).getGraphicsContext();

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
    function(addr, rgbDataAddr, offset, scanlength, x, y, width, height, processAlpha) {
        var rgbData = J2ME.getArrayFromAddr(rgbDataAddr);
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

        var c = NativeMap.get(addr).getGraphicsContext();

        c.drawImage(tempContext.canvas, x, y);
        tempContext.canvas.width = 0;
        tempContext.canvas.height = 0;
    };

    var textEditorId = 0,
        textEditorResolve = null,
        dirtyEditors = [];

    function wakeTextEditorThread(textEditorAddr) {
        dirtyEditors.push(textEditorAddr);
        if (textEditorResolve) {
            textEditorResolve();
            textEditorResolve = null;
        }
    }

    function getTextEditorCaretPosition(nativeTextEditor, textEditor) {
        if (nativeTextEditor.isAttached()) {
            return nativeTextEditor.getSelectionStart();
        }
        if (textEditor.caretPosition !== null) {
            return textEditor.caretPosition;
        }
        return 0;
    }

    function setTextEditorCaretPosition(nativeTextEditor, textEditor, index) {
        if (nativeTextEditor.isAttached()) {
            nativeTextEditor.setSelectionRange(index, index);
        } else {
            textEditor.caretPosition = index;
        }
    };

    Native["com/nokia/mid/ui/TextEditor.init.(Ljava/lang/String;IIII)V"] =
    function(addr, textAddr, maxSize, constraints, width, height) {
        var self = getHandle(addr);

        if (constraints !== 0) {
            console.warn("TextEditor.constraints not implemented");
        }

        var textEditor = TextEditorProvider.getEditor(constraints, null, ++textEditorId);
        setNative(addr, textEditor);
        textEditor.setBackgroundColor(0xFFFFFFFF | 0); // opaque white
        textEditor.setForegroundColor(0xFF000000 | 0); // opaque black

        textEditor.setAttribute("maxlength", maxSize);
        textEditor.setSize(width, height);
        textEditor.setVisible(false);
        var font = getHandle(self.font);
        textEditor.setFont(font);

        textEditor.setContent(J2ME.fromStringAddr(textAddr));
        setTextEditorCaretPosition(textEditor, self, textEditor.getContentSize());

        textEditor.oninput(function(e) {
            wakeTextEditorThread(addr);
        });
    };

    Native["com/nokia/mid/ui/CanvasItem.attachNativeImpl.()V"] = function(addr) {
        var self = getHandle(addr);
        var textEditor = NativeMap.get(addr);
        if (textEditor) {
            textEditor.attach();
            if (self.caretPosition !== 0) {
                textEditor.setSelectionRange(self.caretPosition, self.caretPosition);
                self.caretPosition = null;
            }
        }
    };

    Native["com/nokia/mid/ui/CanvasItem.detachNativeImpl.()V"] = function(addr) {
        var self = getHandle(addr);
        var textEditor = NativeMap.get(addr);
        if (textEditor) {
            self.caretPosition = textEditor.getSelectionStart();
            textEditor.detach();
        }
    };

    Native["javax/microedition/lcdui/Display.setTitle.(Ljava/lang/String;)V"] = function(addr, titleAddr) {
        document.getElementById("display_title").textContent = J2ME.fromStringAddr(titleAddr);
    };

    Native["com/nokia/mid/ui/CanvasItem.setSize.(II)V"] = function(addr, width, height) {
        NativeMap.get(addr).setSize(width, height);
    };

    Native["com/nokia/mid/ui/CanvasItem.setVisible.(Z)V"] = function(addr, visible) {
        NativeMap.get(addr).setVisible(visible ? true : false);
    };

    Native["com/nokia/mid/ui/CanvasItem.getWidth.()I"] = function(addr) {
        return NativeMap.get(addr).getWidth();
    };

    Native["com/nokia/mid/ui/CanvasItem.getHeight.()I"] = function(addr) {
        return NativeMap.get(addr).getHeight();
    };

    Native["com/nokia/mid/ui/CanvasItem.setPosition0.(II)V"] = function(addr, x, y) {
        NativeMap.get(addr).setPosition(x, y);
    };

    Native["com/nokia/mid/ui/CanvasItem.getPositionX.()I"] = function(addr) {
        return NativeMap.get(addr).getLeft();
    };

    Native["com/nokia/mid/ui/CanvasItem.getPositionY.()I"] = function(addr) {
        return NativeMap.get(addr).getTop();
    };

    Native["com/nokia/mid/ui/CanvasItem.isVisible.()Z"] = function(addr) {
        return NativeMap.get(addr).visible ? 1 : 0;
    };

    Native["com/nokia/mid/ui/TextEditor.setConstraints.(I)V"] = function(addr, constraints) {
        var textEditor = NativeMap.get(addr);
        setNative(addr, TextEditorProvider.getEditor(constraints, textEditor, textEditor.id));
    };

    Native["com/nokia/mid/ui/TextEditor.getConstraints.()I"] = function(addr) {
        return NativeMap.get(addr).constraints;
    };

    Native["com/nokia/mid/ui/TextEditor.setFocus.(Z)V"] = function(addr, shouldFocus) {
        var textEditor = NativeMap.get(addr);
        var promise;
        if (shouldFocus && (currentlyFocusedTextEditor !== textEditor)) {
            promise = textEditor.focus();
            currentlyFocusedTextEditor = textEditor;
        } else if (!shouldFocus && (currentlyFocusedTextEditor === textEditor)) {
            promise = textEditor.blur();
            currentlyFocusedTextEditor = null;
        } else {
            return;
        }
        asyncImpl("V", promise);
    };

    Native["com/nokia/mid/ui/TextEditor.hasFocus.()Z"] = function(addr) {
        return (NativeMap.get(addr) === currentlyFocusedTextEditor) ? 1 : 0;
    };

    Native["com/nokia/mid/ui/TextEditor.setCaret.(I)V"] = function(addr, index) {
        var self = getHandle(addr);
        var textEditor = NativeMap.get(addr);

        if (index < 0 || index > textEditor.getContentSize()) {
            throw $.newStringIndexOutOfBoundsException();
        }

        setTextEditorCaretPosition(textEditor, self, index);
    };

    Native["com/nokia/mid/ui/TextEditor.getCaretPosition.()I"] = function(addr) {
        var self = getHandle(addr);
        var nativeTextEditor = NativeMap.get(addr);
        return getTextEditorCaretPosition(nativeTextEditor, self);
    };

    Native["com/nokia/mid/ui/TextEditor.getBackgroundColor.()I"] = function(addr) {
        return NativeMap.get(addr).getBackgroundColor();
    };
    Native["com/nokia/mid/ui/TextEditor.getForegroundColor.()I"] = function(addr) {
        return NativeMap.get(addr).getForegroundColor();
    };
    Native["com/nokia/mid/ui/TextEditor.setBackgroundColor.(I)V"] = function(addr, backgroundColor) {
        NativeMap.get(addr).setBackgroundColor(backgroundColor);
    };
    Native["com/nokia/mid/ui/TextEditor.setForegroundColor.(I)V"] = function(addr, foregroundColor) {
        NativeMap.get(addr).setForegroundColor(foregroundColor);
    };

    Native["com/nokia/mid/ui/TextEditor.getContent.()Ljava/lang/String;"] = function(addr) {
        return J2ME.newString(NativeMap.get(addr).getContent());
    };

    Native["com/nokia/mid/ui/TextEditor.setContent.(Ljava/lang/String;)V"] = function(addr, contentAddr) {
        var self = getHandle(addr);
        var nativeTextEditor = NativeMap.get(addr);
        var content = J2ME.fromStringAddr(contentAddr);
        nativeTextEditor.setContent(content);
        setTextEditorCaretPosition(nativeTextEditor, self, nativeTextEditor.getContentSize());
    };

    addUnimplementedNative("com/nokia/mid/ui/TextEditor.getLineMarginHeight.()I", 0);
    addUnimplementedNative("com/nokia/mid/ui/TextEditor.getVisibleContentPosition.()I", 0);

    Native["com/nokia/mid/ui/TextEditor.getContentHeight.()I"] = function(addr) {
        return NativeMap.get(addr).getContentHeight();
    };

    Native["com/nokia/mid/ui/TextEditor.insert.(Ljava/lang/String;I)V"] = function(addr, textAddr, pos) {
        var self = getHandle(addr);
        var nativeTextEditor = NativeMap.get(addr);
        var text = J2ME.fromStringAddr(textAddr);
        var len = util.toCodePointArray(text).length;
        if (nativeTextEditor.getContentSize() + len > nativeTextEditor.getAttribute("maxlength")) {
            throw $.newIllegalArgumentException();
        }
        nativeTextEditor.setContent(nativeTextEditor.getSlice(0, pos) + text + nativeTextEditor.getSlice(pos));
        setTextEditorCaretPosition(nativeTextEditor, self, pos + len);
    };

    Native["com/nokia/mid/ui/TextEditor.delete.(II)V"] = function(addr, offset, length) {
        var self = getHandle(addr);
        var nativeTextEditor = NativeMap.get(addr);
        var old = nativeTextEditor.getContent();

        var size = nativeTextEditor.getContentSize();
        if (offset < 0 || offset > size || length < 0 || offset + length > size) {
            throw $.newStringIndexOutOfBoundsException("offset/length invalid");
        }

        nativeTextEditor.setContent(nativeTextEditor.getSlice(0, offset) + nativeTextEditor.getSlice(offset + length));
        setTextEditorCaretPosition(nativeTextEditor, self, offset);
    };

    Native["com/nokia/mid/ui/TextEditor.getMaxSize.()I"] = function(addr) {
        return parseInt(NativeMap.get(addr).getAttribute("maxlength"));
    };

    Native["com/nokia/mid/ui/TextEditor.setMaxSize.(I)I"] = function(addr, maxSize) {
        var nativeTextEditor = NativeMap.get(addr);
        if (nativeTextEditor.getContentSize() > maxSize) {
            var self = getHandle(addr);
            var nativeTextEditor = NativeMap.get(addr);

            var oldCaretPosition = getTextEditorCaretPosition(nativeTextEditor, self);

            nativeTextEditor.setContent(nativeTextEditor.getSlice(0, maxSize));

            if (oldCaretPosition > maxSize) {
                setTextEditorCaretPosition(nativeTextEditor, self, maxSize);
            }
        }

        nativeTextEditor.setAttribute("maxlength", maxSize);

        // The return value is the assigned size, which could be less than
        // the size that was requested, although in this case we always set it
        // to the requested size.
        return maxSize;
    };

    Native["com/nokia/mid/ui/TextEditor.size.()I"] = function(addr) {
        return NativeMap.get(addr).getContentSize();
    };

    Native["com/nokia/mid/ui/TextEditor.setFont.(Ljavax/microedition/lcdui/Font;)V"] = function(addr, fontAddr) {
        var self = getHandle(addr);
        self.font = fontAddr;
        var nativeTextEditor = NativeMap.get(addr);
        nativeTextEditor.setFont(getHandle(fontAddr));
    };

    Native["com/nokia/mid/ui/TextEditorThread.getNextDirtyEditor.()Lcom/nokia/mid/ui/TextEditor;"] = function(addr) {
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

    Native["javax/microedition/lcdui/DisplayableLFImpl.initialize0.()V"] = function(addr) {
    };

    Native["javax/microedition/lcdui/DisplayableLFImpl.deleteNativeResource0.(I)V"] = function(addr, nativeId) {
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

    Native["javax/microedition/lcdui/DisplayableLFImpl.setTitle0.(ILjava/lang/String;)V"] =
    function(addr, nativeId, titleAddr) {
        document.getElementById("display_title").textContent = J2ME.fromStringAddr(titleAddr);
    };

    Native["javax/microedition/lcdui/CanvasLFImpl.createNativeResource0.(Ljava/lang/String;Ljava/lang/String;)I"] =
    function(addr, titleAddr, tickerAddr) {
        console.warn("javax/microedition/lcdui/CanvasLFImpl.createNativeResource0.(Ljava/lang/String;Ljava/lang/String;)I not implemented");
        curDisplayableId = nextMidpDisplayableId++;
        return curDisplayableId;
    };

    Native["javax/microedition/lcdui/AlertLFImpl.createNativeResource0.(Ljava/lang/String;Ljava/lang/String;I)I"] =
    function(addr, titleAddr, tickerAddr, type) {
        var nativeId = nextMidpDisplayableId++;
        var alertTemplateNode = document.getElementById("lcdui-alert");
        var el = alertTemplateNode.cloneNode(true);
        el.id = "displayable-" + nativeId;
        el.querySelector('h1.title').textContent = J2ME.fromStringAddr(titleAddr);
        alertTemplateNode.parentNode.appendChild(el);

        return nativeId;
    };

    Native["javax/microedition/lcdui/AlertLFImpl.setNativeContents0.(ILjavax/microedition/lcdui/ImageData;[ILjava/lang/String;)Z"] =
    function(addr, nativeId, imgIdAddr, indicatorBoundsAddr, textAddr) {
        var el = document.getElementById("displayable-" + nativeId);
        el.querySelector('p.text').textContent = J2ME.fromStringAddr(textAddr);

        return 0;
    };

    Native["javax/microedition/lcdui/AlertLFImpl.showNativeResource0.(I)V"] = function(addr, nativeId) {
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
    function(addr, ownerId, labelAddr, layout, interactive, maxValue, initialValue) {
        if (labelAddr !== J2ME.Constants.NULL) {
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
    function(addr, ownerId, labelAddr, layout, bufferAddr, constraints, initialInputModeAddr) {
        console.warn("javax/microedition/lcdui/TextFieldLFImpl.createNativeResource0.(ILjava/lang/String;ILcom/sun/midp/lcdui/DynamicCharacterArray;ILjava/lang/String;)I not implemented");
        return nextMidpDisplayableId++;
    };

    Native["javax/microedition/lcdui/ImageItemLFImpl.createNativeResource0.(ILjava/lang/String;ILjavax/microedition/lcdui/ImageData;Ljava/lang/String;I)I"] =
    function(addr, ownerId, labelAddr, layout, imageDataAddr, altTextAddr, appearanceMode) {
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

    Native["javax/microedition/lcdui/ItemLFImpl.setSize0.(III)V"] = function(addr, nativeId, w, h) {
        console.warn("javax/microedition/lcdui/ItemLFImpl.setSize0.(III)V not implemented");
    };

    Native["javax/microedition/lcdui/ItemLFImpl.setLocation0.(III)V"] = function(addr, nativeId, x, y) {
        console.warn("javax/microedition/lcdui/ItemLFImpl.setLocation0.(III)V not implemented");
    };

    Native["javax/microedition/lcdui/ItemLFImpl.show0.(I)V"] = function(addr, nativeId) {
        console.warn("javax/microedition/lcdui/ItemLFImpl.show0.(I)V not implemented");
    };

    Native["javax/microedition/lcdui/ItemLFImpl.hide0.(I)V"] = function(addr, nativeId) {
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
    function(addr, itemCommandsAddr, numItemCommands, commandsAddr, numCommands) {
        if (numItemCommands !== 0) {
            console.error("NativeMenu.updateCommands: item commands not yet supported");
        }

        var el = document.getElementById("displayable-" + curDisplayableId);

        if (!el) {
            document.getElementById("sidebar").querySelector("nav ul").innerHTML = "";
        }

        if (commandsAddr === J2ME.Constants.NULL) {
            return;
        }

        var commands = J2ME.getArrayFromAddr(commandsAddr);

        var validCommands = [];

        for (var i = 0; i < commands.length; i++) {
            if (commands[i]) {
                validCommands.push(getHandle(commands[i]));
            }
        }

        validCommands.sort(function(a, b) {
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
                button.textContent = J2ME.fromStringAddr(command.shortLabel);

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
                var text = J2ME.fromStringAddr(command.shortLabel);
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
