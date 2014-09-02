package com.sun.midp.crypto;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestSHA implements Testlet {
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
        SHA sha = new SHA();
        th.check(sha.getAlgorithm(), "SHA-1");
        th.check(sha.getDigestLength(), 20);

        byte[] buf = new byte[20];
        try {
            sha.digest(buf, 0, 20);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
        th.check(Util.hexEncode(buf), "da39a3ee5e6b4b0d3255bfef95601890afd80709");

        sha.reset();

        String part1 = "a";
        sha.update(part1.getBytes(), 0, part1.length());

        try {
            sha.digest(buf, 0, 20);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        th.check(Util.hexEncode(buf), "86f7e437faa5a7fce15d1ddcb9eaeaea377667b8");

        sha.reset();

        sha.update(part1.getBytes(), 0, part1.length());

        String part2 = "NOb";
        sha.update(part2.getBytes(), 2, part2.length()-2);

        String part3 = "NOcNO";
        sha.update(part3.getBytes(), 2, part3.length()-4);

        try {
            sha.digest(buf, 0, 20);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        th.check(Util.hexEncode(buf), "a9993e364706816aba3e25717850c26c9cd0d89d");

        // Calculate another hash without calling sha.reset() (sha.digest should reset automatically)

        String longStr = "12345678901234567890123456789012345678901234567890123456789012345678901234567890";
        sha.update(longStr.getBytes(), 0, longStr.length());

        try {
            sha.digest(buf, 0, 20);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }

        th.check(Util.hexEncode(buf), "50abf5706a150990a08b2c5ea40fa0e585554732");

        byte[] shortBuf = new byte[15];
        try {
            sha.digest(buf, 0, 15);
            th.fail("Should've raised an exception");
        } catch (DigestException ex) {
            th.check(true, "Exception raised");
        }

        sha.reset();

        // Ensure we can calculate two digests simultaneously.
        sha.update(part1.getBytes(), 0, part1.length());
        SHA sha2 = new SHA();
        sha2.update(part2.getBytes(), 0, part2.length());
        byte[] buf2 = new byte[20];
        try {
            sha.digest(buf, 0, 20);
            sha2.digest(buf2, 0, 20);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
        th.check(Util.hexEncode(buf), "86f7e437faa5a7fce15d1ddcb9eaeaea377667b8");
        th.check(Util.hexEncode(buf2), "5bd138dc4bccbc9526f6575c21d5e66450cd257f");

        sha.reset();
        sha2.reset();

        sha.update(part1.getBytes(), 0, part1.length());
        sha.reset();
        sha.update(part2.getBytes(), 0, part2.length());
        try {
            sha.digest(buf, 0, 20);
        } catch (DigestException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
        th.check(Util.hexEncode(buf), "5bd138dc4bccbc9526f6575c21d5e66450cd257f");

        sha.reset();

        byte[] bufMsg = hexToBytes("308203c6a00302010202020301300d06092a864886f70d01010505003063310b30090603550406130255533121301f060355040a131854686520476f2044616464792047726f75702c20496e632e3131302f060355040b1328476f20446164647920436c61737320322043657274696669636174696f6e20417574686f72697479301e170d3036313131363031353433375a170d3236313131363031353433375a3081ca310b30090603550406130255533110300e060355040813074172697a6f6e61311330110603550407130a53636f74747364616c65311a3018060355040a1311476f44616464792e636f6d2c20496e632e31333031060355040b132a687474703a2f2f6365727469666963617465732e676f64616464792e636f6d2f7265706f7369746f72793130302e06035504031327476f204461646479205365637572652043657274696669636174696f6e20417574686f726974793111300f06035504051308303739363932383730820122300d06092a864886f70d01010105000382010f003082010a0282010100c42dd5158c9c264cec3235eb5fb859015aa66181593b7063abe3dc3dc72ab8c933d379e43aed3c3023848eb33014b6b287c33d9554049edf99dd0b251e21de65297e35a8a954ebf6f73239d4265595adeffbfe5886d79ef4008d8c2a0cbd4204cea73f04f6ee80f2aaef52a16966dabe1aad5dda2c66ea1a6bbbe51a514a002f48c79875d8b929c8eef8666d0a9cb3f3fc787ca2f8a3f2b5c3f3b97a91c1a7e6252e9ca8ed12656e6af6124453703095c39c2b582b3d08744af2be51b0bf87d04c27586bb535c59daf1731f80b8feead813605890898cf3aaf2587c049eaa7fd67f7458e97cc1439e23685b57e1a37fd16f671119a743016fe1394a33f840d4f0203010001a38201323082012e301d0603551d0e04160414fdac6132936c45d6e2ee855f9abae7769968cce7301f0603551d23041830168014d2c4b0d291d44c1171b361cb3da1fedda86ad4e330120603551d130101ff040830060101ff020100303306082b0601050507010104273025302306082b060105050730018617687474703a2f2f6f6373702e676f64616464792e636f6d30460603551d1f043f303d303ba039a0378635687474703a2f2f6365727469666963617465732e676f64616464792e636f6d2f7265706f7369746f72792f6764726f6f742e63726c304b0603551d200444304230400604551d20003038303606082b06010505070201162a687474703a2f2f6365727469666963617465732e676f64616464792e636f6d2f7265706f7369746f7279300e0603551d0f0101ff040403020106");
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
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
        th.check(Util.hexEncode(buf), "db433834d80013ac248c21b354b482890a8f9fc2");
    }
}
