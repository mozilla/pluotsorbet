package com.sun.midp.crypto;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestMD5 implements Testlet {
    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                  + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public void test(TestHarness th) {
        MD5 md5 = new MD5();
        th.check(md5.getAlgorithm(), "MD5");
        th.check(md5.getDigestLength(), 16);

        byte[] buf = new byte[16];
        try {
            md5.digest(buf, 0, 16);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
        th.check(Util.hexEncode(buf), "d41d8cd98f00b204e9800998ecf8427e");

        md5.reset();

        String part1 = "a";
        md5.update(part1.getBytes(), 0, part1.length());

        try {
            md5.digest(buf, 0, 16);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        th.check(Util.hexEncode(buf), "0cc175b9c0f1b6a831c399e269772661");

        md5.reset();

        md5.update(part1.getBytes(), 0, part1.length());

        String part2 = "NOb";
        md5.update(part2.getBytes(), 2, part2.length()-2);

        String part3 = "NOcNO";
        md5.update(part3.getBytes(), 2, part3.length()-4);

        try {
            md5.digest(buf, 0, 16);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        th.check(Util.hexEncode(buf), "900150983cd24fb0d6963f7d28e17f72");

        // Calculate another hash without calling sha.reset() (sha.digest should reset automatically)

        String longStr = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";
        md5.update(longStr.getBytes(), 0, longStr.length());

        try {
            md5.digest(buf, 0, 16);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        th.check(Util.hexEncode(buf), "57edf4a22be3c955ac49da2e2107b67a");

        byte[] shortBuf = new byte[15];
        try {
            md5.digest(buf, 0, 15);
            th.fail("Should've raised an exception");
        } catch (DigestException ex) {
            th.check(true, "Exception raised");
        }

        md5.reset();

        // Ensure we can calculate two digests simultaneously.
        md5.update(part1.getBytes(), 0, part1.length());
        MD5 md52 = new MD5();
        md52.update(part2.getBytes(), 0, part2.length());
        byte[] buf2 = new byte[16];
        try {
            md5.digest(buf, 0, 16);
            md52.digest(buf2, 0, 16);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
        th.check(Util.hexEncode(buf), "0cc175b9c0f1b6a831c399e269772661");
        th.check(Util.hexEncode(buf2), "40309ea6baa09db375e49aa5edcb40a6");

        md5.reset();
        md52.reset();

        md5.update(part1.getBytes(), 0, part1.length());
        md5.reset();
        md5.update(part2.getBytes(), 0, part2.length());
        try {
            md5.digest(buf, 0, 16);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
        th.check(Util.hexEncode(buf), "40309ea6baa09db375e49aa5edcb40a6");

        byte[] bufMsg = hexToBytes("308203c6a00302010202020301300d06092a864886f70d01010505003063310b30090603550406130255533121301f060355040a131854686520476f2044616464792047726f75702c20496e632e3131302f060355040b1328476f20446164647920436c61737320322043657274696669636174696f6e20417574686f72697479301e170d3036313131363031353433375a170d3236313131363031353433375a3081ca310b30090603550406130255533110300e060355040813074172697a6f6e61311330110603550407130a53636f74747364616c65311a3018060355040a1311476f44616464792e636f6d2c20496e632e31333031060355040b132a687474703a2f2f6365727469666963617465732e676f64616464792e636f6d2f7265706f7369746f72793130302e06035504031327476f204461646479205365637572652043657274696669636174696f6e20417574686f726974793111300f06035504051308303739363932383730820122300d06092a864886f70d01010105000382010f003082010a0282010100c42dd5158c9c264cec3235eb5fb859015aa66181593b7063abe3dc3dc72ab8c933d379e43aed3c3023848eb33014b6b287c33d9554049edf99dd0b251e21de65297e35a8a954ebf6f73239d4265595adeffbfe5886d79ef4008d8c2a0cbd4204cea73f04f6ee80f2aaef52a16966dabe1aad5dda2c66ea1a6bbbe51a514a002f48c79875d8b929c8eef8666d0a9cb3f3fc787ca2f8a3f2b5c3f3b97a91c1a7e6252e9ca8ed12656e6af6124453703095c39c2b582b3d08744af2be51b0bf87d04c27586bb535c59daf1731f80b8feead813605890898cf3aaf2587c049eaa7fd67f7458e97cc1439e23685b57e1a37fd16f671119a743016fe1394a33f840d4f0203010001a38201323082012e301d0603551d0e04160414fdac6132936c45d6e2ee855f9abae7769968cce7301f0603551d23041830168014d2c4b0d291d44c1171b361cb3da1fedda86ad4e330120603551d130101ff040830060101ff020100303306082b0601050507010104273025302306082b060105050730018617687474703a2f2f6f6373702e676f64616464792e636f6d30460603551d1f043f303d303ba039a0378635687474703a2f2f6365727469666963617465732e676f64616464792e636f6d2f7265706f7369746f72792f6764726f6f742e63726c304b0603551d200444304230400604551d20003038303606082b06010505070201162a687474703a2f2f6365727469666963617465732e676f64616464792e636f6d2f7265706f7369746f7279300e0603551d0f0101ff040403020106");
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
            return;
        }
        md.update(bufMsg, 0, bufMsg.length);
        try {
            md.digest(buf, 0, buf.length);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
        th.todo(Util.hexEncode(buf), "3fd0fac5027a9cd891c2dc778c88167a");

        try {
            this.testClone(th);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }

    private void testClone(TestHarness th) throws DigestException {
        MD5 hasher1 = new MD5();
        byte[] buf1 = new byte[16];

        String partA = "a";
        hasher1.update(partA.getBytes(), 0, partA.length());

        MD5 hasher2 = (MD5)hasher1.clone();
        byte[] buf2 = new byte[16];

        String partB = "b";
        hasher1.update(partB.getBytes(), 0, partB.length());

        String partC = "c";
        hasher2.update(partC.getBytes(), 0, partC.length());

        // hasher1 should be unaffected by the update to hasher2.
        // It should hash "ab", the concatenation of partA and partB.
        hasher1.digest(buf1, 0, 16);
        th.check(Util.hexEncode(buf1), "187ef4436122d1cc2f40dc2b92f0eba0");

        // hasher2 should be unaffected by the update to hasher1.
        // It should hash "ac", the concatenation of partA and partC.
        hasher2.digest(buf2, 0, 16);
        th.check(Util.hexEncode(buf2), "e2075474294983e013ee4dd2201c7a73");

        hasher1.reset();
        hasher2.reset();
    }
}
