/*
 * The purpose of this class is to have a generic type
 * for labels and offsets, used by tableswitch and lookupswitch
 *
 */

package jas;

import java.io.DataOutputStream;
import java.io.IOException;

public class LabelOrOffset {
    private Label label;
    private int offset;

    public LabelOrOffset(Label l) {
        label = l;
    }

    public LabelOrOffset(int o) {
        offset = o;
    }

    Label getLabel() {
        return label;
    }

    int getOffset() {
        return offset;
    }

    void writeWideOffset(CodeAttr ce, Insn source, DataOutputStream out)
                                            throws jasError, IOException {
        if(label!=null)
            label.writeWideOffset(ce, source, out);
        else
            out.writeInt(offset);
    }
}