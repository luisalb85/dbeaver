/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ui.editors.sql.util;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorBase;
import org.jkiss.dbeaver.ui.perspective.AbstractPartListener;

public class SQLInformationProvider implements IInformationProvider, IInformationProviderExtension, IInformationProviderExtension2 {

    class EditorWatcher extends AbstractPartListener {

        @Override
        public void partClosed(IWorkbenchPart part)
        {
            if (part == editor) {
                editor.getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);
                partListener = null;
            }
        }

        /**
         * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
         */
        @Override
        public void partActivated(IWorkbenchPart part)
        {
            update();
        }

        @Override
        public void partBroughtToTop(IWorkbenchPart part)
        {
            update();
        }
    }

    protected IEditorPart editor;
    protected IPartListener partListener;

    protected String currentPerspective;
    protected SQLAnnotationHover implementation;
    protected IInformationControlCreator informationControlCreator;

    public SQLInformationProvider(SQLEditorBase editor)
    {
        this.editor = editor;
        implementation = new SQLAnnotationHover(editor);

        if (this.editor != null) {

            partListener = new EditorWatcher();
            IWorkbenchWindow window = this.editor.getSite().getWorkbenchWindow();
            window.getPartService().addPartListener(partListener);

            update();
        }
    }

    protected void update()
    {

        IWorkbenchWindow window = editor.getSite().getWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        if (page != null) {

            IPerspectiveDescriptor perspective = page.getPerspective();
            if (perspective != null) {
                String perspectiveId = perspective.getId();

                if (currentPerspective == null || !currentPerspective.equals(perspectiveId)) {
                    currentPerspective = perspectiveId;

                    implementation.setEditor(editor);
                }
            }
        }
    }

    @Override
    public IRegion getSubject(ITextViewer textViewer, int offset)
    {

        if (implementation != null) {
            return implementation.getHoverRegion(textViewer, offset);
        }

        return null;
    }

    /*
     * @deprecated
     */
    @Override
    public String getInformation(ITextViewer textViewer, IRegion subject)
    {
        Object information = getInformation2(textViewer, subject);
        return information == null ? null : information.toString();
    }

    @Override
    public Object getInformation2(ITextViewer textViewer, IRegion subject)
    {
        if (implementation != null) {
            Object s = implementation.getHoverInfo2(textViewer, subject);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.text.information.IInformationProviderExtension2#getInformationPresenterControlCreator()
     */
    @Override
    public IInformationControlCreator getInformationPresenterControlCreator()
    {
/*
        IInformationControlCreator controlCreator = null;
        if (implementation != null) {
            controlCreator = implementation.getInformationPresenterControlCreator();
        }
        if (controlCreator != null) {
            return controlCreator;
        }

*/
        if (informationControlCreator == null) {
            informationControlCreator = new IInformationControlCreator() {
                @Override
                public IInformationControl createInformationControl(Shell shell)
                {
                    //boolean cutDown = false;
                    //int style = cutDown ? SWT.NONE : (SWT.V_SCROLL | SWT.H_SCROLL);
                    return new DefaultInformationControl(shell, true);
                }
            };
        }
        return informationControlCreator;
    }

}