'use strict';

var TextEditorProvider = (function() {
    function extendsObject(targetObj, srcObj) {
        for (var m in srcObj) {
            targetObj[m] = srcObj[m];
        }
        return targetObj;
    }

    var CommonEditorPrototype = {
        destroy: function() {
            if (this.textEditorElem && this.textEditorElem.parentNode) {
                this.textEditorElem.parentNode.removeChild(this.textEditorElem);
            }
            if (this.textEditorElem) {
                this.textEditorElem.oninput = null;
            }
            this.parentNode = null;
            this.textEditorElem = null;
            this.oninputCallback = null;
            this.constraints = null;
            this.attributes = null;
        },

        setParent: function(parentNode) {
            this.parentNode = parentNode;
            if (!parentNode) {
                if (this.textEditorElem.parentNode) {
                    this.textEditorElem.parentNode.removeChild(this.textEditorElem);
                }
                return;
            }

            if (this.textEditorElem) {
                parentNode.appendChild(this.textEditorElem);
            }
        },

        getParent: function() {
            return this.parentNode;
        },

        decorateTextEditorElem: function() {
            if (this.parentNode) {
                this.parentNode.appendChild(this.textEditorElem);
            }

            // Set attributes and styles.
            if (this.attributes) {
                for (var attr in this.attributes) {
                    this.textEditorElem.setAttribute(attr, this.attributes[attr]);
                }
            }

            if (this.styles) {
                for (var styleKey in this.styles) {
                    this.textEditorElem.style.setProperty(styleKey, this.styles[styleKey]);
                }
            }

            this.setContent(this.content || '');
            if (this.selectionRange) {
                this.setSelectionRange(this.selectionRange[0], this.selectionRange[1]);
                delete this.selectionRange;
            }

            if (this.focused) this.focus();
            this.setVisible(this.visible);
        },

        setStyle: function(styleKey, styleValue) {
            // Set input/textarea elem styles.
            if (!this.styles) {
                this.styles = {};
            }

            this.styles[styleKey] = styleValue;
            if (this.textEditorElem) {
                this.textEditorElem.style.setProperty(styleKey, styleValue);
            }
        },

        getStyle:  function(styleKey) {
            return (this.styles && this.styles[styleKey]) || null;
        },

        focus: function() {
            this.focused = true;
            this.textEditorElem && this.textEditorElem.focus();
        },

        blur: function() {
            this.focused = false;
            this.textEditorElem && this.textEditorElem.blur();
        },

        getVisible: function() {
            return this.visible || false;
        },

        setVisible: function(aVisible) {
            this.visible = aVisible;

            if (aVisible) {
                // Sometimes in Java, setVisible() is called after focus(), to make
                // sure the native input won't lose focus, we change opacity instead
                // of visibility.
                this.setStyle('opaque', 1);
                this.setStyle('z-index', 999);
            } else {
                this.setStyle('opaque', 0);
                // To make sure the j2me control could be clicked again to show the
                // textEditor, we need to put the textEditor at the bottom.
                this.setStyle('z-index', -999);
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
            this.setStyle("font-style", font.style);
            this.setStyle("font-size", font.size);
            this.setStyle("font-face", font.face);
        },

        oninput: function(callback) {
            if (typeof callback == 'function') this.oninputCallback = callback;
        },
    }

    function TextAreaEditor() {
        this.content = "";
        this.textEditorElem = document.createElement('div');
        this.textEditorElem.contentEditable = true;
        this.setStyle('word-break', 'break-all');
        this.setStyle('word-wrap', 'break-word');
        this.setStyle('overflow', 'auto');
        this.setStyle('white-space', 'pre-wrap');
        this.setStyle('-moz-appearance', 'textfield-multiline');

        this.textEditorElem.onkeydown = function(e) {
            if (this.getSize() >= this.getAttribute("maxlength")) {
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
        }.bind(this);

        this.textEditorElem.oninput = function(e) {
            if (e.isComposing) {
                return;
            }

            // Save the current selection.
            var range = this.getSelectionRange();

            // Remove the last <br> tag if any.
            var content = this.textEditorElem.innerHTML;
            var lastBr = content.lastIndexOf("<br>");
            if (lastBr !== -1) {
                content = content.substring(0, lastBr);
            }

            // Replace <br> by \n
            content = content.replace("<br>", "\n", "g");

            // Convert the emoji images back to characters.
            // The original character is stored in the alt attribute of its
            // img tag with the format of <img ... alt='X' ..>.
            content = content.replace(/<img[^>]*alt="(\S*)"[^>]*>/g, '$1');

            this.setContent(content);

            // Restore the current selection after updating emoji images.
            this.setSelectionRange(range[0].index, range[1].index);

            // Notify TextEditor listeners.
            if (this.oninputCallback) {
                this.oninputCallback();
            }
        }.bind(this);
    }

    TextAreaEditor.prototype = extendsObject({
        getContent: function() {
            return this.content;
        },

        setContent: function(content) {
            // Filter all the \r characters as we use \n.
            content = content.replace("\r", "", "g");

            this.content = content;

            if (!this.textEditorElem) {
                return;
            }

            var toImg = function(str) {
                return '<img src="' + emoji.strToImg(str) + '" height="' + this.font.size +
                       'pt" width="' + this.font.size + 'pt" alt="' + str + '">';
            }.bind(this);

            // Replace "\n" by <br>
            var html = content.replace("\n", "<br>", "g");

            html = html.replace(emoji.regEx, toImg) + "<br>";

            this.textEditorElem.innerHTML = html;
        },

        _getNodeTextLength: function(node) {
            if (node.nodeType == Node.TEXT_NODE) {
                return node.textContent.length;
            } else if (node instanceof HTMLBRElement) {
                // Don't count the last <br>
                return node.nextSibling ? 1 : 0;
            } else {
                // It should be an HTMLImageElement of a emoji.
                return util.toCodePointArray(node.alt).length;
            }
        },

        _getSelectionOffset: function(node, offset) {
            if (!this.textEditorElem) {
                return { index: 0, node: null };
            }

            if (node !== this.textEditorElem &&
                node.parentNode !== this.textEditorElem) {
                console.error("_getSelectionOffset called while the editor is unfocused");
                return { index: 0, node: null };
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

            return { index: count, node: selectedNode };
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

            if (start.index > end.index) {
                return [ end, start ];
            }

            return [ start, end ];
        },

        setSelectionRange: function(from, to) {
            if (!this.textEditorElem) {
                this.selectionRange = [from, to];
            } else {
                if (from != to) {
                    console.error("setSelectionRange not supported when from != to");
                }

                var children = this.textEditorElem.childNodes;
                for (var i = 0; i < children.length; i++) {
                    var cur = children[i];
                    var length = this._getNodeTextLength(cur);

                    if (length >= from) {
                        var range = window.getSelection().getRangeAt(0);
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
            }
        },

        getSlice: function(from, to) {
            return util.toCodePointArray(this.content).slice(from, to).join("");
        },

        /*
         * The TextEditor::size() method returns the length of the content in codepoints
         */
        getSize: function() {
            return util.toCodePointArray(this.content).length;
        },

        /*
         * The height of the content is estimated by creating an hidden div
         * with the same style as the TextEditor element.
         */
        getContentHeight: function() {
            var div = document.createElement("div");
            div.style.setProperty("width", this.getStyle("width"));
            div.style.setProperty("overflow", "none");
            div.style.setProperty("word-break", "break-all");
            div.style.setProperty("word-wrap", "break-word");
            div.style.setProperty("white-space", "pre-wrap");
            div.style.setProperty("position", "absolute");
            div.style.setProperty("left", "0px");
            div.style.setProperty("top", "0px");
            div.style.setProperty("visibility", "hidden");
            div.style.setProperty("display", "block");
            div.style.setProperty("font-style", this.getStyle("font-style"));
            div.style.setProperty("font-size", this.getStyle("font-size"));
            div.style.setProperty("font-face", this.getStyle("font-face"));
            div.innerHTML = this.textEditorElem.innerHTML;
            document.body.appendChild(div);

            var height = div.offsetHeight;

            document.body.removeChild(div);

            return height;
        },
    }, CommonEditorPrototype);

    function PasswordEditor() {
        this.textEditorElem = document.createElement('input');
        this.textEditorElem.type = 'password';

        this.textEditorElem.oninput = function() {
            this.content = this.textEditorElem.value;
            if (this.oninputCallback) {
                this.oninputCallback();
            }
        }.bind(this);
    }

    PasswordEditor.prototype = extendsObject({
        getContent: function() {
            return this.content || '';
        },

        setContent: function(content) {
            this.content = content;

            if (this.textEditorElem) {
                this.textEditorElem.value = content;
            }
        },

        getSelectionStart: function() {
            if (this.textEditorElem) {
                return { index: this.textEditorElem.selectionStart, node: this.textEditorElem };
            }

            return { index: 0, node: null };
        },

        setSelectionRange: function(from, to) {
            if (!this.textEditorElem) {
                this.selectionRange = [from, to];
            } else {
                this.textEditorElem.setSelectionRange(from, to);
            }
        },

        getSlice: function(from, to) {
            return this.content.slice(from, to);
        },

        getSize: function() {
            return this.content.length;
        },

        getContentHeight: function() {
            var lineHeight = this.font.klass.classInfo.getField("I.height.I").get(this.font);
            return ((this.content.match(/\n/g) || []).length + 1) * lineHeight;
        },
    }, CommonEditorPrototype);

    function TextEditorWrapper(constraints) {
        this.textEditor = null;
        this.setConstraints(constraints);
        this.textEditor.decorateTextEditorElem();
    }

    TextEditorWrapper.prototype = {
        setConstraints: function(constraints) {
            var TYPE_TEXTAREA = 'textarea';
            var TYPE_PASSWORD = 'password';

            // https://docs.oracle.com/javame/config/cldc/ref-impl/midp2.0/jsr118/javax/microedition/lcdui/TextField.html#constraints
            var CONSTRAINT_ANY = 0;
            var CONSTRAINT_PASSWORD = 0x10000;

            function _createEditor(type) {
                switch(type) {
                    case TYPE_PASSWORD:
                        return new PasswordEditor();
                    case TYPE_TEXTAREA:
                    default:
                        return new TextAreaEditor();
                }
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

            if (!this.textEditor) {
                this.textEditor = _createEditor(type);
                this.type = type;
                this.constraints = constraints;
                return;
            }

            // If the type is changed, we need to copy all the attributes/styles.
            if (type != this.type) {
                var newEditor = _createEditor(type);
                if (this.textEditor.styles) {
                    for (var styleKey in this.textEditor.styles) {
                        newEditor.setStyle(styleKey, this.textEditor.styles[styleKey]);
                    }
                }
                if (this.textEditor.attributes) {
                    for (var attrName in this.textEditor.attributes) {
                        newEditor.setAttribute(attrName, this.textEditor.attributes[attrName]);
                    }
                }
                if (this.textEditor.focused) {
                    newEditor.focus();
                }
                newEditor.setVisible(this.textEditor.getVisible());
                if (this.textEditor.oninputCallback) {
                    newEditor.oninput(this.textEditor.oninputCallback);
                }
                if (this.textEditor.parentNode) {
                    newEditor.setParent(this.textEditor.parentNode);
                }
                newEditor.setContent(this.textEditor.getContent());

                newEditor.font = this.textEditor.font;

                this.textEditor.destroy();
                this.textEditor = newEditor;
            }

            this.type = type;
            this.constraints = constraints;
        },

        getConstraints: function() {
            return this.constraints || 0;
        },

        setParent: function(parentNode) {
            this.textEditor.setParent(parentNode);
        },

        getParent: function() {
            return this.textEditor.parentNode;
        },

        setStyle: function(styleKey, styleValue) {
            this.textEditor.setStyle(styleKey, styleValue);
        },

        getStyle:  function(styleKey) {
            return this.textEditor.getStyle(styleKey);
        },

        getContent: function() {
            return this.textEditor.getContent()
        },

        setContent: function(content) {
            this.textEditor.setContent(content);
        },

        focus: function() {
            this.textEditor.focus();
        },

        blur: function() {
            this.textEditor.blur();
        },

        setVisible: function(aVisible) {
            this.textEditor.setVisible(aVisible);
        },

        getSelectionStart: function() {
            return this.textEditor.getSelectionStart();
        },

        setSelectionRange: function(from, to) {
            this.textEditor.setSelectionRange(from, to);
        },

        setAttribute: function(attrName, value) {
            this.textEditor.setAttribute(attrName, value);
        },

        getAttribute: function(attrName) {
            return this.textEditor.getAttribute(attrName);
        },

        getSize: function() {
            return this.textEditor.getSize();
        },

        getSlice: function(from, to) {
            return this.textEditor.getSlice(from, to);
        },

        getContentHeight: function() {
            return this.textEditor.getContentHeight();
        },

        setFont: function(font) {
            this.textEditor.setFont(font);
        },

        oninput: function(callback) {
            this.textEditor.oninput(callback);
        },
    };

    return {
        createEditor: function(constraints) {
            return new TextEditorWrapper(constraints);
        }
    };
})();

