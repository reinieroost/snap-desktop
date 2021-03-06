/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.snap.ui.product;

import com.bc.ceres.core.ProgressMonitor;
import com.vividsolutions.jts.geom.Point;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Placemark;
import org.esa.snap.core.datamodel.PlacemarkDescriptor;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductNodeEvent;
import org.esa.snap.core.datamodel.ProductNodeListenerAdapter;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.core.util.ArrayUtils;
import org.esa.snap.core.util.math.MathUtils;
import org.opengis.referencing.operation.TransformException;

import javax.swing.table.DefaultTableModel;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public abstract class AbstractPlacemarkTableModel extends DefaultTableModel {

    private final PlacemarkDescriptor placemarkDescriptor;

    private Product product;
    private Band[] selectedBands;
    private TiePointGrid[] selectedGrids;

    private final PlacemarkListener placemarkListener;
    private final ArrayList<Placemark> placemarkList = new ArrayList<>(10);

    protected AbstractPlacemarkTableModel(PlacemarkDescriptor placemarkDescriptor, Product product, Band[] selectedBands,
                                          TiePointGrid[] selectedGrids) {
        this.placemarkDescriptor = placemarkDescriptor;
        this.product = product;
        initSelectedBands(selectedBands);
        initSelectedGrids(selectedGrids);
        placemarkListener = new PlacemarkListener();
        if (product != null) {
            product.addProductNodeListener(placemarkListener);
        }
        initPlacemarkList(product);
    }

    public Placemark[] getPlacemarks() {
        return placemarkList.toArray(new Placemark[placemarkList.size()]);
    }

    public Placemark getPlacemarkAt(int modelRow) {
        return placemarkList.get(modelRow);
    }

    public PlacemarkDescriptor getPlacemarkDescriptor() {
        return placemarkDescriptor;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        if (this.product == product) {
            return;
        }
        if (this.product != null) {
            this.product.removeProductNodeListener(placemarkListener);
        }
        this.product = product;
        if (this.product != null) {
            this.product.addProductNodeListener(placemarkListener);
        }

        placemarkList.clear();
        initPlacemarkList(this.product);
        selectedBands = new Band[0];
        selectedGrids = new TiePointGrid[0];
        fireTableStructureChanged();
    }

    public Band[] getSelectedBands() {
        return selectedBands;
    }

    public void setSelectedBands(Band[] selectedBands) {
        this.selectedBands = selectedBands != null ? selectedBands : new Band[0];
        fireTableStructureChanged();
    }

    public TiePointGrid[] getSelectedGrids() {
        return selectedGrids;
    }

    public void setSelectedGrids(TiePointGrid[] selectedGrids) {
        this.selectedGrids = selectedGrids != null ? selectedGrids : new TiePointGrid[0];
        fireTableStructureChanged();
    }

    public boolean addPlacemark(Placemark placemark) {
        if (placemarkList.add(placemark)) {
            final int insertedRowIndex = placemarkList.indexOf(placemark);
            fireTableRowsInserted(insertedRowIndex, insertedRowIndex);
            return true;
        }
        return false;
    }

    public boolean removePlacemark(Placemark placemark) {
        final int index = placemarkList.indexOf(placemark);
        if (index != -1) {
            placemarkList.remove(placemark);
            fireTableRowsDeleted(index, index);
            return true;
        }
        return false;
    }

    public void removePlacemarkAt(int index) {
        if (placemarkList.size() > index) {
            final Placemark placemark = placemarkList.get(index);
            removePlacemark(placemark);
        }
    }

    public abstract String[] getStandardColumnNames();

    public String[] getAdditionalColumnNames() {
        String[] standardColumnNames = getStandardColumnNames();
        int columnCount = getColumnCount();
        final int columnCountMin = standardColumnNames.length;
        String[] additionalColumnNames = new String[columnCount - columnCountMin];
        for (int i = 0; i < additionalColumnNames.length; i++) {
            additionalColumnNames[i] = getColumnName(columnCountMin + i);
        }
        return additionalColumnNames;
    }


    @Override
    public abstract boolean isCellEditable(int rowIndex, int columnIndex);

    protected abstract Object getStandardColumnValueAt(int rowIndex, int columnIndex);

    @Override
    public int getRowCount() {
        if (placemarkList == null) {
            return 0;
        }
        return placemarkList.size();
    }

    @Override
    public int getColumnCount() {
        int count = getStandardColumnNames().length;
        if (selectedBands != null) {
            count += selectedBands.length;
        }
        if (selectedGrids != null) {
            count += selectedGrids.length;
        }
        return count;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex < getStandardColumnNames().length) {
            return getStandardColumnNames()[columnIndex];
        }
        int newIndex = columnIndex - getStandardColumnNames().length;
        if (newIndex < getNumSelectedBands()) {
            return selectedBands[newIndex].getName();
        }
        newIndex -= getNumSelectedBands();
        if (selectedGrids != null && newIndex < selectedGrids.length) {
            return selectedGrids[newIndex].getName();
        }
        return "?";
    }

    public int getColumnIndex(String columnName) {
        String[] standardColumnNames = getStandardColumnNames();
        int index = Arrays.binarySearch(standardColumnNames, columnName);
        if(index < 0) {
            int elementIndex = ArrayUtils.getElementIndex(columnName, getAdditionalColumnNames());
            if (elementIndex >= 0) {
                index = standardColumnNames.length + elementIndex;
            }
        }
        return index;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < getStandardColumnNames().length - 1) {
            return Double.class;
        }
        return Object.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex < getStandardColumnNames().length) {
            return getStandardColumnValueAt(rowIndex, columnIndex);
        }
        return getAdditionalValue(rowIndex, columnIndex);
    }

    private Object getAdditionalValue(int rowIndex, int columnIndex) {
        final Placemark placemark = placemarkList.get(rowIndex);
        int index = columnIndex - getStandardColumnNames().length;
        final Object defaultGeometry = placemark.getFeature().getDefaultGeometry();
        if (!(defaultGeometry instanceof Point)) {
            throw new IllegalStateException("A placemark must have a point feature");
        }
        final Point point = (Point) defaultGeometry;
        Point2D.Double sceneCoords = new Point2D.Double(point.getX(), point.getY());

        if (index < getNumSelectedBands()) {
            final Band band = selectedBands[index];
            final AffineTransform modelToImageTransform;
            final Point2D modelCoords;
            try {
                modelCoords = band.getSceneToModelTransform().transform(sceneCoords, new Point2D.Double());
                modelToImageTransform = band.getImageToModelTransform().createInverse();
            } catch (NoninvertibleTransformException | TransformException e) {
                return "Indeterminate";
            }
            PixelPos rasterPos = (PixelPos) modelToImageTransform.transform(modelCoords, new PixelPos());
            final int x = MathUtils.floorInt(rasterPos.getX());
            final int y = MathUtils.floorInt(rasterPos.getY());
            final int width = band.getRasterWidth();
            final int height = band.getRasterHeight();

            if (x < 0 || x >= width || y < 0 || y >= height) {
                return "No-data";
            }
            if (band.isPixelValid(x, y)) {
                try {
                    float[] value = null;
                    value = band.readPixels(x, y, 1, 1, value, ProgressMonitor.NULL);
                    return value[0];
                } catch (IOException ignored) {
                    return "I/O-error";
                }
            } else {
                return "NaN";
            }
        }
        index -= getNumSelectedBands();
        if (index < selectedGrids.length) {
            final TiePointGrid grid = selectedGrids[index];
            final AffineTransform modelToImageTransform;
            final Point2D modelCoords;
            try {
                modelCoords = grid.getSceneToModelTransform().transform(sceneCoords, new Point2D.Double());
                modelToImageTransform = grid.getImageToModelTransform().createInverse();
            } catch (NoninvertibleTransformException | TransformException e) {
                return "Indeterminate";
            }
            PixelPos rasterPos = (PixelPos) modelToImageTransform.transform(modelCoords, new PixelPos());
            final int x = MathUtils.floorInt(rasterPos.getX());
            final int y = MathUtils.floorInt(rasterPos.getY());
            final int width = grid.getRasterWidth();
            final int height = grid.getRasterHeight();

            if (x < 0 || x >= width || y < 0 || y >= height) {
                return "No-data";
            }
            try {
                float[] value = null;
                value = grid.readPixels(x, y, 1, 1, value, ProgressMonitor.NULL);
                return value[0];
            } catch (IOException ignored) {
                return "I/O-error";
            }
        }

        return "";
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (value == null) {
            return;
        }
        if (columnIndex < getStandardColumnNames().length) {
            Placemark placemark = placemarkList.get(rowIndex);
            if (columnIndex == 0) {
                setPixelPosX(value, placemark);
            } else if (columnIndex == 1) {
                setPixelPosY(value, placemark);
            } else if (columnIndex == 2) {
                this.setGeoPosLon(value, placemark);
            } else if (columnIndex == 3) {
                setGeoPosLat(value, placemark);
            } else if (columnIndex == getStandardColumnNames().length - 1) {
                String strValue = value.toString();
                placemark.setLabel(strValue);
            } else {
                throw new IllegalStateException(
                        "Column[" + columnIndex + "] '" + getColumnName(columnIndex) + "' is not editable");
            }
        }
    }

    public void dispose() {
        if (product != null) {
            product.removeProductNodeListener(placemarkListener);
        }
        selectedBands = null;
        selectedGrids = null;
        placemarkList.clear();
    }

    protected void setGeoPosLat(Object lat, Placemark placemark) {
        double lon = placemark.getGeoPos() == null ? Double.NaN : placemark.getGeoPos().lon;
        placemark.setGeoPos(new GeoPos((Double) lat, lon));
    }

    protected void setGeoPosLon(Object lon, Placemark placemark) {
        double lat = placemark.getGeoPos() == null ? Double.NaN : placemark.getGeoPos().lat;
        placemark.setGeoPos(new GeoPos(lat, (Double) lon));
    }

    protected void setPixelPosY(Object value, Placemark placemark) {
        double pixelX = placemark.getPixelPos() == null ? -1 : placemark.getPixelPos().x;
        placemark.setPixelPos(new PixelPos(pixelX, (Double) value));
    }

    protected void setPixelPosX(Object value, Placemark placemark) {
        double pixelY = placemark.getPixelPos() == null ? -1 : placemark.getPixelPos().y;
        placemark.setPixelPos(new PixelPos((Double) value, pixelY));
    }

    private void initSelectedBands(Band[] selectedBands) {
        this.selectedBands = selectedBands != null ? selectedBands : new Band[0];
    }

    private void initSelectedGrids(TiePointGrid[] selectedGrids) {
        this.selectedGrids = selectedGrids != null ? selectedGrids : new TiePointGrid[0];
    }

    private void initPlacemarkList(Product product) {
        if (product != null) {
            Placemark[] placemarks = placemarkDescriptor.getPlacemarkGroup(product).toArray(new Placemark[0]);
            placemarkList.addAll(Arrays.asList(placemarks));
        }
    }

    private int getNumSelectedBands() {
        return selectedBands != null ? selectedBands.length : 0;
    }

    private class PlacemarkListener extends ProductNodeListenerAdapter {

        @Override
        public void nodeChanged(ProductNodeEvent event) {
            fireTableDataChanged(event);
        }

        @Override
        public void nodeDataChanged(ProductNodeEvent event) {
            if (event.getSourceNode() instanceof Band) {
                Band sourceBand = (Band) event.getSourceNode();
                if (selectedBands != null) {
                    for (Band band : selectedBands) {
                        if (band == sourceBand) {
                            AbstractPlacemarkTableModel.this.fireTableDataChanged();
                            return;
                        }
                    }
                }
            }
            if (event.getSourceNode() instanceof TiePointGrid) {
                TiePointGrid sourceTPG = (TiePointGrid) event.getSourceNode();
                if (selectedGrids != null) {
                    for (TiePointGrid tpg : selectedGrids) {
                        if (tpg == sourceTPG) {
                            AbstractPlacemarkTableModel.this.fireTableDataChanged();
                            return;
                        }
                    }
                }
            }
        }

        private void fireTableDataChanged(ProductNodeEvent event) {
            if (event.getSourceNode() instanceof Placemark) {
                Placemark placemark = (Placemark) event.getSourceNode();
                // BEAM-1117: VISAT slows down using pins with GCP geo-coded images
                final int index = placemarkList.indexOf(placemark);
                if (index != -1) {
                    AbstractPlacemarkTableModel.this.fireTableRowsUpdated(index, index);
                }
            }
        }
    }
}
