/*
 * This file is part of Wakame, a Java reimplementation of Nori, an educational ray tracer by Wenzel Jakob.
 *
 * Copyright (c) 2015 by Pramook Khungurn
 *
 * Wakame is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License Version 3
 * as published by the Free Software Foundation.
 *
 * Wakame is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package yondoko.util;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import javax_.vecmath.Tuple3f;
import javax_.vecmath.Tuple4f;
import javax_.vecmath.Tuple4i;

import org.apache.commons.io.input.SwappedDataInputStream;

/**
 * Utility class for writing binary values to streams.
 */
public class BinaryIo {
    public static void readTuple3f(SwappedDataInputStream fin, Tuple3f v) throws IOException {
        v.x = fin.readFloat();
        v.y = fin.readFloat();
        v.z = fin.readFloat();
    }

    public static void readTuple4f(SwappedDataInputStream fin, Tuple4f v) throws IOException {
        v.x = fin.readFloat();
        v.y = fin.readFloat();
        v.z = fin.readFloat();
        v.w = fin.readFloat();
    }

    public static void readTuple4i(SwappedDataInputStream fin, Tuple4i v) throws IOException {
        v.x = fin.readInt();
        v.y = fin.readInt();
        v.z = fin.readInt();
        v.w = fin.readInt();
    }

    public static String readShiftJisString(SwappedDataInputStream fin, int length) throws IOException {
        byte[] data = new byte[length];
        int read = fin.read(data, 0, length);
        if (read == -1) {
            throw new EOFException("end of file reached");
        } else {
            int l = 0;
            while (l < data.length && data[l] != '\0') {
                l++;
            }
            byte[] s = new byte[l];
            for (int i = 0; i < l; i++) {
                s[i] = data[i];
            }
            return new String(s, "Shift-JIS");
        }
    }

    public static void writeByteString(DataOutputStream fout, byte[] b, int length) throws IOException {
        fout.write(b);
        for (int i = 0; i < length - b.length; i++) {
            fout.write('\0');
        }
    }

    public static void writeString(DataOutputStream fout, String s, int length) throws IOException {
        byte[] b = s.getBytes();
        writeByteString(fout, b, length);
    }

    public static void writeString(DataOutputStream fout, String s) throws IOException {
        writeString(fout, s, s.length());
    }

    public static void writeLittleEndianVaryingLengthString(DataOutputStream fout, String s) throws IOException {
        writeLittleEndianInt(fout, s.length());
        writeString(fout, s);
    }

    public static void writeShiftJISString(DataOutputStream fout, String s, int length) throws IOException {
        byte[] b = s.getBytes("Shift-JIS");
        writeByteString(fout, b, length);
    }

    public static void writeLittleEndianTuple3f(DataOutputStream fout, Tuple3f t) throws IOException {
        writeLittleEndianFloat(fout, t.x);
        writeLittleEndianFloat(fout, t.y);
        writeLittleEndianFloat(fout, t.z);
    }

    public static void writeLittleEndianTuple4f(DataOutputStream fout, Tuple4f t) throws IOException {
        writeLittleEndianFloat(fout, t.x);
        writeLittleEndianFloat(fout, t.y);
        writeLittleEndianFloat(fout, t.z);
        writeLittleEndianFloat(fout, t.w);
    }

    public static void writeLittleEndianTuple4i(DataOutputStream fout, Tuple4i t) throws IOException {
        writeLittleEndianInt(fout, t.x);
        writeLittleEndianInt(fout, t.y);
        writeLittleEndianInt(fout, t.z);
        writeLittleEndianInt(fout, t.w);
    }

    public static String readString(SwappedDataInputStream fin, int length) throws IOException {
        byte[] data = new byte[length];
        int read = fin.read(data, 0, length);
        if (read == -1) {
            throw new EOFException("end of file reached");
        } else {
            int l = 0;
            while (l < data.length && data[l] != '\0') {
                l++;
            }
            byte[] s = new byte[l];
            for (int i = 0; i < l; i++) {
                s[i] = data[i];
            }
            return new String(s);
        }
    }

    public static String readVariableLengthString(SwappedDataInputStream fin) throws IOException {
        int length = fin.readInt();
        String result = readString(fin, length);
        return result;
    }

    public static void writeLittleEndianShort(DataOutputStream out, short value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >> 8) & 0xFF);
    }

    public static void writeLittleEndianInt(DataOutputStream out, int value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >> 8) & 0xFF);
        out.writeByte((value >> 16) & 0xFF);
        out.writeByte((value >> 24) & 0xFF);
    }

    public static void writeLittleEndianFloat(DataOutputStream out, float value) throws IOException {
        writeLittleEndianInt(out, Float.floatToIntBits(value));
    }
}
