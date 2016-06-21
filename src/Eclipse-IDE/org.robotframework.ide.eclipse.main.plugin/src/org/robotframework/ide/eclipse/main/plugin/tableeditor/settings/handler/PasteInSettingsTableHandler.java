/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.handler.PasteInSettingsTableHandler.E4PasteInSettingsTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteInSettingsTableHandler extends DIParameterizedHandler<E4PasteInSettingsTableHandler> {

    public PasteInSettingsTableHandler() {
        super(E4PasteInSettingsTableHandler.class);
    }

    public static class E4PasteInSettingsTableHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Inject
        @Named(RobotEditorSources.SUITE_FILE_MODEL)
        private RobotSuiteFile fileModel;

        @Execute
        public Object paste(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final Clipboard clipboard) {

            final List<RobotElement> selectedSettings = Selections.getElements(selection, RobotElement.class);
            final List<EditorCommand> pasteCommands = new PasteSettingsCellsCommandsCollector().collectPasteCommands(
                    editor.getSelectionLayerAccessor().getSelectionLayer(), selectedSettings, clipboard);

            for (EditorCommand command : pasteCommands) {
                commandsStack.execute(command);
            }

            return null;
        }
    }
}
