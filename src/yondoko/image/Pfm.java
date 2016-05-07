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

package yondoko.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javax_.vecmath.Tuple3d;
import javax_.vecmath.Tuple3f;

import org.apache.commons.io.FileUtils;
import wakame.struct.Color3d;
import yondoko.util.BinaryIo;

import javax.imageio.ImageIO;

/**
 * Represents an image in PFM format. (http://www.pauldebevec.com/Research/HDR/PFM)
 */
public class Pfm {
    public int width, height;
    public float[] data;

    /**
     * Create a blank PFM image with the given width and height.
     * @param width
     * @param height
     */
    public Pfm(int width, int height) {
        this.width = width;
        this.height = height;
        this.data = new float[4 * width * height];
    }

    /**
     * Load the PFM image given the file name.
     * @param fileName
     * @return the loaded PFM image
     * @throws IOException
     */
    public static Pfm load(String fileName) throws IOException {
        FileInputStream fin = new FileInputStream(fileName);

        String line = readLine(fin);
        if (line.charAt(0) != 'P') {
            fin.close();
            throw new IOException("first character must be P");
        }
        boolean isColor = line.charAt(1) == 'F';
        int floatCount = (isColor) ? 3 : 1;

        line = readLine(fin);
        String comps[] = line.split(" ");
        int width = Integer.valueOf(comps[0]);
        int height = Integer.valueOf(comps[1]);

        line = readLine(fin);
        float scalingAndEndian = Float.valueOf(line);
        boolean swap = scalingAndEndian < 0;

        Pfm pfm = new Pfm(width, height);

        FileChannel channel = fin.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(height * width * floatCount * 4);
        channel.read(buffer);
        buffer.rewind();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (!isColor) {
                    pfm.data[4 * index + 0]
                            = pfm.data[4 * index + 1]
                            = pfm.data[4 * index + 2]
                            = getFloat(buffer, index, swap);
                } else {
                    pfm.data[4 * index + 0] = getFloat(buffer, 3 * index + 0, swap);
                    pfm.data[4 * index + 1] = getFloat(buffer, 3 * index + 1, swap);
                    pfm.data[4 * index + 2] = getFloat(buffer, 3 * index + 2, swap);
                }
                pfm.data[4 * index + 3] = 1;
            }
        }

        fin.close();
        return pfm;
    }

    /**
     * Set the pixel at (x,y) so that all the RGB channels all have the given value, and the A channel is 1.
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     * @param value the value to the set to the RGB channel
     */
    public void setFloat(int x, int y, float value) {
        int index = y * width + x;
        data[4 * index + 0] = value;
        data[4 * index + 1] = value;
        data[4 * index + 2] = value;
        data[4 * index + 3] = 1;
    }

    /**
     * Set the pixel at (x,y) so that all the RGB channels all have the given value, and the A channel is 1.
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     * @param value the value to the set to the RGB channel
     */
    public void setFloat(int x, int y, double value) {
        int index = y * width + x;
        data[4 * index + 0] = (float)value;
        data[4 * index + 1] = (float)value;
        data[4 * index + 2] = (float)value;
        data[4 * index + 3] = 1;
    }

    /**
     * Set the RGB color of pixel at (x,y) to the given 3-tuple.
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     * @param value the 3-tuple containing the RGB color in the x, y, and z-component, respectively.
     */
    public void setColor(int x, int y, Tuple3f value) {
        int index = y * width + x;
        data[4 * index + 0] = value.x;
        data[4 * index + 1] = value.y;
        data[4 * index + 2] = value.z;
        data[4 * index + 3] = 1;
    }

    /**
     * Set the RGB color of pixel at (x,y) to the given 3-tuple.
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     * @param value the 3-tuple containing the RGB color in the x, y, and z-component, respectively.
     */
    public void setColor(int x, int y, Tuple3d value) {
        int index = y * width + x;
        data[4 * index + 0] = (float)value.x;
        data[4 * index + 1] = (float)value.y;
        data[4 * index + 2] = (float)value.z;
        data[4 * index + 3] = 1;
    }

    /**
     * Set the values of the given 3-tuple to the RGB color of the pixel at (x,y)
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     * @param value the 3-tuple to contain the RGB color.
     */
    public void getColor(int x, int y, Tuple3f value) {
        int index = y * width + x;
        value.x = data[4 * index + 0];
        value.y = data[4 * index + 1];
        value.z = data[4 * index + 2];
    }

    /**
     * Set the values of the given 3-tuple to the RGB color of the pixel at (x,y)
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     * @param value the 3-tuple to contain the RGB color.
     */
    public void getColor(int x, int y, Tuple3d value) {
        int index = y * width + x;
        value.x = data[4 * index + 0];
        value.y = data[4 * index + 1];
        value.z = data[4 * index + 2];
    }

    /**
     * Get the value of the red channel of the specified pixel
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     * @return value of the red channel at pixel (x,y)
     */
    public float getFloat(int x, int y) {
        int index = y * width + x;
        return data[4 * index + 0];
    }

    /**
     * Save the PFM image to the file with the given file name.
     * @param fileName
     * @throws IOException
     */
    public void save(String fileName) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        DataOutputStream fout = new DataOutputStream(outStream);

        fout.writeBytes("PF\n");
        fout.writeBytes(String.format("%d %d\n", width, height));
        fout.writeBytes("-1.0\n");

        for (int i = 0; i < width * height; i++) {
            int index = 4 * i;
            BinaryIo.writeLittleEndianFloat(fout, data[index + 0]);
            BinaryIo.writeLittleEndianFloat(fout, data[index + 1]);
            BinaryIo.writeLittleEndianFloat(fout, data[index + 2]);
        }

        fout.close();
        outStream.close();

        byte[] data = outStream.toByteArray();
        FileUtils.writeByteArrayToFile(new File(fileName), data);
    }

    private static String readLine(FileInputStream fin) throws IOException {
        StringBuilder builder = new StringBuilder();

        while (true) {
            int ch = fin.read();
            if (ch == '\n' || ch == -1) {
                break;
            } else {
                builder.append((char) ch);
            }
        }

        return builder.toString();
    }

    private static float getFloat(ByteBuffer buffer, int index, boolean swap) {
        int a = (buffer.get(4 * index + 0) & 0xff);
        int b = (buffer.get(4 * index + 1) & 0xff);
        int c = (buffer.get(4 * index + 2) & 0xff);
        int d = (buffer.get(4 * index + 3) & 0xff);
        int v;
        if (swap) {
            v = ((d << 24) | (c << 16) | (b << 8) | a);
        } else {
            v = ((a << 24) | (b << 16) | (c << 8) | d);
        }
        return Float.intBitsToFloat(v);
    }

    /**
     * Return the average luminance of the pixel values.
     * @return the average luminance of the pixel values
     */
    public double averageLuminance() {
        Color3d color = new Color3d();
        double sum = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                getColor(x,y,color);
                sum += color.getLuminance();
            }
        }
        return sum / (width * height);
    }

    public static void convertPngToPfm(String pngFile, String pfmFile) {
        try {
            BufferedImage image = ImageIO.read(new File(pngFile));
            Pfm pfm = new Pfm(image.getWidth(null), image.getHeight(null));

            for (int i = 0; i < image.getWidth(null); i++) {
                for (int j = 0; j < image.getHeight(null); j++) {
                    int rgb = image.getRGB(i, j);
                    float r = ((rgb & 0x00ff0000) >> 16) * 1.0f / 255;
                    float g = ((rgb & 0x0000ff00) >> 8) * 1.0f / 255;
                    float b = ((rgb & 0x000000ff)) * 1.0f / 255;
                    int x = i;
                    int y = image.getHeight(null) - j - 1;
                    pfm.data[4*(pfm.width*y+x)+0] = r;
                    pfm.data[4*(pfm.width*y+x)+1] = g;
                    pfm.data[4*(pfm.width*y+x)+2] = b;
                    pfm.data[4*(pfm.width*y+x)+3] = 1;
                }
            }

            pfm.save(pfmFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
