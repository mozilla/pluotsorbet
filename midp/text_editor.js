'use strict'

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

            if (e.keyCode == 8) {
                var range = this.getSelectionRange();

                if (range[0].index != range[1].index) {
                    // If some text has been selected, remove it and set the new caret position
                    // to the first character before the selection.
                    this.setContent(this.getSlice(0, range[0].index) + this.getSlice(range[1].index));
                    this.setSelectionRange(range[0].index, range[0].index);
                } else {
                    var toRemove = 1;

                    // If the node that is currently selected is an image and its codepoint length
                    // is 2, we remove both the codepoints.
                    // On the Nokia Asha, only the second codepoint is removed, so another emoji is
                    // shown instead of the first one (the emoji associated with the first codepoint).
                    if (range[0].node.nodeType === 1 && util.toCodePointArray(range[0].node.alt).length === 2) {
                        toRemove = 2;
                    }

                    // If there's no text currently selected, remove the first character before
                    // the current caret position and reduce the caret position by 1.
                    this.setContent(this.getSlice(0, range[0].index - toRemove) + this.getSlice(range[0].index));
                    this.setSelectionRange(range[0].index - toRemove, range[0].index - toRemove);
                }

                if (this.oninputCallback) {
                    this.oninputCallback();
                }

                return false;
            } else if (e.keyCode == 13) {
                this.addToContent("\n");
                return false;
            }
        }.bind(this);

        this.textEditorElem.onkeypress = function(e) {
            if (e.charCode) {
                this.addToContent(String.fromCharCode(e.charCode));
                return false;
            }
        }.bind(this);
    }
    TextAreaEditor.prototype = extendsObject({
        getContent: function() {
            return this.content || '';
        },

        addToContent: function(newContent) {
            var range = this.getSelectionRange();

            // Add the new content, replacing the current selection.
            // If the selection is collapsed, just add the content
            // at the selected position.
            this.setContent(this.getSlice(0, range[0].index) +
                            newContent +
                            this.getSlice(range[1].index));

            // Set the current selection after the new added character.
            this.setSelectionRange(range[0].index + 1, range[0].index + 1);

            // Notify TextEditor listeners.
            if (this.oninputCallback) {
                this.oninputCallback();
            }
        },

        setContent: function(content) {
            this.content = content;

            if (!this.textEditorElem) {
                return;
            }

            var toImg = function(str) {
                return '<img src="' + emoji.strToImg(str) + '" height="' + this.font.size +
                       'pt" width="' + this.font.size + 'pt" alt="' + str + '">';
            }.bind(this);

            this.textEditorElem.innerHTML = content.replace(emoji.regEx, toImg) + "\n";
        },

        getSelectionEnd: function() {
            if (this.textEditorElem) {
                var sel = window.getSelection();

                if (sel.focusNode !== this.textEditorElem &&
                    sel.focusNode.parentNode !== this.textEditorElem) {
                    console.error("getSelectionEnd called while the editor is unfocused");
                    return 0;
                }

                var selectedNode = null;
                var count = 0;

                if (sel.focusNode.nodeType === 3) {
                    selectedNode = sel.focusOffset;
                    count = sel.focusOffset;
                    var prev = sel.focusNode.previousSibling;
                    while (prev) {
                        count += (prev.textContent) ? prev.textContent.length : util.toCodePointArray(prev.alt).length;
                        prev = prev.previousSibling;
                    }
                } else {
                    var children = sel.focusNode.childNodes;
                    for (var i = 0; i < sel.focusOffset; i++) {
                        var cur = children[i];
                        count += (cur.textContent) ? cur.textContent.length : util.toCodePointArray(cur.alt).length;
                    }
                    selectedNode = children[sel.focusOffset - 1];
                }

                // If the position returned is higher than the size of the content,
                // the selected character is the additional "\n" that we have in the
                // div innerHTML. We subtract 1 to the position to retrieve the correct
                // value.
                if (count > this.getSize()) {
                    count = count - 1;
                }

                return { index: count, node: selectedNode };
            }

            return { index: 0, node: null };
        },

        getSelectionStart: function() {
            if (this.textEditorElem) {
                var sel = window.getSelection();

                if (sel.anchorNode !== this.textEditorElem &&
                    sel.anchorNode.parentNode !== this.textEditorElem) {
                    console.error("getSelectionStart called while the editor is unfocused");
                    return { index: 0, node: null };
                }

                var selectedNode = null;
                var count = 0;

                if (sel.anchorNode.nodeType === 3) {
                    selectedNode = sel.anchorNode;
                    count = sel.anchorOffset;
                    var prev = sel.anchorNode.previousSibling;
                    while (prev) {
                        count += (prev.textContent) ? prev.textContent.length : util.toCodePointArray(prev.alt).length;
                        prev = prev.previousSibling;
                    }
                } else {
                    var children = sel.anchorNode.childNodes;
                    for (var i = 0; i < sel.anchorOffset; i++) {
                        var cur = children[i];
                        count += (cur.textContent) ? cur.textContent.length : util.toCodePointArray(cur.alt).length;
                    }
                    selectedNode = children[sel.anchorOffset - 1];
                }

                // If the position returned is higher than the size of the content,
                // the selected character is the additional "\n" that we have in the
                // div innerHTML. We subtract 1 to the position to retrieve the correct
                // value.
                if (count > this.getSize()) {
                    count = count - 1;
                }

                return { index: count, node: selectedNode };
            }

            return { index: 0, node: null };
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

                // If we're trying to set the selection to the last character and
                // the last character is a "\n", we need to add 1 to the position
                // because of the additional "\n" we have in the div innerHTML.
                var size = this.getSize();
                if (from === size && this.content[this.content.length-1] == "\n") {
                    from = from + 1;
                }

                var children = this.textEditorElem.childNodes;
                for (var i = 0; i < children.length; i++) {
                    var cur = children[i];
                    var length = (cur.textContent) ? cur.textContent.length : util.toCodePointArray(cur.alt).length;

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

