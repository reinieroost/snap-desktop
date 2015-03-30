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
package org.esa.snap.rcp.worldmap;

import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductManager;
import org.esa.beam.framework.datamodel.ProductNode;
import org.esa.beam.framework.ui.WorldMapPane;
import org.esa.beam.framework.ui.WorldMapPaneDataModel;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.rcp.util.SelectionSupport;
import org.esa.snap.rcp.windows.ToolTopComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

@TopComponent.Description(
        preferredID = "WorldMapTopComponent",
        iconBase = "org/esa/snap/rcp/icons/WorldMap24.gif",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS //todo define
)
@TopComponent.Registration(
        mode = "navigator",
        openAtStartup = false,
        position = 1
)
@ActionID(category = "Window", id = "org.esa.snap.rcp.worldmap.WorldMapTopComponent")
@ActionReferences({
        @ActionReference(path = "Menu/Window/Tool Windows"),
        @ActionReference(path = "Toolbars/Views")
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_WorldMapTopComponent_Name",
        preferredID = "WorldMapTopComponent"
)
@NbBundle.Messages({
        "CTL_WorldMapTopComponent_Name=World Map",
        "CTL_WorldMapTopComponent_HelpId=showWorldMapWnd"
})
/**
 * The window displaying the world map.
 *
 * @author Sabine Embacher
 * @author Norman Fomferra
 * @author Marco Peters
 * @version $Revision$ $Date$
 */
public class WorldMapTopComponent extends ToolTopComponent {

    public static final String ID = WorldMapTopComponent.class.getName();

    protected WorldMapPaneDataModel worldMapDataModel;

    public WorldMapTopComponent() {
        setDisplayName(Bundle.CTL_WorldMapTopComponent_Name());
        initUI();
    }

    public void initUI() {
        setLayout(new BorderLayout());

        final JPanel mainPane = new JPanel(new BorderLayout(4, 4));
        mainPane.setPreferredSize(new Dimension(320, 160));

        worldMapDataModel = new WorldMapPaneDataModel();
        final WorldMapPane worldMapPane = new WorldMapPane(worldMapDataModel);
        worldMapPane.setNavControlVisible(true);
        mainPane.add(worldMapPane, BorderLayout.CENTER);

        final SnapApp snapApp = SnapApp.getDefault();
        snapApp.getProductManager().addListener(new WorldMapProductManagerListener());
        snapApp.getSelectionSupport(ProductNode.class).addHandler(new SelectionSupport.Handler<ProductNode>() {
            @Override
            public void selectionChange(@NullAllowed ProductNode oldValue, @NullAllowed ProductNode newValue) {
                setSelectedProduct(newValue.getProduct());
            }
        });
        setProducts(snapApp.getProductManager().getProducts());
        setSelectedProduct(snapApp.getSelectedProduct());

        add(mainPane, BorderLayout.CENTER);
    }

    public void setSelectedProduct(Product product) {
        worldMapDataModel.setSelectedProduct(product);
    }

    public Product getSelectedProduct() {
        return worldMapDataModel.getSelectedProduct();
    }


    public void setProducts(Product[] products) {
        worldMapDataModel.setProducts(products);
    }

    @Override
    protected void productSceneViewSelected(@NonNull ProductSceneView view) {
        setSelectedProduct(view.getProduct());
    }

    @Override
    protected void productSceneViewDeselected(@NonNull ProductSceneView view) {
        setSelectedProduct(null);
    }

    private class WorldMapProductManagerListener implements ProductManager.Listener {

        @Override
        public void productAdded(ProductManager.Event event) {
            final Product product = event.getProduct();
            worldMapDataModel.addProduct(product);
            setSelectedProduct(product);
        }

        @Override
        public void productRemoved(ProductManager.Event event) {
            final Product product = event.getProduct();
            if (getSelectedProduct() == product) {
                setSelectedProduct(null);
            }
            worldMapDataModel.removeProduct(product);
        }
    }

}