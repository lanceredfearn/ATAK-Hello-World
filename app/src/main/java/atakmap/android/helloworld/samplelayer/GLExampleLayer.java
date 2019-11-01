
package com.atakmap.android.helloworld.samplelayer;

import android.graphics.Bitmap;
import android.util.Pair;

import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.map.MapRenderer;
import com.atakmap.map.layer.Layer;
import com.atakmap.map.layer.opengl.GLAbstractLayer;
import com.atakmap.map.layer.opengl.GLLayer2;
import com.atakmap.map.layer.opengl.GLLayerSpi2;
import com.atakmap.map.opengl.GLMapView;
import com.atakmap.opengl.GLES20FixedPipeline;
import com.atakmap.opengl.GLTexture;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

public class GLExampleLayer extends GLAbstractLayer {

    // The GLLayerSpi will automatically create an instance of the renderer when
    // the ExampleLayer is added to the map

    public final static GLLayerSpi2 SPI = new GLLayerSpi2() {
        @Override
        public int getPriority() {
            // ExampleLayer : Layer
            return 1;
        }

        @Override
        public GLLayer2 create(Pair<MapRenderer, Layer> object) {
            if (!(object.second instanceof ExampleLayer))
                return null;
            return new GLExampleLayer(object.first,
                    (ExampleLayer) object.second);
        }
    };

    /*************************************************************************/

    // XXX - consider number of frames as property on the ExampleLayer

    /** The number of frames that may be displayed on the map at once */
    private final static int NUM_FRAMES = 1;

    /** circular buffer of video frames */
    private GLVideoFrame frames;
    /** the next frame number */
    private int frameNum;

    private ExampleLayer subject;

    public GLExampleLayer(MapRenderer surface, ExampleLayer subject) {
        super(surface, subject);
        this.subject = subject;
        System.out.println("shb: construct");
    }

    @Override
    protected void init() {
        System.out.println("shb: init");

        super.init();

        this.frames = new GLVideoFrame();
        this.frameNum = 0;
        setData(subject.frameRGB, subject.frameWidth, subject.frameHeight,
                subject.upperLeft, subject.upperRight, subject.lowerRight,
                subject.lowerLeft);
    }

    @Override
    protected void drawImpl(GLMapView view) {
        GLVideoFrame frame;

        frame = this.frames;
        // transform the frame's corner coordinates to GL x,y
        view.forward(frame.points, frame.vertexCoordinates);

        // XXX - consider applying alpha to the frames based on time
        //       received -- custom draw method should be internally
        //       implemented on this class if desired.

        // draw the frame

        if (frame.texture == null) {
            System.out.println("shb: null texture");
            return;
        }
        frame.texture.draw(4, GLES20FixedPipeline.GL_FLOAT,
                frame.textureCoordinates, frame.vertexCoordinates);
    }

    @Override
    public void release() {
        // release all frame textures
        this.frames.texture.release();
        this.frames = null;
        super.release();
    }

    public void setData(int[] rgb, final int width, final int height,
            GeoPoint upperLeft,
            GeoPoint upperRight, GeoPoint lowerRight, GeoPoint lowerLeft) {

        // create copies of the objects we plan to offload to the GL thread
        // we are using RGB565 to cut down on bandwidth; ARGB8888 will produce
        // better quality if needed

        final Bitmap bitmap = Bitmap.createBitmap(rgb, width, height,
                Bitmap.Config.ARGB_8888);
        final GeoPoint ul = new GeoPoint(upperLeft);
        final GeoPoint ur = new GeoPoint(upperRight);
        final GeoPoint lr = new GeoPoint(lowerRight);
        final GeoPoint ll = new GeoPoint(lowerLeft);

        // offload the actual update to the GL thread -- GL objects may only be
        // updated on the GL thread (e.g. texture).
        this.renderContext.queueEvent(new Runnable() {
            public void run() {
                try {
                    videoFrameImpl(bitmap, width, height, ul, ur, lr, ll);
                } finally {
                    // cleanup the bitmap
                    bitmap.recycle();
                }
            }
        });
    }

    public void videoFrameImpl(Bitmap bitmap, int width, int height,
            GeoPoint upperLeft,
            GeoPoint upperRight, GeoPoint lowerRight, GeoPoint lowerLeft) {

        // guard against 'release' occuring before event queue has been fully
        // processed
        if (this.frames == null)
            return;

        this.frames.update(bitmap, width, height, upperLeft, upperRight,
                lowerRight, lowerLeft);
    }

    /**************************************************************************/

    private static class GLVideoFrame {
        public GLTexture texture;
        public DoubleBuffer points;
        public FloatBuffer vertexCoordinates;
        public ByteBuffer textureCoordinates;

        public GLVideoFrame() {
            this.texture = null;
            this.points = ByteBuffer.allocateDirect(8 * 2 * 4)
                    .order(ByteOrder.nativeOrder()).asDoubleBuffer();
            this.vertexCoordinates = ByteBuffer.allocateDirect(4 * 2 * 4)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            this.textureCoordinates = ByteBuffer.allocateDirect(4 * 2 * 4)
                    .order(ByteOrder.nativeOrder());
        }

        // XXX - we could use a java.nio.Buffer in place of a bitmap that
        //       contains the pixel data. This may be faster as we won't incur
        //       additional copying during Bitmap construction. The reason
        //       Bitmap was selected is that we can explicitly clean up object
        //       when we are done; use of Buffer relies on GC. As is generally
        //       the case with mobile development we must weigh the options and
        //       experiment to determine the best utilization of resources
        //       versus performance

        public void update(Bitmap frame, final int width, final int height,
                final GeoPoint ul, final GeoPoint ur, final GeoPoint lr,
                final GeoPoint ll) {
            // if the bitmap data exceeds the bounds of the texture, allocate a
            // new instance
            if (this.texture == null
                    || (this.texture.getTexWidth() < width || this.texture
                            .getTexHeight() < height)) {
                if (this.texture != null)
                    this.texture.release();
                this.texture = new GLTexture(width, height, frame.getConfig());
            }

            this.texture.load(null, 0, 0, width, height);

            // note that while 'v' originates in the lower-left, by using an
            // upper-left origin we will have the GPU do the vertical flip for
            // us

            // update the texture coordinates to match the size of the new frame
            this.textureCoordinates.clear();
            this.textureCoordinates.putFloat(0.0f); // upper-left
            this.textureCoordinates.putFloat(0.0f);
            this.textureCoordinates.putFloat((float) width
                    / (float) this.texture.getTexWidth()); // upper-right
            this.textureCoordinates.putFloat(0.0f);
            this.textureCoordinates.putFloat((float) width
                    / (float) this.texture.getTexWidth()); // lower-right
            this.textureCoordinates.putFloat((float) height
                    / (float) this.texture.getTexHeight());
            this.textureCoordinates.putFloat(0.0f); // lower-left
            this.textureCoordinates.putFloat((float) height
                    / (float) this.texture.getTexHeight());
            this.textureCoordinates.flip();

            // update the corner coordinates for the frame; pairs are ordered
            // X, Y (longitude, latitude)
            this.points.clear();
            this.points.put(ul.getLongitude());
            this.points.put(ul.getLatitude());
            this.points.put(ur.getLongitude());
            this.points.put(ur.getLatitude());
            this.points.put(lr.getLongitude());
            this.points.put(lr.getLatitude());
            this.points.put(ll.getLongitude());
            this.points.put(ll.getLatitude());
            this.points.flip();

            // upload the bitmap data
            this.texture.load(frame);
        }
    }
}
