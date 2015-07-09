package org.robotframework.ide.eclipse.main.plugin;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

public interface RobotElement {

    String getName();

    String getComment();

    RobotElement getParent();

    void fixParents(RobotElement parent);

    RobotSuiteFile getSuiteFile();

    List<RobotElement> getChildren();

    ImageDescriptor getImage();

    OpenStrategy getOpenRobotEditorStrategy(IWorkbenchPage page);

    public class OpenStrategy {
        public void run() {

        }
    }
}
