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
package org.olat.course.nodes.videotask.ui.components;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.olat.modules.video.VideoSegmentCategory;

/**
 * 
 * Initial date: 23 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CategoryAlphabeticalComparator implements Comparator<VideoSegmentCategory> {

	private final Collator collator;
	
	public CategoryAlphabeticalComparator(Locale locale) {
		collator = Collator.getInstance(locale);
	}

	@Override
	public int compare(VideoSegmentCategory o1, VideoSegmentCategory o2) {
		String t1 = o1.getTitle();
		String t2 = o2.getTitle();
		
		int c;
		if(t1 == null && t2 == null) {
			c = 0;
		} else if(t1 != null && t2 == null) {
			c = 1;
		} else if(t1 == null && t2 != null) {
			c = -1;
		} else {
			c = collator.compare(t1, t2);
		}
		return c;
	}
}
