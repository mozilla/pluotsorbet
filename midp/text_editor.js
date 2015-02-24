'use strict';

var TextEditorProvider = (function() {
    var eTextArea = document.getElementById('textarea-editor');
    var ePassword = document.getElementById('password-editor');
    var currentVisibleEditor = null;

    function extendsObject(targetObj, srcObj) {
        for (var m in srcObj) {
            targetObj[m] = srcObj[m];
        }
        return targetObj;
    }

    var CommonEditorPrototype = {
        attached: false,
        width: 0,
        height: 0,
        left: 0,
        top: 0,
        constraints: 0,
        type: "",
        content: "",
        visible: false,
        id: -1,
        selectionRange: [0, 0],
        focused: false,
        oninputCallback: null,

        // opaque white
        backgroundColor:  0xFFFFFFFF | 0,

        // opaque black
        foregroundColor:  0xFF000000 | 0,

        attach: function() {
            this.attached = true;
        },

        detach: function() {
            this.attached = false;
        },

        isAttached: function() {
            return this.attached;
        },

        decorateTextEditorElem: function() {
            // Set attributes.
            if (this.attributes) {
                for (var attr in this.attributes) {
                    this.textEditorElem.setAttribute(attr, this.attributes[attr]);
                }
            }

            this.setContent(this.content);

            this.setSelectionRange(this.selectionRange[0], this.selectionRange[1]);
            this.setSize(this.width, this.height);
            this.setFont(this.font);
            this.setPosition(this.left, this.top);
            this.setBackgroundColor(this.backgroundColor);
            this.setForegroundColor(this.foregroundColor);
        },

        _setStyle: function(styleKey, styleValue) {
            if (this.visible) {
                this.textEditorElem.style.setProperty(styleKey, styleValue);
            }
        },

        focus: function() {
            this.focused = true;
            return new Promise(function(resolve, reject) {
                if (currentVisibleEditor !== this ||
                    document.activeElement === this.textEditorElem) {
                    resolve();
                    return;
                }
                setTimeout(this.textEditorElem.focus.bind(this.textEditorElem));
                this.textEditorElem.onfocus = resolve;
            }.bind(this));
        },

        blur: function() {
            this.focused = false;
            return new Promise(function(resolve, reject) {
                if (currentVisibleEditor !== this ||
                    document.activeElement !== this.textEditorElem) {
                    resolve();
                    return;
                }
                setTimeout(this.textEditorElem.blur.bind(this.textEditorElem));
                this.textEditorElem.onblur = resolve;
            }.bind(this));
        },

        getVisible: function() {
            return this.visible;
        },

        setVisible: function(aVisible) {
            // Check if we need to show or hide the html editor elements.
            if ((currentVisibleEditor === this && aVisible) ||
                (currentVisibleEditor !== this && !aVisible)) {
                this.visible = aVisible;
                return;
            }

            this.visible = aVisible;

            if (aVisible) {
                if (currentVisibleEditor) {
                    currentVisibleEditor.visible = false;
                }
                currentVisibleEditor = this;
            } else {
                currentVisibleEditor = null;
            }

            if (aVisible) {
                this.textEditorElem.classList.add("show");
            } else {
                this.textEditorElem.classList.remove("show");
            }

            if (this.visible) {
                var oldId = this.textEditorElem.getAttribute("editorId") || -1;
                if (oldId !== this.id) {
                    this.textEditorElem.setAttribute("editorId", this.id);
                    this.decorateTextEditorElem();
                    if (this.focused) {
                        setTimeout(this.textEditorElem.focus.bind(this.textEditorElem));
                    }
                }
                this.activate();
            } else {
                if (!this.focused) {
                    setTimeout(this.textEditorElem.blur.bind(this.textEditorElem));
                }
                this.deactivate();
            }
        },

        setAttribute: function(attrName, value) {
            if (!this.attributes) {
                this.attributes = { };
            }

            this.attributes[attrName] = value;
            if (this.textEditorElem) {
                this.textEditorElem.setAttribute(attrName, value);
            }
        },

        getAttribute: function(attrName) {
            if (!this.attributes) {
                return null;
            }

            return this.attributes[attrName];
        },

        setFont: function(font) {
            this.font = font;
            this._setStyle("font", font.css);
        },

        setSize: function(width, height) {
            this.width = width;
            this.height = height;
            this._setStyle("width", width + "px");
            this._setStyle("height", height + "px");
        },

        getWidth: function() {
            return this.width;
        },

        getHeight: function() {
            return this.height;
        },

        setPosition: function(left, top) {
            this.left = left;
            this.top = top;
            var t = MIDP.context2D.canvas.offsetTop + top;
            this._setStyle("left", left + "px");
            this._setStyle("top",  t + "px");
        },

        getLeft: function() {
            return this.left;
        },

        getTop: function() {
            return this.top;
        },

        setBackgroundColor: function(color) {
            this.backgroundColor = color;
            this._setStyle("backgroundColor", util.abgrIntToCSS(color));
        },

        getBackgroundColor: function() {
            return this.backgroundColor;
        },

        setForegroundColor: function(color) {
            this.foregroundColor = color;
            this._setStyle("color", util.abgrIntToCSS(color));
        },

        getForegroundColor: function() {
            return this.foregroundColor;
        },

        oninput: function(callback) {
            if (typeof callback == 'function') this.oninputCallback = callback;
        },
    }

    function TextAreaEditor() {
        this.textEditorElem = eTextArea;
    }

    TextAreaEditor.prototype = extendsObject({
        html: '',

        activate: function() {
            this.textEditorElem.onkeydown = function(e) {
                if (this.getContentSize() >= this.getAttribute("maxlength")) {
                    // http://stackoverflow.com/questions/12467240/determine-if-javascript-e-keycode-is-a-printable-non-control-character
                    if ((e.keyCode >= 48 && e.keyCode <= 57)  || // number keys
                        e.keyCode === 32 || e.keyCode === 13 || // spacebar & return key(s) (if you want to allow carriage returns)
                        (e.keyCode >= 65 && e.keyCode <= 90)   || // letter keys
                        (e.keyCode >= 96 && e.keyCode <= 111)  || // numpad keys
                        (e.keyCode >= 186 && e.keyCode <= 192) || // ;=,-./` (in order)
                        (e.keyCode >= 219 && e.keyCode <= 222)) { // [\]' (in order)
                        return false;
                    }
                }
                return true;
            }.bind(this);

            this.textEditorElem.oninput = function(e) {
                if (e.isComposing) {
                    return;
                }

                // Save the current selection.
                var range = this.getSelectionRange();

                // Remove the last <br> tag if any.
                var html = this.textEditorElem.innerHTML;
                var lastBr = html.lastIndexOf("<br>");
                if (lastBr !== -1) {
                    html = html.substring(0, lastBr);
                }

                // Replace <br> by \n so that textContent attribute doesn't
                // strip new lines.
                html = html.replace("<br>", "\n", "g");

                // Convert the emoji images back to characters.
                // The original character is stored in the alt attribute of its
                // object tag with the format of <object ... name='X' ..>.
                html = html.replace(/<object[^>]*name="(\S*)"[^>]*><\/object>/g, '$1');

                this.textEditorElem.innerHTML = html;

                this.setContent(this.textEditorElem.textContent);

                // Restore the current selection after updating emoji images.
                this.setSelectionRange(range[0], range[1]);

                // Notify TextEditor listeners.
                if (this.oninputCallback) {
                    this.oninputCallback();
                }
            }.bind(this);
        },

        deactivate: function() {
            this.textEditorElem.onkeydown = null;
            this.textEditorElem.oninput = null;
        },

        getContent: function() {
            return this.content;
        },

        setContent: function(content) {
            // Filter all the \r characters as we use \n.
            content = content.replace("\r", "", "g");

            this.content = content;

            // Escape the content to avoid malicious injection via innerHTML.
            this.textEditorElem.textContent = content;
            var html = this.textEditorElem.innerHTML;

            if (!this.visible) {
                return;
            }

            var toImg = function(str) {
                var emojiData = emoji.getData(str, this.font.size);

                var scale = this.font.size / emoji.squareSize;

                var style = 'display:inline-block;';
                style += 'width:' + this.font.size + 'px;';
                style += 'height:' + this.font.size + 'px;';
                style += 'background:url(' + emojiData.img.src + ') -' + (emojiData.x * scale) + 'px 0px no-repeat;';
                style += 'background-size:' + (emojiData.img.naturalWidth * scale) + 'px ' + this.font.size + 'px;';

                // We use <object> instead of <img> to not allow image resizing.
                return '<object style="' + style + '" name="' + str + '"></object>';
            }.bind(this);

            // Replace "\n" by <br>
            html = html.replace("\n", "<br>", "g");

            html = html.replace(emoji.regEx, toImg) + "<br>";

            this.textEditorElem.innerHTML = html;
            this.html = html;
        },

        _getNodeTextLength: function(node) {
            if (node.nodeType == Node.TEXT_NODE) {
                return node.textContent.length;
            } else if (node instanceof HTMLBRElement) {
                // Don't count the last <br>
                return node.nextSibling ? 1 : 0;
            } else {
                // It should be an HTMLImageElement of a emoji.
                return util.toCodePointArray(node.name).length;
            }
        },

        _getSelectionOffset: function(node, offset) {
            if (!this.visible) {
                return 0;
            }

            if (node !== this.textEditorElem &&
                node.parentNode !== this.textEditorElem) {
                console.error("_getSelectionOffset called while the editor is unfocused");
                return 0;
            }

            var selectedNode = null;
            var count = 0;

            if (node.nodeType === Node.TEXT_NODE) {
                selectedNode = node;
                count = offset;
                var prev = node.previousSibling;
                while (prev) {
                    count += this._getNodeTextLength(prev);
                    prev = prev.previousSibling;
                }
            } else {
                var children = node.childNodes;
                for (var i = 0; i < offset; i++) {
                    var cur = children[i];
                    count += this._getNodeTextLength(cur);
                }
                selectedNode = children[offset - 1];
            }

            return count;
        },

        getSelectionEnd: function() {
            var sel = window.getSelection();
            return this._getSelectionOffset(sel.focusNode, sel.focusOffset);
        },

        getSelectionStart: function() {
            var sel = window.getSelection();
            return this._getSelectionOffset(sel.anchorNode, sel.anchorOffset);
        },

        getSelectionRange: function() {
            var start = this.getSelectionStart();
            var end = this.getSelectionEnd();

            if (start > end) {
                return [ end, start ];
            }

            return [ start, end ];
        },

        setSelectionRange: function(from, to) {
            this.selectionRange = [from, to];
            if (!this.visible) {
                return;
            }
            if (from != to) {
                console.error("setSelectionRange not supported when from != to");
            }

            var children = this.textEditorElem.childNodes;
            for (var i = 0; i < children.length; i++) {
                var cur = children[i];
                var length = this._getNodeTextLength(cur);

                if (length >= from) {
                    var selection = window.getSelection();
                    var range;
                    if (selection.rangeCount === 0) {
                        // XXX: This makes it so chrome does not break here, but
                        // text boxes still do not behave correctly in chrome.
                        range = document.createRange();
                        selection.addRange(range);
                    } else {
                        range = selection.getRangeAt(0);
                    }
                    if (cur.textContent) {
                        range.setStart(cur, from);
                    } else if (from === 0) {
                        range.setStartBefore(cur);
                    } else {
                        range.setStartAfter(cur);
                    }
                    range.collapse(true);
                    break;
                }

                from -= length;
            }
        },

        getSlice: function(from, to) {
            return util.toCodePointArray(this.content).slice(from, to).join("");
        },

        /*
         * The TextEditor::getContentSize() method returns the length of the content in codepoints
         */
        getContentSize: function() {
            return util.toCodePointArray(this.content).length;
        },

        /*
         * The height of the content is estimated by creating an hidden div
         * with the same style as the TextEditor element.
         */
        getContentHeight: function() {
            var div = document.getElementById("hidden-textarea-editor");
            div.style.setProperty("width", this.getWidth() + "px");
            div.style.setProperty("font", this.font.css);
            div.innerHTML = this.html;
            var height = div.offsetHeight;

            div.innerHTML = "";

            return height;
        },
    }, CommonEditorPrototype);

    function PasswordEditor() {
        this.textEditorElem = ePassword;
    }

    PasswordEditor.prototype = extendsObject({
        activate: function() {
            this.textEditorElem.oninput = function() {
                this.content = ePassword.value;
                if (this.oninputCallback) {
                    this.oninputCallback();
                }
            }.bind(this);
        },

        deactivate: function() {
            this.textEditorElem.oninput = null;
        },

        getContent: function() {
            return this.content;
        },

        setContent: function(content) {
            this.content = content;

            if (this.visible) {
                this.textEditorElem.value = content;
            }
        },

        getSelectionStart: function() {
            if (this.visible) {
                return this.textEditorElem.selectionStart;
            }

            return 0;
        },

        getSelectionEnd: function() {
            if (this.visible) {
                return this.textEditorElem.selectionEnd;
            }

            return 0;
        },

        getSelectionRange: function() {
            var start = this.getSelectionStart();
            var end = this.getSelectionEnd();

            if (start > end) {
                return [ end, start ];
            }

            return [ start, end ];
        },

        setSelectionRange: function(from, to) {
            this.selectionRange = [from, to];
            if (!this.visible) {
                return;
            }
            this.textEditorElem.setSelectionRange(from, to);
        },

        getSlice: function(from, to) {
            return this.content.slice(from, to);
        },

        getContentSize: function() {
            return this.content.length;
        },

        getContentHeight: function() {
            var lineHeight = this.font.klass.classInfo.getField("I.height.I").get(this.font);
            return ((this.content.match(/\n/g) || []).length + 1) * lineHeight;
        },
    }, CommonEditorPrototype);

    return {
        getEditor: function(constraints, oldEditor, editorId) {
            var TYPE_TEXTAREA = 'textarea';
            var TYPE_PASSWORD = 'password';

            // https://docs.oracle.com/javame/config/cldc/ref-impl/midp2.0/jsr118/javax/microedition/lcdui/TextField.html#constraints
            var CONSTRAINT_ANY = 0;
            var CONSTRAINT_PASSWORD = 0x10000;

            function _createEditor(type, constraints, editorId) {
                var editor;
                switch(type) {
                    case TYPE_PASSWORD:
                        editor = new PasswordEditor();
                        break;
                    case TYPE_TEXTAREA: // fall through
                    default:
                        editor = new TextAreaEditor();
                        break;
                }
                editor.type = type;
                editor.constraints = constraints;
                editor.id = editorId;
                return editor;
            }

            var type = TYPE_TEXTAREA;
            if (constraints == CONSTRAINT_ANY) {
                type = TYPE_TEXTAREA;
            } else if (constraints == (CONSTRAINT_ANY | CONSTRAINT_PASSWORD)) {
                type = TYPE_PASSWORD;
            } else {
                console.warn('Constraints ' + constraints + ' not supported.');

                if (constraints & CONSTRAINT_PASSWORD) {
                    // Special case: use the PASSWORD type if there's a PASSWORD constraint,
                    // even though the mode isn't ANY.
                    type = TYPE_PASSWORD;
                } else {
                    // Fall back to the default value.
                    type = TYPE_TEXTAREA;
                }
            }

            var newEditor;

            if (!oldEditor) {
                newEditor = _createEditor(type, constraints, editorId);
                return newEditor;
            }

            if (type === oldEditor.type) {
                return oldEditor;
            }

            // The type is changed and we need to copy all the attributes.
            var newEditor = _createEditor(type, constraints, editorId);
            ["attributes",
             "width", "height",
             "left", "top",
             "backgroundColor", "foregroundColor",
             "attached",
             "content",
             "font",
             "oninputCallback"].forEach(function(attr) {
                newEditor[attr] = oldEditor[attr];
            });

            // Call setVisible to update display.
            newEditor.setVisible(oldEditor.visible);
            return newEditor;
        }
    };
})();
