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

            this.textEditorElem.oninput = function() {
                this.content = this.textEditorElem.innerHTML;
                this.oninputCallback && this.oninputCallback();
            }.bind(this);

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

            this.textEditorElem.innerHTML = this.content || '';
            if (this.selectionRange) {
                this.textEditorElem.setSelectionRange(this.selectionRange[0], this.selectionRange[1]);
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

        getContent: function() {
            return this.content || '';
        },

        setContent: function(content) {
            this.content = content;
            if (!this.textEditorElem) {
                return;
            }

            this.textEditorElem.innerHTML = content;
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

            if (!this.textEditorElem) {
                if (!aVisible) return;

                // Only create and initialize the text editor element when changing
                // the visibility to true at the first time.
                this.createTextEditorElem();
                this.decorateTextEditorElem();
            }

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

        getSelectionStart: function() {
            if (this.textEditorElem) {
                //return this.textEditorElem.selectionStart;
                return 0;
            }

            return 0;
        },

        setSelectionRange: function(from, to) {
            if (!this.textEditorElem) {
                this.selectionRange = [from, to];
            } else {
                //this.textEditorElem.setSelectionRange(from, to);
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

    function TextAreaEditor() { }
    TextAreaEditor.prototype = extendsObject({
        createTextEditorElem: function() {
            this.textEditorElem = document.createElement('div');
            this.textEditorElem.contentEditable = true;
            this.setStyle('word-break', 'break-all');
        }
    }, CommonEditorPrototype);

    function PasswordEditor() {}
    PasswordEditor.prototype = extendsObject({
        createTextEditorElem: function() {
            this.textEditorElem = document.createElement('input');
            this.textEditorElem.type = 'password';
        }
    }, CommonEditorPrototype);

    function TextEditorWrapper(constraints) {
        this.textEditor = null;
        this.setConstraints(constraints);
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
            //this.textEditor.setSelectionRange(from, to);
        },

        setAttribute: function(attrName, value) {
            this.textEditor.setAttribute(attrName, value);
        },

        getAttribute: function(attrName) {
            return this.textEditor.getAttribute(attrName);
        },

        oninput: function(callback) {
            this.textEditor.oninput(callback);
        }
    };

    return {
        createEditor: function(constraints) {
            return new TextEditorWrapper(constraints);
        }
    };
})();

