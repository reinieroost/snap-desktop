package org.esa.snap.rcp.placemark;

import org.esa.snap.framework.datamodel.VectorDataNode;
import org.esa.snap.framework.ui.product.ProductPlacemarkView;
import org.esa.snap.netbeans.docwin.DocumentTopComponent;
import org.esa.snap.netbeans.docwin.WindowUtilities;
import org.esa.snap.rcp.windows.ProductSceneViewTopComponent;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import java.awt.BorderLayout;

/**
 * @author Tonio Fincke
 */
@TopComponent.Description(
        preferredID = "PlacemarkViewTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "navigator",
        openAtStartup = true,
        position = 0
)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_PlacemarkViewTopComponentName",
        preferredID = "PlacemarkViewTopComponent"
)
@NbBundle.Messages({
        "CTL_PlacemarkViewTopComponentName=PlacemarkView",
})
public class PlacemarkViewTopComponent extends DocumentTopComponent<VectorDataNode> {

    public PlacemarkViewTopComponent(VectorDataNode document) {
        super(document);
        updateDisplayName();
        setName(getDisplayName());
        ProductPlacemarkView placemarkView = new ProductPlacemarkView(document);
        setLayout(new BorderLayout());
        add(placemarkView, BorderLayout.CENTER);
    }

    private void updateDisplayName() {
        setDisplayName(WindowUtilities.getUniqueTitle(getDocument().getName(),
                                                      ProductSceneViewTopComponent.class));
    }

}
