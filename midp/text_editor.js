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

    function countNewlines(str) {
        var count = 0;
        for (var i = 0; i < str.length; i++) {
            if (str.charCodeAt(i) == 10) {
                count++;
            }
        }
        return count;
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
            // http://stackoverflow.com/questions/12467240/determine-if-javascript-e-keycode-is-a-printable-non-control-character
            if (((e.keyCode > 47 && e.keyCode < 58)  || // number keys
                e.keyCode === 32 || e.keyCode === 13 || // spacebar & return key(s) (if you want to allow carriage returns)
                (e.keyCode > 64 && e.keyCode < 91)   || // letter keys
                (e.keyCode > 95 && e.keyCode < 112)  || // numpad keys
                (e.keyCode > 185 && e.keyCode < 193) || // ;=,-./` (in order)
                (e.keyCode > 218 && e.keyCode < 223)) && // [\]' (in order)
                this.getSize() >= this.getAttribute("maxlength")) {
                return false;
            }

            if (e.keyCode == 8) {
                var range = this.getSelectionRange();

                if (range[0] != range[1]) {
                    // If some text has been selected, remove it and set the new caret position
                    // to the first character before the selection.
                    this.setContent(this.getSlice(0, range[0]) + this.getSlice(range[1]));
                    this.setSelectionRange(range[0], range[0]);
                } else {
                    // If there's no text currently selected, remove the first character before
                    // the current caret position and reduce the caret position by 1.
                    this.setContent(this.getSlice(0, range[0] - 1) + this.getSlice(range[0]));
                    this.setSelectionRange(range[0] - 1, range[0] - 1);
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

            console.log("CONTENT: " + countNewlines(this.content));
            console.log("INNERHTML: " + countNewlines(this.textEditorElem.innerHTML));
            console.log("CONTENT: " + this.content);
            console.log("INNERHTML: " + this.textEditorElem.innerHTML);
        }.bind(this);
    }
    TextAreaEditor.prototype = extendsObject({
        // http://www.unicode.org/Public/UNIDATA/EmojiSources.txt
        // http://developer.nokia.com/resources/library/Java/developers-guides/data-handling/emoji.html
        ranges: [
            '\ud83c[\udf00-\udfff]', // U+1F300 to U+1F3FF
            '\ud83d[\udc00-\ude4f]', // U+1F400 to U+1F64F
            '\ud83d[\ude80-\udeff]', // U+1F680 to U+1F6FF
            '[\u0023|\u0030-\u0039]\u20e3', // U+2320E3 to U+3920E3
            '\ud83c\uddef\ud83c\uddf5', // U+1F1EF U+1F1F5 (JP)
            '\ud83c\uddf0\ud83c\uddf7', // U+1F1F0 U+1F1F7 (KR)
            '\ud83c\udde9\ud83c\uddea', // U+1F1E9 U+1F1EA (DE)
            '\ud83c\udde8\ud83c\uddf3', // U+1F1E8 U+1F1F3 (CN)
            '\ud83c\uddfa\ud83c\uddf8', // U+1F1FA U+1F1F8 (US)
            '\ud83c\uddeb\ud83c\uddf7', // U+1F1EB U+1F1F7 (FR)
            '\ud83c\uddea\ud83c\uddf8', // U+1F1EA U+1F1F8 (ES)
            '\ud83c\uddee\ud83c\uddf9', // U+1F1EE U+1F1F9 (IT)
            '\ud83c\uddf7\ud83c\uddfa', // U+1F1F7 U+1F1FA (RU)
            '\ud83c\uddec\ud83c\udde7', // U+1F1EC U+1F1E7 (GB)
            '\u00a9',
            '\u00ae',
            '\u2122',
            '\u2139',
            '\u2194',
            '\u2195',
            '\u2196',
            '\u2197',
            '\u2198',
            '\u2199',
            '\u21A9',
            '\u21AA',
            '\u231A',
            '\u231B',
            '\u23E9',
            '\u23EA',
            '\u23EB',
            '\u23EC',
            '\u23F0',
            '\u23F3',
            '\u24C2',
            '\u25AA',
            '\u25AB',
            '\u25B6',
            '\u25C0',
            '\u25FB',
            '\u25FC',
            '\u25FD',
            '\u25FE',
            '\u2600',
            '\u2601',
            '\u260E',
            '\u2611',
            '\u2614',
            '\u2615',
            '\u261D',
            '\u2648',
            '\u2649',
            '\u264A',
            '\u264B',
            '\u264C',
            '\u264D',
            '\u264E',
            '\u264F',
            '\u2650',
            '\u2651',
            '\u2652',
            '\u2653',
            '\u2660',
            '\u2663',
            '\u2665',
            '\u2666',
            '\u2668',
            '\u267B',
            '\u267F',
            '\u2693',
            '\u26A0',
            '\u26A1',
            '\u26AA',
            '\u26AB',
            '\u26BD',
            '\u26BE',
            '\u26C4',
            '\u26C5',
            '\u26CE',
            '\u26D4',
            '\u26EA',
            '\u26FA',
            '\u26F2',
            '\u26F3',
            '\u26F5',
            '\u26FD',
            '\u2702',
            '\u2705',
            '\u2708',
            '\u2709',
            '\u270A',
            '\u270B',
            '\u270C',
            '\u270F',
            '\u2712',
            '\u2714',
            '\u2716',
            '\u2728',
            '\u2733',
            '\u2734',
            '\u2744',
            '\u2747',
            '\u274C',
            '\u274E',
            '\u2753',
            '\u2754',
            '\u2755',
            '\u2757',
            '\u2764',
            '\u2795',
            '\u2796',
            '\u2797',
            '\u27A1',
            '\u27B0',
            '\u27BF',
            '\u2934',
            '\u2935',
            '\u2B05',
            '\u2B06',
            '\u2B07',
            '\u2B1B',
            '\u2B1C',
            '\u2B50',
            '\u2B55',
            '\u3030',
            '\u303D',
            '\u3297',
            '\u3299',
            '\ud83c\udd70', // U+1F170
            '\ud83c\udd71', // U+1F171
            '\ud83c\udd7E', // U+1F17E
            '\ud83c\udd7F', // U+1F17F
            '\ud83c\udd8E', // U+1F18E
            '\ud83c\udd91', // U+1F191
            '\ud83c\udd92', // U+1F192
            '\ud83c\udd93', // U+1F193
            '\ud83c\udd94', // U+1F194
            '\ud83c\udd95', // U+1F195
            '\ud83c\udd96', // U+1F196
            '\ud83c\udd97', // U+1F197
            '\ud83c\udd98', // U+1F198
            '\ud83c\udd99', // U+1F199
            '\ud83c\udd9A', // U+1F19A
            '\ud83c\udE01', // U+1F201
            '\ud83c\udE02', // U+1F202
            '\ud83c\udE1A', // U+1F21A
            '\ud83c\udE2F', // U+1F22F
            '\ud83c\udE32', // U+1F232
            '\ud83c\udE33', // U+1F233
            '\ud83c\udE34', // U+1F234
            '\ud83c\udE35', // U+1F235
            '\ud83c\udE36', // U+1F236
            '\ud83c\udE37', // U+1F237
            '\ud83c\udE38', // U+1F238
            '\ud83c\udE39', // U+1F239
            '\ud83c\udE3A', // U+1F23A
            '\ud83c\udE50', // U+1F250
            '\ud83c\udE51', // U+1F251
        ],

        getContent: function() {
            return this.content || '';
        },

        addToContent: function(newContent) {
            var range = this.getSelectionRange();

            // Add the new content, replacing the current selection.
            // If the selection is collapsed, just add the content
            // at the selected position.
            this.setContent(this.getSlice(0, range[0]) +
                            newContent +
                            this.getSlice(range[1]));

            // Set the current selection after the new added character
            this.setSelectionRange(range[0] + 1, range[0] + 1);

            // Notify TextEditor listeners
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
                var firstCodePoint = str.codePointAt(0);
                var img = firstCodePoint.toString(16);
                str = str.substr(String.fromCodePoint(firstCodePoint).length);
                if (str.length > 0) {
                    img += "-" + str.codePointAt(0).toString(16);
                }

                return '<img src="style/emoji/' + img + '.png" height="' + this.font.size +
                       'pt" width="' + this.font.size + 'pt">';
            }.bind(this);

            this.textEditorElem.innerHTML = content.replace(new RegExp(this.ranges.join('|'), 'g'), toImg) + "\n";
        },

        getSelectionEnd: function() {
            if (this.textEditorElem) {
                var sel = window.getSelection();

                if (sel.focusNode !== this.textEditorElem &&
                    sel.focusNode.parentNode !== this.textEditorElem) {
                    console.warn("getSelectionEnd called while the editor is unfocused");
                    return 0;
                }

                var count = 0;

                if (sel.focusNode.nodeType === 3) {
                    count = sel.focusOffset;
                    var prev = sel.focusNode.previousSibling;
                    while (prev) {
                        count += (prev.textContent) ? prev.textContent.length : 1;
                        prev = prev.previousSibling;
                    }
                } else {
                    var children = sel.focusNode.childNodes;
                    for (var i = 0; i < sel.focusOffset; i++) {
                        var cur = children[i];
                        count += (cur.textContent) ? cur.textContent.length : 1;
                    }
                }

                // If the position returned is higher than the size of the content,
                // the selected character is the additional "\n" that we have in the
                // div innerHTML. We subtract 1 to the position to retrieve the correct
                // value.
                if (count > this.getSize()) {
                    count = count - 1;
                }

                return count;
            }

            return 0;
        },

        getSelectionStart: function() {
            if (this.textEditorElem) {
                var sel = window.getSelection();

                if (sel.anchorNode !== this.textEditorElem &&
                    sel.anchorNode.parentNode !== this.textEditorElem) {
                    console.warn("getSelectionStart called while the editor is unfocused");
                    return 0;
                }

                var count = 0;

                if (sel.anchorNode.nodeType === 3) {
                    count = sel.anchorOffset;
                    var prev = sel.anchorNode.previousSibling;
                    while (prev) {
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

                // If the position returned is higher than the size of the content,
                // the selected character is the additional "\n" that we have in the
                // div innerHTML. We subtract 1 to the position to retrieve the correct
                // value.
                if (count > this.getSize()) {
                    count = count - 1;
                }

                return count;
            }

            return 0;
        },

        getSelectionRange: function() {
            var indexStart = this.getSelectionStart();
            var indexEnd = this.getSelectionEnd();

            if (indexStart > indexEnd) {
                var tmp = indexStart;
                indexStart = indexEnd;
                indexEnd = tmp;
            }

            return [ indexStart, indexEnd ];
        },

        setSelectionRange: function(from, to) {
            if (!this.textEditorElem) {
                this.selectionRange = [from, to];
            } else {
                if (from != to) {
                    console.warn("setSelectionRange not supported when from != to");
                }

                // If we're trying to set the selection to the last character and
                // the last character is a "\n", we need to add 1 to the position
                // because of the additional "\n" we have in the div innerHTML.
                var size = this.getSize();
                if (from === size && this.content[this.content.length-1] == "\n") {
                    from = from + 1;
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
            return util.toCodePointArray(this.content).length;
        },

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

        getContentHeight: function() {
            var lineHeight = this.font.class.getField("I.height.I").get(this.font);

            var count = 1;
            for (var i = 0; i < this.content.length; i++) {
                if (this.content[i] == "\n") {
                    count++;
                }
            }

            return count * lineHeight;
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

