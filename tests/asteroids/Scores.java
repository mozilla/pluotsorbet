/**
 * Copyright 2001 Jean-Francois Doue
 *
 * This file is part of Asteroid Zone. Asteroid Zone is free software;
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * Asteroid Zone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Asteroid Zone; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 */

package asteroids;

import javax.microedition.rms.*;
import java.io.*;

/**
 * Class used for storing game scores.
 * @author Jean-Francois Doue
 * @version 1.0, 2001/07/26
 */
public class Scores {
    /**
     * An array of player names.
     */

    public String[] names = {"LAU", "RAG", "NES"};
    /**
     * An array of player high scores.
     */
    public int[] values = {980, 675, 172};
    //public int[] values = {30, 20, 10};

    /**
     * Utility method to convert a integer score to a character
     * array.
     */
    public static void toCharArray(int score, char[] charArray) {
        // Convert the score to an array of chars
        // of the form: 0000
        charArray[0] = (char)('0' + (score / 1000));
        score = score % 1000;
        charArray[1] = (char)('0' + (score / 100));
        score = score % 100;
        charArray[2] = (char)('0' + (score / 10));
        score = score % 10;
        charArray[3] = (char)('0' + score);
    }

    public Scores() {

        // Initialize / read scores from persistent storage.
        RecordStore recordStore = null;
        try {
            recordStore = RecordStore.openRecordStore("scores", true);

            // If the record store exists and contains records,
            // read the high scores.
            if (recordStore.getNumRecords() > 0) {
                for (int i = 0; i < names.length; i++) {
                    byte[] record = recordStore.getRecord(i + 1);
                    DataInputStream istream = new DataInputStream(new ByteArrayInputStream(record, 0, record.length));
                    values[i] = istream.readInt();
                    names[i] = istream.readUTF();
                }
            } else {
                // Otherwise, create the records and initialize them
                // with the default values. They will have record IDs
                // 1, 2, 3
                for (int i = 0; i < names.length; i++) {
                    ByteArrayOutputStream bstream = new ByteArrayOutputStream(12);
                    DataOutputStream ostream = new DataOutputStream(bstream);
                    ostream.writeInt(values[i]);
                    ostream.writeUTF(names[i]);
                    ostream.flush();
                    ostream.close();
                    byte[] record = bstream.toByteArray();
                    recordStore.addRecord(record, 0, record.length);
                }
            }
        } catch(Exception e) {
        } finally {
            if (recordStore != null) {
                try {
                    recordStore.closeRecordStore();
                } catch(Exception e) {
                }
            }
        }
    }

    /**
     * Returns true if the score is among the high scores.
     */
    public boolean isHighScore(int score) {
        for (int i = 0; i < names.length; i++) {
            if (score >= values[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the high score database with the supplied name and score.
     */
    public void addHighScore(int score, String name) {
        for (int i = 0; i < names.length; i++) {
            if (score >= values[i]) {
                // Shift the score table.
                for (int j = names.length - 1; j > i; j--) {
                    values[j] = values[j - 1];
                    names[j] = names[j - 1];
                }

                // Insert the new score.
                values[i] = score;
                names[i] = name;

                // Overwrite the scores in persistent storage.
                RecordStore recordStore = null;
                try {
                    recordStore = RecordStore.openRecordStore("scores", true);
                    for (int j = 0; j < names.length; j++) {
                        ByteArrayOutputStream bstream = new ByteArrayOutputStream(12);
                        DataOutputStream ostream = new DataOutputStream(bstream);
                        ostream.writeInt(values[j]);
                        ostream.writeUTF(names[j]);
                        ostream.flush();
                        ostream.close();
                        byte[] record = bstream.toByteArray();
                        recordStore.setRecord(j + 1, record, 0, record.length);
                    }
                } catch(Exception e) {
                } finally {
                    if (recordStore != null) {
                        try {
                            recordStore.closeRecordStore();
                        } catch(Exception e) {
                        }
                    }
                }
                break;
            }
        }
    }
}
