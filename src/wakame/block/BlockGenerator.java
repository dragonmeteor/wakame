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

package wakame.block;

/**
 * Spiraling block generator
 *
 * This class can be used to chop up an image into many small
 * rectangular blocks suitable for parallel rendering. The blocks
 * are ordered in spiraling pattern so that the center is
 * rendered first.
 */
public class BlockGenerator {
    public static final int RIGHT = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int UP = 3;

    public int blockX = 0;
    public int blockY = 0;
    public int numBlockX = 0;
    public int numBlockY = 0;
    public int sizeX = 0;
    public int sizeY = 0;
    public int blockSize;
    public int numSteps;
    public int blocksLeft;
    public int stepsLeft;
    public int direction;

    /**
     * Create a block generator
     * @param sizeX the width of the image
     * @param sizeY the height of the image
     * @param blockSize the maximum size of the individual block
     */
    public BlockGenerator(int sizeX, int sizeY, int blockSize) {
        numBlockX = (int)Math.ceil(sizeX * 1.0 / blockSize);
        numBlockY = (int)Math.ceil(sizeY * 1.0 / blockSize);
        blocksLeft = numBlockX*numBlockY;
        direction = RIGHT;
        blockX = numBlockX / 2;
        blockY = numBlockY / 2;
        stepsLeft = 1;
        numSteps = 1;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.blockSize = blockSize;
    }

    /**
     * Return the next block to be rendered
     *
     * This function is thread-safe.
     *
     * @param block
     * @return false if there were no more blocks
     */
    public synchronized boolean next(ImageBlock block) {
        if (blocksLeft == 0) {
            return false;
        }

        int posX = blockX * blockSize;
        int posY = blockY * blockSize;
        block.setOffset(posX, posY);
        block.setSize(
                Math.min(blockSize, sizeX - posX),
                Math.min(blockSize, sizeY - posY));

        blocksLeft--;
        if (blocksLeft == 0)
            return true;

        do {
            switch (direction) {
                case RIGHT:
                    blockX++;
                    break;
                case DOWN:
                    blockY++;
                    break;
                case LEFT:
                    blockX--;
                    break;
                case UP:
                    blockY--;
                    break;
            }

            stepsLeft--;
            if (stepsLeft == 0) {
                direction = (direction + 1) % 4;
                if (direction == LEFT || direction == RIGHT) {
                    numSteps++;
                }
                stepsLeft = numSteps;
            }
        } while (blockX < 0 || blockY < 0 || blockX >= numBlockX || blockY >= numBlockY);

        return true;
    }

    /**
     * Get the number of blocks left.
     * @return the total number of blocks left
     */
    public int getBlockLeft() {
        return blocksLeft;
    }
}
