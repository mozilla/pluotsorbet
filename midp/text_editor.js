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
                    this.textEditorElem.style[styleKey] = this.styles[styleKey];
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
                this.textEditorElem.style[styleKey] = styleValue;
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
                this.setStyle('zIndex', 999);
            } else {
                this.setStyle('opaque', 0);
                // To make sure the j2me control could be clicked again to show the
                // textEditor, we need to put the textEditor at the bottom.
                this.setStyle('zIndex', -999);
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

        oninput: function(callback) {
            if (typeof callback == 'function') this.oninputCallback = callback;
        }
    }

    function TextAreaEditor() {
        this.textEditorElem = document.createElement('div');
        this.textEditorElem.contentEditable = true;
        this.setStyle('word-break', 'break-all');
        this.setStyle('word-wrap', 'break-word');
        this.setStyle('overflow', 'auto');
        this.setStyle('-moz-appearance', 'textfield-multiline');

        this.textEditorElem.oninput = function() {
            this.content = this.textEditorElem.textContent;
            this.oninputCallback && this.oninputCallback();
        }.bind(this);
    }
    TextAreaEditor.prototype = extendsObject({
        ranges: [
            '\ud83c[\udf00-\udfff]', // U+1F300 to U+1F3FF
            '\ud83d[\udc00-\ude4f]', // U+1F400 to U+1F64F
            '\ud83d[\ude80-\udeff]'  // U+1F680 to U+1F6FF
        ],

        getContent: function() {
            return this.content || '';
        },

        setContent: function(content) {
            this.content = content;

            if (!this.textEditorElem) {
                return;
            }

            var img = '<img src="http://img1.wikia.nocookie.net/__cb20120718024112/fantendo/images/6/6e/Small-mario.png" height="16" width="16">';
            this.textEditorElem.innerHTML = content.replace(new RegExp(this.ranges.join('|'), 'g'), img);
        },

        getSelectionStart: function() {
            if (this.textEditorElem) {
                var sel = window.getSelection();

                if (sel.anchorNode !== this.textEditorElem &&
                    sel.anchorNode.parentNode !== this.textEditorElem) {
                    // The editor isn't currently selected
                    console.log("GET SELECTION WHILE UNFOCUSED");
                    return 0;
                }

                var count = 0;

                if (sel.anchorNode.nodeType === 3) {
                    count = sel.anchorOffset;
                    var prev = sel.anchorNode.previousSibling;
                    while (prev !== null) {
                        count += (prev.textContent) ? prev.textContent.length : 1;
                        prev = prev.previousSibling;
                    }
                } else {
                    var children = sel.anchorNode.childNodes;
                    for (var i = 0; i < sel.anchorOffset; i++) {
                        var cur = children[i];
                        count += (cur.textContent) ? cur.textContent.length : 1;
                    }
                }

                return count;
            }

            return 0;
        },

        setSelectionRange: function(from, to) {
            if (!this.textEditorElem) {
                this.selectionRange = [from, to];
            } else {
                if (from != to) {
                    console.warn("setSelectionRange not supported when from != to");
                }

                var sel = window.getSelection();
                var range = sel.getRangeAt(0);

                var children = this.textEditorElem.childNodes;
                for (var i = 0; i < children.length; i++) {
                    var cur = children[i];
                    var length = (cur.textContent) ? cur.textContent.length : 1;

                    if (length >= from) {
                        if (cur.textContent) {
                            range.setStart(cur, from);
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

        getSize: function() {
            return this.content.replace(new RegExp(this.ranges.join('|'), 'g'), " ").length;
        },
    }, CommonEditorPrototype);

    function PasswordEditor() {
        this.textEditorElem = document.createElement('input');
        this.textEditorElem.type = 'password';

        this.textEditorElem.oninput = function() {
            this.content = this.textEditorElem.value;
            this.oninputCallback && this.oninputCallback();
        }.bind(this);
    }

    PasswordEditor.prototype = extendsObject({
        getContent: function() {
            return this.content || '';
        },

        setContent: function(content) {
            this.content = content;
            if (!this.textEditorElem) {
                return;
            }

            this.textEditorElem.value = content;
        },

        getSelectionStart: function() {
            if (this.textEditorElem) {
                return this.textEditorElem.selectionStart;
            }

            return 0;
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

