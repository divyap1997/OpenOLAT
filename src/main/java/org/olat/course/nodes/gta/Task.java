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
package org.olat.course.nodes.gta;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.Role;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface Task extends TaskRef {
	
	public Date getSubmissionDate();
	
	public Role getSubmissionDoerRole();
	
	public Date getSubmissionRevisionsDate();
	
	public Role getSubmissionRevisionsDoerRole();
	
	public Date getCollectionDate();
	
	public Date getCollectionRevisionsDate();
	
	public Date getAcceptationDate();
	
	public Date getSolutionDate();
	
	public Date getGraduationDate();
	
	public Date getAllowResetDate();

	public Identity getAllowResetIdentity();
	
	public TaskList getTaskList();
	
	public Identity getIdentity();
	
	public BusinessGroup getBusinessGroup();

}
