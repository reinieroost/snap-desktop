package org.esa.snap.rcp.util;

import org.esa.snap.framework.dataio.ProductSubsetDef;
import org.esa.snap.framework.datamodel.AbstractGeoCoding;
import org.esa.snap.framework.datamodel.Band;
import org.esa.snap.framework.datamodel.GeoPos;
import org.esa.snap.framework.datamodel.MetadataElement;
import org.esa.snap.framework.datamodel.PixelPos;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.datamodel.ProductData;
import org.esa.snap.framework.datamodel.Scene;
import org.esa.snap.framework.datamodel.TiePointGrid;
import org.esa.snap.framework.dataop.maptransf.Datum;

import javax.media.jai.operator.ConstantDescriptor;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Random;

/**
 * Creates product instances for testing.
 *
 * @author Norman Fomferra
 */
public class TestProducts {


    public static Product[] createProducts() {
        return new Product[]{createProduct1(), createProduct2(), createProduct3()};
    }

    public static Product createProduct1() {
        Product product = new Product("Test_Product_1", "Test_Type_1", 2048, 1024);
        product.addTiePointGrid(new TiePointGrid("Grid_A", 32, 16, 0, 0, 2048f / 32, 1024f / 16, createRandomPoints(32 * 16)));
        product.addTiePointGrid(new TiePointGrid("Grid_B", 32, 16, 0, 0, 2048f / 32, 1024f / 16, createRandomPoints(32 * 16)));
        product.addBand("Band_A", "sin(4 * PI * sqrt( sqr(X/1000.0 - 1) + sqr(Y/500.0 - 1) ))");
        product.addBand("Band_B", "sin(4 * PI * sqrt( 2.0 * abs(X/1000.0 * Y/500.0) ))");
        product.addMask("Mask_A", "Band_A > 0.5", "I am Mask A", Color.ORANGE, 0.5);
        product.addMask("Mask_B", "Band_B < 0.0", "I am Mask B", Color.RED, 0.5);
        product.getMetadataRoot().addElement(new MetadataElement("Global_Attributes"));
        product.getMetadataRoot().addElement(new MetadataElement("Local_Attributes"));
        product.setModified(false);
        double sx = 40.0 / product.getSceneRasterWidth();
        AffineTransform at = new AffineTransform();
        at.translate(-80, -30);
        at.rotate(0.3, 20.0, 10.0);
        at.scale(sx, sx);
        product.setGeoCoding(new ATGeoCoding(at));
        return product;
    }

    public static Product createProduct2() {
        Product product = new Product("Test_Product_2", "Test_Type_2", 1024, 2048);
        product.addTiePointGrid(new TiePointGrid("Grid_1", 16, 32, 0, 0, 1024f / 16, 2048f / 32, createRandomPoints(32 * 16)));
        product.addTiePointGrid(new TiePointGrid("Grid_2", 16, 32, 0, 0, 1024f / 16, 2048f / 32, createRandomPoints(32 * 16)));
        product.addBand("Band_1", "cos(X/100)-sin(Y/100)");
        product.addBand("Band_2", "sin(X/100)+cos(Y/100)");
        product.addBand("Band_3", "cos(X/100)*cos(Y/100)");
        product.addMask("Mask_1", "Band_1 > 0.5", "I am Mask 1", Color.GREEN, 0.5);
        product.addMask("Mask_2", "Band_2 < 0.0", "I am Mask 2", Color.CYAN, 0.5);
        product.addMask("Mask_3", "Band_3 > -0.1 && Band_3 < 0.1", "I am Mask 3", Color.BLUE, 0.5);
        product.getMetadataRoot().addElement(new MetadataElement("Global_Attributes"));
        product.getMetadataRoot().addElement(new MetadataElement("Local_Attributes"));
        product.setModified(false);
        double sx = 20.0 / product.getSceneRasterWidth();
        AffineTransform at = new AffineTransform();
        at.scale(sx, sx);
        at.rotate(-0.2, 10.0, 10.0);
        product.setGeoCoding(new ATGeoCoding(at));
        return product;
    }

