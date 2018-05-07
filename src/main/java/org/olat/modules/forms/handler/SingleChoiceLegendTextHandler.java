/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.forms.handler;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.model.xml.SingleChoice;
import org.olat.modules.forms.ui.LegendTextController;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.model.LegendTextDataSource;
import org.olat.modules.forms.ui.model.SingleChoiceLegendTextDataSource;
import org.olat.modules.portfolio.ui.editor.PageElement;

/**
 * 
 * Initial date: 07.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceLegendTextHandler implements EvaluationFormReportHandler {

	@Override
	public String getType() {
		return "sclegendtext";
	}

	@Override
	public Component getReportComponent(UserRequest ureq, WindowControl windowControl, PageElement element,
			List<? extends EvaluationFormSessionRef> sessions, ReportHelper reportHelper) {
		if (element instanceof SingleChoice) {
			SingleChoice singleChoice = (SingleChoice) element;
			LegendTextDataSource dataSource = new SingleChoiceLegendTextDataSource(singleChoice, sessions);
			Controller ctrl = new LegendTextController(ureq, windowControl, dataSource, reportHelper);
			return ctrl.getInitialComponent();
		}
		return null;
	}

}
