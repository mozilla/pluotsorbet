/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

(function(Native) {
    Native.create("com/sun/midp/lcdui/DisplayDeviceContainer.getDisplayDevicesIds0.()[I", function(ctx) {
        var ids = ctx.newPrimitiveArray("I", 1);
        ids[0] = 1;
        return ids;
    });

    Native.create("com/sun/midp/lcdui/DisplayDevice.getDisplayName0.(I)Ljava/lang/String;", function(ctx, id) {
        return null;
    });

    Native.create("com/sun/midp/lcdui/DisplayDevice.isDisplayPrimary0.(I)Z", function(ctx, id) {
        console.warn("DisplayDevice.isDisplayPrimary0.(I)Z not implemented (" + id + ")");
        return true;
    });

    Native.create("com/sun/midp/lcdui/DisplayDevice.isbuildInDisplay0.(I)Z", function(ctx, id) {
        return true;
    });

    Native.create("com/sun/midp/lcdui/DisplayDevice.getDisplayCapabilities0.(I)I", function(ctx, id) {
        return 0x3ff;
    });

    Native.create("com/sun/midp/lcdui/DisplayDevice.isDisplayPenSupported0.(I)Z", function(ctx, id) {
        return true;
    });

    Native.create("com/sun/midp/lcdui/DisplayDevice.isDisplayPenMotionSupported0.(I)Z", function(ctx, id) {
        return true;
    });

    Native.create("com/sun/midp/lcdui/DisplayDevice.getReverseOrientation0.(I)Z", function(ctx, id) {
        return false;
    });

    Native.create("com/sun/midp/lcdui/DisplayDevice.getScreenWidth0.(I)I", function(ctx, id) {
        return MIDP.Context2D.canvas.width;
    });

    Native.create("com/sun/midp/lcdui/DisplayDevice.getScreenHeight0.(I)I", function(ctx, id) {
        return MIDP.Context2D.canvas.height;
    });

    Native.create("com/sun/midp/lcdui/DisplayDevice.displayStateChanged0.(II)V", function(ctx, hardwareId, state) {
        console.warn("DisplayDevice.displayStateChanged0.(II)V not implemented (" + hardwareId + ", " + state + ")");
    });

    Native.create("com/sun/midp/lcdui/DisplayDevice.setFullScreen0.(IIZ)V", function(ctx, hardwareId, displayId, mode) {
        console.warn("DisplayDevice.setFullScreen0.(IIZ)V not implemented (" +
                     hardwareId + ", " + displayId + ", " + mode + ")");
    });

    Native.create("com/sun/midp/lcdui/DisplayDevice.gainedForeground0.(II)V", function(ctx, hardwareId, displayId) {
        console.warn("DisplayDevice.gainedForeground0.(II)V not implemented (" + hardwareId + ", " + displayId + ")");
    });

    Native.create("com/sun/midp/lcdui/DisplayDeviceAccess.vibrate0.(IZ)Z", function(ctx, displayId, on) {
        return true;
    });

    Native.create("com/sun/midp/lcdui/DisplayDeviceAccess.isBacklightSupported0.(I)Z", function(ctx, displayId) {
        return true;
    });

    Native.create("com/sun/midp/lcdui/DisplayDevice.refresh0.(IIIIII)V", function(ctx, hardwareId, displayId, x1, y1, x2, y2) {
        console.warn("DisplayDevice.refresh0.(IIIIII)V not implemented (" +
                     hardwareId + ", " + displayId + ", " + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")");
    });

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

    Native.create("javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeImage.(Ljavax/microedition/lcdui/ImageData;[BII)V",
    function(ctx, imageData, bytes, offset, length) {
        return new Promise(function(resolve, reject) {
            var blob = new Blob([bytes.subarray(offset, offset + length)], { type: "image/png" });
            var img = new Image();
            img.src = URL.createObjectURL(blob);
            img.onload = function() {
                setImageData(imageData, img.naturalWidth, img.naturalHeight, img);
                resolve();
            }
            img.onerror = function(e) {
                reject(new JavaException("java/lang/IllegalArgumentException", "error decoding image"));
            }
        });
    });

    Native.create("javax/microedition/lcdui/ImageDataFactory.createMutableImageData.(Ljavax/microedition/lcdui/ImageData;II)V",
    function(ctx, imageData, width, height) {
        var context = createContext2d(width, height);
        context.fillStyle = "rgb(255,255,255)"; // white
        context.fillRect(0, 0, width, height);
        setImageData(imageData, width, height, context);
    });

    Native.create("javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeRGBImage.(Ljavax/microedition/lcdui/ImageData;[IIIZ)V",
    function(ctx, imageData, rgbData, width, height, processAlpha) {
        var context = createContext2d(width, height);
        rgbDataToContext(context, rgbData, 0, width, processAlpha ? swapRB : swapRBAndSetAlpha);
        setImageData(imageData, width, height, context);
    });

    Native.create("javax/microedition/lcdui/ImageData.getRGB.([IIIIIII)V", function(ctx, rgbData, offset, scanlength, x, y, width, height) {
        contextToRgbData(convertNativeImageData(this), rgbData, offset, scanlength, x, y, width, height, swapRB);
    });

    Native.create("com/nokia/mid/ui/DirectUtils.makeMutable.(Ljavax/microedition/lcdui/Image;)V", function(ctx, image) {
        var imageData = image.class.getField("I.imageData.Ljavax/microedition/lcdui/ImageData;").get(image);
        imageData.class.getField("I.isMutable.Z").set(imageData, 1);
    });

    Native.create("com/nokia/mid/ui/DirectUtils.setPixels.(Ljavax/microedition/lcdui/Image;I)V", function(ctx, image, argb) {
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
    });

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

    Native.create("javax/microedition/lcdui/Font.init.(III)V", function(ctx, face, style, size) {
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

        this.class.getField("I.baseline.I").set(this, (size/2)|0);
        this.class.getField("I.height.I").set(this, (size * 1.3)|0);
        this.css = style + " " + size + "pt " + face;
    });

    Native.create("javax/microedition/lcdui/Font.stringWidth.(Ljava/lang/String;)I", function(ctx, str) {
        return withFont(this, MIDP.Context2D, util.fromJavaString(str));
    });

    Native.create("javax/microedition/lcdui/Font.charWidth.(C)I", function(ctx, char) {
        return withFont(this, MIDP.Context2D, String.fromCharCode(char));
    });

    Native.create("javax/microedition/lcdui/Font.charsWidth.([CII)I", function(ctx, str, offset, len) {
        return withFont(this, MIDP.Context2D, util.fromJavaChars(str).slice(offset, offset + len));
    });

    Native.create("javax/microedition/lcdui/Font.substringWidth.(Ljava/lang/String;II)I", function(ctx, str, offset, len) {
        return withFont(this, MIDP.Context2D, util.fromJavaString(str).slice(offset, offset + len));
    });

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

    function withFont(font, c, str) {
        c.font = font.css;
        return c.measureText(str).width | 0;
    }

    function withTextAnchor(g, c, anchor, x, y, str, cb) {
        withClip(g, c, x, y, function(x, y) {
            var w = withFont(g.class.getField("I.currentFont.Ljavax/microedition/lcdui/Font;").get(g), c, str);
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

    function renderImage(graphics, image, x, y, anchor) {
        var texture = image.class.getField("I.imageData.Ljavax/microedition/lcdui/ImageData;").get(image).nativeImageData;

        if (!texture) {
            console.warn("Graphics.render: image missing native data");
            return;
        }

        if (texture instanceof CanvasRenderingContext2D) {
            texture = texture.canvas; // Render the canvas, not the context.
        }

        withGraphics(graphics, function(c) {
            withAnchor(graphics, c, anchor, x, y, texture.width, texture.height, function(x, y) {
                c.drawImage(texture, x, y);
            });
        });
    }

    Override.create("com/sun/midp/chameleon/CGraphicsUtil.draw9pcsBackground.(Ljavax/microedition/lcdui/Graphics;IIII[Ljavax/microedition/lcdui/Image;)V",
    function(ctx, g, x, y, w, h, image) {
        if (image == null || image.length != 9) {
            return;
        }

        var transX = g.class.getField("I.transX.I");
        var transY = g.class.getField("I.transY.I");

        transX.set(g, transX.get(g) + x);
        transY.set(g, transY.get(g) + y);

        // Top Border
        var iW = image[1].class.getField("I.width.I").get(image[1]);
        renderImage(g, image[0], 0, 0, LEFT | TOP);
        w -= image[2].class.getField("I.width.I").get(image[2]);
        for (var i = image[0].class.getField("I.width.I").get(image[0]); i < w; i += iW) {
            renderImage(g, image[1], i, 0, LEFT | TOP);
        }
        w += image[2].class.getField("I.width.I").get(image[2]);
        renderImage(g, image[2], w, 0, RIGHT | TOP);

        // Tile middle rows
        if (image[4] != null) {
            iW = image[4].class.getField("I.width.I").get(image[4]);
        }
        var iH = image[3].class.getField("I.height.I").get(image[3]);
        h -= image[6].class.getField("I.height.I").get(image[6]);
        w -= image[5].class.getField("I.width.I").get(image[5]);
        for (var i = image[0].class.getField("I.height.I").get(image[0]); i <= h; i += iH) {
            renderImage(g, image[3], 0, i, LEFT | TOP);
            for (var j = image[3].class.getField("I.width.I").get(image[3]); j <= w; j += iW) {
                renderImage(g, image[4], j, i, LEFT | TOP);
            }
            renderImage(g, image[5], w + image[5].class.getField("I.width.I").get(image[5]), i,
                        RIGHT | TOP);
        }
        w += image[5].class.getField("I.width.I").get(image[5]);
        h += image[6].class.getField("I.height.I").get(image[6]);

        // Bottom border
        iW = image[7].class.getField("I.width.I").get(image[7]);
        renderImage(g, image[6], 0, h, LEFT | BOTTOM);
        w -= image[8].class.getField("I.width.I").get(image[8]);
        for (var i = image[6].class.getField("I.width.I").get(image[6]); i < w; i += iW) {
            renderImage(g, image[7], i, h, LEFT | BOTTOM);
        }
        w += image[8].class.getField("I.width.I").get(image[8]);
        renderImage(g, image[8], w, h, RIGHT | BOTTOM);

        transX.set(g, transX.get(g) - x);
        transY.set(g, transY.get(g) - y);
    });

    Native.create("javax/microedition/lcdui/Graphics.getDisplayColor.(I)I", function(ctx, color) {
        return color;
    });

    Native.create("javax/microedition/lcdui/Graphics.getPixel.(IIZ)I", function(ctx, rgb, gray, isGray) {
        return swapRB(rgb) | 0xff000000;
    });

    Native.create("com/nokia/mid/ui/DirectGraphicsImp.setARGBColor.(I)V", function(ctx, rgba) {
        var g = this.class.getField("I.graphics.Ljavax/microedition/lcdui/Graphics;").get(this);
        var red = (rgba >> 16) & 0xff;
        var green = (rgba >> 8) & 0xff;
        var blue = rgba & 0xff;
        g.class.getField("I.pixel.I").set(g, swapRB(rgba));
        g.class.getField("I.rgbColor.I").set(g, rgba & 0x00ffffff);
        // Conversion matches Graphics#grayVal(int, int, int).
        g.class.getField("I.gray.I").set(g, (red * 76 + green * 150 + blue * 29) >> 8);
    });

    Native.create("com/nokia/mid/ui/DirectGraphicsImp.getAlphaComponent.()I", function(ctx) {
        var g = this.class.getField("I.graphics.Ljavax/microedition/lcdui/Graphics;").get(this);
        var pixel = g.class.getField("I.pixel.I").get(g);
        return (pixel >> 24) & 0xff;
    });

    Native.create("com/nokia/mid/ui/DirectGraphicsImp.getPixels.([SIIIIIII)V",
    function(ctx, pixels, offset, scanlength, x, y, width, height, format) {
        if (pixels == null) {
            throw new JavaException("java/lang/NullPointerException", "Pixels array is null");
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
                var r = (rgba & 0xF8000000) >>> 16;
                var g = (rgba & 0xFC0000) >>> 13;
                var b = (rgba & 0xF800) >>> 11;
                return (r | g | b);
            };
        } else {
            throw new JavaException("java/lang/IllegalArgumentException", "Format unsupported");
        }

        var graphics = this.class.getField("I.graphics.Ljavax/microedition/lcdui/Graphics;").get(this);
        var image = graphics.class.getField("I.img.Ljavax/microedition/lcdui/Image;").get(graphics);
        var imageData = image.class.getField("I.imageData.Ljavax/microedition/lcdui/ImageData;").get(image);

        contextToRgbData(convertNativeImageData(imageData), pixels, offset, scanlength, x, y, width, height, converterFunc);
    });

    Native.create("com/nokia/mid/ui/DirectGraphicsImp.drawPixels.([SZIIIIIIII)V",
    function(ctx, pixels, transparency, offset, scanlength, x, y, width, height, manipulation, format) {
        if (pixels == null) {
            throw new JavaException("java/lang/NullPointerException", "Pixels array is null");
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
            throw new JavaException("java/lang/IllegalArgumentException", "Format unsupported");
        }

        var graphics = this.class.getField("I.graphics.Ljavax/microedition/lcdui/Graphics;").get(this);

        var context = createContext2d(width, height);
        rgbDataToContext(context, pixels, offset, scanlength, converterFunc);
        withGraphics(graphics, function(c) {
            withClip(graphics, c, x, y, function(x, y) {
                c.drawImage(context.canvas, x, y);
            });
        });
    });

    Native.create("javax/microedition/lcdui/Graphics.render.(Ljavax/microedition/lcdui/Image;III)Z", function(ctx, image, x, y, anchor) {
        renderImage(this, image, x, y, anchor);
        return true;
    });

    Native.create("javax/microedition/lcdui/Graphics.drawString.(Ljava/lang/String;III)V", function(ctx, jStr, x, y, anchor) {
        var str = util.fromJavaString(jStr);
        var g = this;
        withGraphics(g, function(c) {
            withTextAnchor(g, c, anchor, x, y, str, function(x, y) {
                withOpaquePixel(g, c, function() {
                    c.fillText(str, x, y);
                });
            });
        });
    });

    Native.create("javax/microedition/lcdui/Graphics.drawChars.([CIIIII)V", function(ctx, data, offset, len, x, y, anchor) {
        var str = util.fromJavaChars(data, offset, len);
        var g = this;
        withGraphics(g, function(c) {
            withTextAnchor(g, c, anchor, x, y, str, function(x, y) {
                withPixel(g, c, function() {
                    c.fillText(str, x, y);
                });
            });
        });
    });

    Native.create("javax/microedition/lcdui/Graphics.drawChar.(CIII)V", function(ctx, jChr, x, y, anchor) {
        var chr = String.fromCharCode(jChr);
        var g = this;
        withGraphics(g, function(c) {
            withTextAnchor(g, c, anchor, x, y, chr, function(x, y) {
                withPixel(g, c, function() {
                    c.fillText(chr, x, y);
                });
            });
        });
    });

    Native.create("javax/microedition/lcdui/Graphics.fillTriangle.(IIIIII)V", function(ctx, x1, y1, x2, y2, x3, y3) {
        var g = this;
        withGraphics(g, function(c) {
            withClip(g, c, x1, y1, function(x, y) {
                withPixel(g, c, function() {
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
    });

    Native.create("javax/microedition/lcdui/Graphics.drawRect.(IIII)V", function(ctx, x, y, w, h) {
        var g = this;
        withGraphics(g, function(c) {
            withClip(g, c, x, y, function(x, y) {
                withPixel(g, c, function() {
                    withSize(w, h, function(w, h) {
                        c.strokeRect(x, y, w, h);
                    });
                });
            });
        });
    });

    Native.create("javax/microedition/lcdui/Graphics.fillRect.(IIII)V", function(ctx, x, y, w, h) {
        var g = this;
        withGraphics(g, function(c) {
            withClip(g, c, x, y, function(x, y) {
                withPixel(g, c, function() {
                    withSize(w, h, function(w, h) {
                        c.fillRect(x, y, w, h);
                    });
                });
            });
        });
    });

    Native.create("javax/microedition/lcdui/Graphics.fillRoundRect.(IIIIII)V", function(ctx, x, y, w, h, arcWidth, arcHeight) {
        var g = this;
        withGraphics(g, function(c) {
            withClip(g, c, x, y, function(x, y) {
                withPixel(g, c, function() {
                    withSize(w, h, function(w, h) {
                        // TODO implement rounding
                        c.fillRect(x, y, w, h);
                    });
                });
            });
        });
    });

    Native.create("javax/microedition/lcdui/Graphics.drawArc.(IIIIII)V", function(ctx, x, y, width, height, startAngle, arcAngle) {
        var g = this;
        withGraphics(g, function(c) {
            withPixel(g, c, function() {
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
    });

    Native.create("javax/microedition/lcdui/Graphics.fillArc.(IIIIII)V", function(ctx, x, y, width, height, startAngle, arcAngle) {
        var g = this;
        withGraphics(g, function(c) {
            withPixel(g, c, function() {
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
    });

    var TRANS_NONE = 0;
    var TRANS_MIRROR_ROT180 = 1;
    var TRANS_MIRROR = 2;
    var TRANS_ROT180 = 3;
    var TRANS_MIRROR_ROT270 = 4;
    var TRANS_ROT90 = 5;
    var TRANS_ROT270 = 6;
    var TRANS_MIRROR_ROT90 = 7;

    Override.create("javax/microedition/lcdui/Graphics.drawRegion.(Ljavax/microedition/lcdui/Image;IIIIIIII)V",
    function(ctx, image, sx, sy, sw, sh, transform, x, y, anchor) {
        if (!image) {
            throw new JavaException("java/lang/NullPointerException", "src image is null");
        }

        var imgData = image.class.getField("I.imageData.Ljavax/microedition/lcdui/ImageData;").get(image),
            texture = imgData.nativeImageData;

        if (texture instanceof CanvasRenderingContext2D) {
            texture = texture.canvas; // Render the canvas, not the context.
        }

        var w = sw, h = sh;
        var g = this;
        withGraphics(g, function(c) {
            withAnchor(g, c, anchor, x, y, w, h, function(x, y) {
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
    });

    Native.create("javax/microedition/lcdui/Graphics.drawLine.(IIII)V", function(ctx, x1, y1, x2, y2) {
        var dx = x2 - x1, dy = y2 - y1;
        var g = this;
        withGraphics(g, function(c) {
            withClip(g, c, x1, y1, function(x, y) {
                withSize(dx, dy, function(dx, dy) {
                    withPixel(g, c, function() {
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
    });

    Native.create("javax/microedition/lcdui/Graphics.drawRGB.([IIIIIIIZ)V",
    function(ctx, rgbData, offset, scanlength, x, y, width, height, processAlpha) {
        var context = createContext2d(width, height);
        rgbDataToContext(context, rgbData, offset, scanlength, processAlpha ? swapRB : swapRBAndSetAlpha);
        var g = this;
        withGraphics(g, function(c) {
            withClip(g, c, x, y, function(x, y) {
                c.drawImage(context.canvas, x, y);
            });
        });
    });

    var textEditorId = 0,
        textEditorResolve = null,
        dirtyEditors = [];

    function wakeTextEditorThread(id) {
        dirtyEditors.push(id);
        if (textEditorResolve) {
            textEditorResolve();
            textEditorResolve = null;
        }
    }

    Native.create("com/nokia/mid/ui/TextEditor.init.(Ljava/lang/String;IIII)I",
    function(ctx, text, maxSize, constraints, width, height) {
        if (constraints != 0) {
            console.warn("TextEditor.constraints not implemented");
        }

        this.textEditorId = ++textEditorId;
        this.textEditor = document.createElement("textarea");
        this.visible = false;
        this.focused = false;
        this.backgroundColor = 0xFFFFFFFF | 0; // opaque white
        this.foregroundColor = 0xFF000000 | 0; // opaque black
        this.textEditor.style.border = "none";
        this.textEditor.style.resize = "none";
        this.textEditor.style.backgroundColor = abgrIntToCSS(this.backgroundColor);
        this.textEditor.style.color = abgrIntToCSS(this.foregroundColor);
        this.textEditor.value = util.fromJavaString(text);
        this.textEditor.setAttribute("maxlength", maxSize);
        this.textEditor.style.width = width + "px";
        this.textEditor.style.height = height + "px";
        this.textEditor.style.position = "absolute";
        this.textEditor.oninput = function(e) {
            wakeTextEditorThread(this.textEditorId);
        }.bind(this);
        return textEditorId;
    });

    Native.create("com/nokia/mid/ui/CanvasItem.setSize.(II)V", function(ctx, width, height) {
        this.textEditor.style.width = width + "px";
        this.textEditor.style.height = height + "px";
    });

    Native.create("com/nokia/mid/ui/CanvasItem.setVisible.(Z)V", function(ctx, visible) {
        if (visible) {
            document.getElementById("display").appendChild(this.textEditor);
        } else if (this.visible) {
            document.getElementById("display").removeChild(this.textEditor);
        }
        this.visible = visible;
    });

    Native.create("com/nokia/mid/ui/CanvasItem.getWidth.()I", function(ctx) {
        return parseInt(this.textEditor.style.width);
    });

    Native.create("com/nokia/mid/ui/CanvasItem.getHeight.()I", function(ctx) {
        return parseInt(this.textEditor.style.height);
    });

    Native.create("com/nokia/mid/ui/CanvasItem.setPosition.(II)V", function(ctx, x, y) {
        this.textEditor.style.left = x + "px";
        this.textEditor.style.top = y + "px";
    });

    Native.create("com/nokia/mid/ui/CanvasItem.getPositionX.()I", function(ctx) {
        return parseInt(this.textEditor.style.left) || 0;
    });

    Native.create("com/nokia/mid/ui/CanvasItem.getPositionY.()I", function(ctx) {
        return parseInt(this.textEditor.style.top) || 0;
    });

    Native.create("com/nokia/mid/ui/CanvasItem.isVisible.()Z", function(ctx) {
        return this.visible;
    });

    Native.create("com/nokia/mid/ui/TextEditor.setFocus.(Z)V", function(ctx, focused) {
        if (focused) {
            this.textEditor.focus();
        } else {
            this.textEditor.blur();
        }
        this.focused = focused;
    });

    Native.create("com/nokia/mid/ui/TextEditor.hasFocus.()Z", function(ctx) {
        return this.focused;
    });

    Native.create("com/nokia/mid/ui/TextEditor.setCaret.(I)V", function(ctx, index) {
        if (!this.visible) {
            console.warn("setCaret ignored when TextEditor is invisible");
            return;
        }

        this.textEditor.setSelectionRange(index, index);
    });

    Native.create("com/nokia/mid/ui/TextEditor.getCaretPosition.()I", function(ctx) {
        return this.textEditor.selectionStart;
    });

    Native.create("com/nokia/mid/ui/TextEditor.getBackgroundColor.()I", function(ctx) {
        return this.backgroundColor;
    });
    Native.create("com/nokia/mid/ui/TextEditor.getForegroundColor.()I", function(ctx) {
        return this.foregroundColor;
    });
    Native.create("com/nokia/mid/ui/TextEditor.setBackgroundColor.(I)V", function(ctx, backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.textEditor.style.backgroundColor = abgrIntToCSS(backgroundColor);
    });
    Native.create("com/nokia/mid/ui/TextEditor.setForegroundColor.(I)V", function(ctx, foregroundColor) {
        this.foregroundColor = foregroundColor;
        this.textEditor.style.color = abgrIntToCSS(foregroundColor);
    });

    Native.create("com/nokia/mid/ui/TextEditor.getContent.()Ljava/lang/String;", function(ctx) {
        return this.textEditor.value;
    });

    Native.create("com/nokia/mid/ui/TextEditor.setContent.(Ljava/lang/String;)V", function(ctx, str) {
        this.textEditor.value = util.fromJavaString(str);
    });

    Native.create("com/nokia/mid/ui/TextEditor.insert.(Ljava/lang/String;I)V", function(ctx, str, pos) {
        var old = this.textEditor.value;
        this.textEditor.value = old.slice(0, pos) + util.fromJavaString(str) + old.slice(pos);
    });

    Native.create("com/nokia/mid/ui/TextEditor.delete.(II)V", function(ctx, offset, length) {
        var old = _this.textEditor.value;

        if (offset < 0 || offset > old.length || length < 0 || offset + length > old.length) {
            ctx.raiseExceptionAndYield("java.lang.StringIndexOutOfBoundsException", "offset/length invalid");
        }

        this.textEditor.value = old.slice(0, offset) + old.slice(offset + length);
    });

    Native.create("com/nokia/mid/ui/TextEditor.getMaxSize.()I", function(ctx) {
        return parseInt(this.textEditor.getAttribute("maxlength"));
    });

    Native.create("com/nokia/mid/ui/TextEditor.setMaxSize.(I)I", function(ctx, maxSize) {
        this.textEditor.setAttribute("maxlength", maxSize);
        this.textEditor.value = this.textEditor.value.substring(0, maxSize);

        // The return value is the assigned size, which could be less than
        // the size that was requested, although in this case we always set it
        // to the requested size.
        return maxSize;
    });

    Native.create("com/nokia/mid/ui/TextEditor.size.()I", function(ctx) {
        return this.textEditor.value.length;
    });

    Native.create("com/nokia/mid/ui/TextEditorThread.sleep.()V", function(ctx) {
        return new Promise(function(resolve, reject) {
          if (!dirtyEditors.length) {
              textEditorResolve = resolve;
          }
        });
    });

    Native.create("com/nokia/mid/ui/TextEditorThread.getNextDirtyEditor.()I", function(ctx) {
        if (!dirtyEditors.length) {
            console.error("ERROR: getNextDirtyEditor called but no dirty editors");
            return 0;
        }

        return dirtyEditors.shift();
    });
})(Native);