    public static Product createProduct3() {
        int size = 10 * 1024;
        Product product = new Product("Test_Product_3", "Test_Type_3", size, size);
        product.setPreferredTileSize(512, 512);
        Band band1 = new Band("Big_Band_1", ProductData.TYPE_FLOAT64, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        Band band2 = new Band("Big_Band_2", ProductData.TYPE_FLOAT64, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        Band band3 = new Band("Big_Band_3", ProductData.TYPE_FLOAT64, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        Band band4 = new Band("Big_Band_4", ProductData.TYPE_FLOAT64, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        Band band5 = new Band("Big_Band_5", ProductData.TYPE_FLOAT64, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        band1.setSourceImage(ConstantDescriptor.create(1f * size, 1F * size, new Double[]{1.0}, null));
        band2.setSourceImage(ConstantDescriptor.create(1f * size, 1F * size, new Double[]{2.0}, null));
        band3.setSourceImage(ConstantDescriptor.create(1f * size, 1F * size, new Double[]{3.0}, null));
        band4.setSourceImage(ConstantDescriptor.create(1f * size, 1F * size, new Double[]{4.0}, null));
        band5.setSourceImage(ConstantDescriptor.create(1f * size, 1F * size, new Double[]{5.0}, null));
        product.addBand(band1);
        product.addBand(band2);
        product.addBand(band3);
        product.addBand(band4);
        product.addBand(band5);
        product.setModified(true);
        double sx = 30.0 / product.getSceneRasterWidth();
        AffineTransform at = new AffineTransform();
        at.translate(100, 0.0);
        at.rotate(0.1, 15.0, 15.0);
        at.scale(sx, sx);
        product.setGeoCoding(new ATGeoCoding(at));
        return product;
    }

    private static float[] createRandomPoints(int n) {
        Random random = new Random();
        float[] pnts = new float[n];
        for (int i = 0; i < pnts.length; i++) {
            pnts[i] = (float) random.nextGaussian();
        }
        return pnts;
    }

    private static class ATGeoCoding extends AbstractGeoCoding {
        private static final PixelPos INVALID_PIXEL_POS = new PixelPos(Double.NaN, Double.NaN);
        private final AffineTransform affineTransform;

        public ATGeoCoding(AffineTransform affineTransform) {
            this.affineTransform = affineTransform;
        }

        @Override
        public boolean transferGeoCoding(Scene srcScene, Scene destScene, ProductSubsetDef subsetDef) {
            return false;
        }

        @Override
        public boolean isCrossingMeridianAt180() {
            return false;
        }

        @Override
        public boolean canGetPixelPos() {
            return true;
        }

        @Override
        public boolean canGetGeoPos() {
            return true;
        }

        @Override
        public PixelPos getPixelPos(GeoPos geoPos, PixelPos pixelPos) {
            try {
                Point2D p = affineTransform.inverseTransform(new Point2D.Double(geoPos.lon, geoPos.lat), null);
                if (pixelPos == null) {
                    pixelPos = new PixelPos();
                }
                pixelPos.x = p.getX();
                pixelPos.y = p.getY();
                return pixelPos;
            } catch (NoninvertibleTransformException e) {
                return INVALID_PIXEL_POS;
            }
        }

        @Override
        public GeoPos getGeoPos(PixelPos pixelPos, GeoPos geoPos) {
            Point2D point2D = affineTransform.transform(pixelPos, null);
            if (geoPos == null) {
                geoPos = new GeoPos();
            }
            geoPos.lon = point2D.getX();
            geoPos.lat = point2D.getY();
            return geoPos;
        }

        @Override
        public Datum getDatum() {
            return Datum.WGS_84;
        }

        @Override
        public void dispose() {
        }
    }
}
