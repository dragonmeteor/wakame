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

package wakame.app;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wakame.Constants;
import wakame.Parser;
import wakame.Scene;
import wakame.WakameObject;
import wakame.block.BlockGenerator;
import wakame.block.ImageBlock;
import wakame.camera.Camera;
import wakame.integrator.Integrator;
import wakame.rfilter.ReconstructionFilter;
import wakame.sampler.Sampler;
import wakame.struct.Color3d;
import wakame.struct.Ray;
import yondoko.image.Pfm;
import yondoko.util.FileResolver;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.*;

public class Main {
    /**
     * The logger
     */
    private static Logger logger = LoggerFactory.getLogger(Main.class);
    /**
     * Convenient fields so that we don't have to pass them through methods.
     */
    private ImageBlock image;
    private BlockGenerator blockGenerator;
    private Scene scene;
    private ExecutorService executor;
    private CompletionService completionService;

    public void renderBlock(ImageBlock block, Sampler sampler) {
        Camera camera = scene.getCamera();
        Integrator integrator = scene.getIntegrator();

        int offsetX = block.getOffsetX();
        int offsetY = block.getOffsetY();
        int sizeX = block.getSizeX();
        int sizeY = block.getSizeY();

        /* Clear the block contents */
        block.clear();

        javax_.vecmath.Vector2d samplePosition = new javax_.vecmath.Vector2d();
        javax_.vecmath.Vector2d apertureSample = new javax_.vecmath.Vector2d();
        javax_.vecmath.Vector2d mu0 = new javax_.vecmath.Vector2d();
        Ray ray = new Ray();
        Color3d sampleWeight = new Color3d();
        Color3d radiance = new Color3d();

        /* For each pixel and pixel sample sample */
        for (int y = 0; y < sizeY; ++y) {
            for (int x = 0; x < sizeX; ++x) {
                for (int i = 0; i < sampler.getSampleCount(); ++i) {
                    sampler.next2D(mu0);
                    samplePosition.set(x + offsetX + mu0.x, y + offsetY + mu0.y);
                    sampler.next2D(apertureSample);

                    /* Sample a ray from the camera */
                    camera.sampleRay(samplePosition, apertureSample, ray, sampleWeight);
                    //System.out.println(ray);

                    /* Compute the incident radiance */
                    radiance.set(0,0,0);
                    integrator.Li(scene, sampler, ray, radiance);
                    radiance.mul(sampleWeight);

                    /* Store in the image block */
                    block.put(samplePosition.x, samplePosition.y, radiance);
                }
            }
        }
    }

    public void render(Scene scene, String fileName) {
        this.scene = scene;
        Camera camera = scene.getCamera();
        javax_.vecmath.Point2i outputSize = new javax_.vecmath.Point2i();
        camera.getOutputSize(outputSize);
        scene.getIntegrator().preprocess(scene);

        /* Create a block generator (i.e. a work scheduler) */
        blockGenerator = new BlockGenerator(outputSize.x, outputSize.y, Constants.BLOCK_SIZE);

        /* Allocate memory for the entire output image and clear it */
        image = new ImageBlock(outputSize.x, outputSize.y, camera.getReconstructionFilter());
        image.clear();

        /** Determine the number of threads to use to render the image.
         * Change the numThreads variable to change the number of threads used.
         */
        int numThreads = Runtime.getRuntime().availableProcessors();
        //numThreads = 1;
        executor = Executors.newFixedThreadPool(numThreads);
        completionService = new ExecutorCompletionService(executor);

        // Start time.
        long start = System.currentTimeMillis();

        // Submit the jobs.
        int numBlocks = blockGenerator.getBlockLeft();
        for (int i = 0; i < numBlocks; i++) {
            completionService.submit(new BlockRender());
        }

        final RenderingProgressFrame frame = new RenderingProgressFrame(image);
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        frame.setSize(RenderingProgressFrame.WINDOW_WIDTH, RenderingProgressFrame.WINDOW_HEIGHT);
                        frame.setVisible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < numBlocks; i++) {
                Future<Integer> future = completionService.take();
                future.get();
                String message = String.format("Rendered %d blocks out of %d blocks (%03.2f%%)",
                        i + 1, numBlocks, (i + 1) * 100.0 / numBlocks);
                logger.info(message);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // Print the rendering time.
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        logger.info(String.format("Rendering took %d min(s) %d second(s) %d ms",
                elapsed / (60*1000), (elapsed / 1000) % 60, elapsed % 1000));

        /* Determine the filename of the output bitmap */
        String pfmFileName = FilenameUtils.removeExtension(fileName) + ".pfm";

        /* Now turn the rendered image block into
        a properly normalized bitmap */
        Pfm pfm = new Pfm(outputSize.x, outputSize.y);
        javax_.vecmath.Vector4d[][] data = image.getData();
        javax_.vecmath.Vector3d color = new javax_.vecmath.Vector3d();
        int borderSize = image.getBorderSize();
        for (int y = 0; y < outputSize.y; y++) {
            for (int x = 0; x < outputSize.x; x++) {
                javax_.vecmath.Vector4d d = data[y + borderSize][x + borderSize];
                color.x = d.x / d.w;
                color.y = d.y / d.w;
                color.z = d.z / d.w;
                pfm.setColor(x, outputSize.y-y-1, color);
            }
        }

        try {
            logger.info(String.format("Writing a %dx%d PFM file to \"%s\"", image.getSizeX(), image.getSizeY(),
                    pfmFileName));
            pfm.save(pfmFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdown();
        //frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public void run(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java wakame.app.Main <scene.xml>");
            System.exit(0);
        }

        String path = args[0];
        String extension = FilenameUtils.getExtension(path).toLowerCase();
        if (extension.equals("xml")) {
            // This is a scene file, so render it.

            // Add the parent directory of the scene file to the file resolver.
            // This way, the XML file can reference resources (OBJ files, texture) using relative paths.
            FileResolver.append(new File(path).getParent());
            WakameObject obj = Parser.loadFromXML(path);
            if (obj instanceof Scene) {
                Scene scene = (Scene) obj;

                System.out.println("\n");
                System.out.println("Configuration: " + scene.toString());
                System.out.println("\n");

                render(scene, path);
            }
        } else if (extension.equals("pfm")) {
            // Alternatively provide a basic PFM image viewer.
            ViewPfm.main(args);
        } else {
            System.err.println("Fatal error: unknown file " + path +
                    ", expected an extension of type .json or .pfm");
        }
    }

    public static void main(String[] args) {
        new Main().run(args);
    }

    public class BlockRender implements Callable<Integer> {

        public BlockRender() {
            // NO-OP
        }

        @Override
        public Integer call() throws Exception {
            /* Allocate memory for a small image block to be rendered
               by the current thread */
            ReconstructionFilter filter = scene.getCamera().getReconstructionFilter();
            ImageBlock block = new ImageBlock(Constants.BLOCK_SIZE, Constants.BLOCK_SIZE, filter);

            /* Create a clone of the sampler for the current thread */
            Sampler sampler = (Sampler) scene.getSampler().clone();

            /* Request an image block from the block generator */
            blockGenerator.next(block);

            //System.out.println(String.format("(%d,%d)", block.getOffsetX(), block.getOffsetY()));

            /* Inform the sampler about the block to be rendered */
            sampler.prepare(block);

            /* Render all contained pixels */
            renderBlock(block, sampler);

            /* The image block has been processed. Now add it to
            the "big" block that represents the entire image */
            image.put(block);

            return null;
        }
    }
}
