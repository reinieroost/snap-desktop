/*
 * Copyright (C) 2014 by Array Systems Computing Inc. http://www.array.ca
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
package org.esa.snap.graphbuilder.rcp.actions;

import org.esa.snap.graphbuilder.rcp.dialogs.BatchGraphDialog;
import org.esa.snap.framework.ui.command.CommandEvent;
import org.esa.snap.util.IconUtils;
import org.esa.snap.visat.actions.AbstractVisatAction;

public class BatchProcessingAction extends AbstractVisatAction {

    @Override
    public void actionPerformed(final CommandEvent event) {
        final BatchGraphDialog dialog = new BatchGraphDialog(getAppContext(),
                "Batch Processing", "batchProcessing", false);
        dialog.getJDialog().setIconImage(IconUtils.esaPlanetIcon.getImage());
        dialog.show();
    }

}