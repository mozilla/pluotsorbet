package gnu.testlet.vm;

import gnu.testlet.*;

public class ArrayProblemTest implements Testlet {
    private static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                  + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static byte[] expandData = hexToBytes("1019334b4d314c337d0c49314a542a7d150f0a38523d4d3b4b4d2c5a783c4931580f107d0b7defbfbdefbfbd28523d7d112d483c5a6b10257d015a355038492852337d0c3a5c3b0f2a5235503849317b7d0c3a5c2d55310f79317d133c4d30542d553a49344b480d744d304b715a39344b7852335b0f0b3d483c5a783c537defbfbdefbfbd28523d4d4910efbfbdefbfbd5c2a7d192d483c494c337d1b7defbfbdefbfbd320f4c49314a542a7d153b4b4d314c33500e753a49344b78497d133c4d304b350e3a39344b7852335b3c4d304b714a0e392852337d0c3a5c3b715a3550380f343c3a5c2d553a493d503849317d131005534d314c337d1b39314a542a7d19330f1c3a3d4d3b4b4d3152783c49314a380e0c537defbfbdefbfbd28523d4d492d483c5a780f35315a35503849314a337d0c3a5c2d0f313d503849317d13743a5c2d553a521020497d133c4d304b35553a49344b7d110e2c55304b715a353c4b7852337d140f13383c5a783c497d0828523d4d3b3d1012327d192d483c5a3c337d1b7defbfbdefbfbd285c101b39314a542a7d19334b4d314c337d0c0f214a49344b78527d0b3c4d304b7d050e2d3c4b7852337d144d304b715a3910294a337d0c3a5c2d7d015a3550384928103d2a5c2d553a4934583849317d133c3c0f0855314c337d1b694a542a7d192d5310024d4d3b4b4d314c7defbfbdefbfbd3c49314a543a0f117d0828523d4d3b3d483c5a783c53101a3c337d1b7defbfbdefbfbd285c2a7d192d483c491024583849317d133c3c3a5c2d553a493d0f327d0b3c4d304b7d053a49344b78490e22484b715a3550337852337d0c550f1d2c5a783c49315828523d4d3b4b380f0d7d112d483c5a6b7d1b7defbfbdefbfbd2852320f414a542a7d192d534d314c337d1b3910147defbfbdefbfbd3c49314a543a3d4d3b4b4d31520e38337852337d0c55304b715a353c0f637d0c3a5c2d55315a35503849314a1028542d553a49344b4849317d133c4d2a1004494c337d1b7defbfbdefbfbd32542a7d192d48550f553b4b4d314c33503c49314a542a4d0e015828523d4d3b4b383c5a783c490f437d1b7defbfbdefbfbd2852327d192d483c5a3c102b4849317d133c4d2a5c2d553a4934580d235b3c4d304b714a49344b78520e3c3b715a3550387052337d0c3a480f0952783c49314a38523d4d3b4b4d2c0e15492d483c5a787d0b7defbfbdefbfbd28523d0f1832542a7d192d4855314c337d1b690f03503c49314a542a4d4d3b4b4d314c0e307052337d0c3a484b715a3550330e3b743a5c2d553a5235503849317b103335553a49344b7d11317d133c4d3054");
    private static byte[] key = hexToBytes("65696768746b6579");

    private static byte[] expandKey(byte[] key, int keyOffset) {
        byte ek[] = new byte[128];
        int pos = 0;

        for (int i = 0; i < 8; i++) {
            System.out.println("KEYOFFSET: " + keyOffset);
            int octet = key[keyOffset++];
            int len;

            for (int j = 0; j < 7; j++)  {
                System.out.println("pos1: " + pos);
                len = expandData[pos++];
                int offset = 0;

                if ((octet & (0x80 >> j)) != 0) {

                    while (len-- > 0) {
                        int v;
                        System.out.println("pos2: " + pos);
                        v = expandData[pos];
                        System.out.println("v: " + v);
                        pos++;
                        if (v == 125) {
                            offset += 16;
                        } else {
                            System.out.println("offset: " + offset);
                            System.out.println("v: " + v);
                            System.out.println("v >> 3: " + (v >> 3));
                            System.out.println("total: " + (offset + (v >> 3)));
                            ek[offset += (v >> 3)] |= (1 << (v & 0x7));
                        }
                    }
                } else {
                    pos += len;
                }
            }
        }
        return ek;
    }

    public void test(TestHarness th) {
        expandKey(key, 0);
    }
}
