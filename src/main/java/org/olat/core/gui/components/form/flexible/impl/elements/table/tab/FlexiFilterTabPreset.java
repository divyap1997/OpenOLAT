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
package org.olat.core.gui.components.form.flexible.impl.elements.table.tab;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;

/**
 * This is a tab with a preset of filters.
 * 
 * 
 * Initial date: 10 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFilterTabPreset extends FlexiFiltersTabImpl implements FlexiFiltersPreset {
	
	private List<String> enabledFilters;
	private List<String> implicitFilters;
	private List<FlexiTableFilterValue> filtersValues;
	
	public FlexiFilterTabPreset(String id, String label) {
		super(id, label);
	}
	
	public FlexiFilterTabPreset(String id, String label, TabSelectionBehavior selectionBehavior) {
		super(id, label, selectionBehavior);
	}

	/**
	 * 
	 * @param id The id of the tab, will be used for the business path
	 * @param label The label of the tab
	 * @param selectionBehavior Behavior if the tab is selected
	 * @param implicitValueFilters a list of filters and theirs values, the filters
	 * 		will be implicit, always selected and invisible to the user.
	 * @return The configuration of a tab
	 */
	public static FlexiFilterTabPreset presetWithImplicitFilters(String id, String label,
			TabSelectionBehavior selectionBehavior, List<FlexiTableFilterValue> implicitValueFilters) {
		FlexiFilterTabPreset preset = new FlexiFilterTabPreset(id, label, selectionBehavior);
		
		List<String> implicitFilters = new ArrayList<>(implicitValueFilters.size());
		for(FlexiTableFilterValue implicitValueFilter:implicitValueFilters) {
			implicitFilters.add(implicitValueFilter.getFilter());
		}
		preset.implicitFilters = implicitFilters;
		preset.filtersValues = implicitValueFilters;
		return preset;
	}
	
	/**
	 * 
	 * @param id The id of the tab, will be used for the business path
	 * @param label The label of the tab
	 * @param selectionBehavior Behavior if the tab is selected
	 * @param valueFilters a list of filters and theirs values, the filters
	 * 		will be explicit, selected and visible to the user which can change them.
	 * @return The configuration of a tab
	 */
	public static FlexiFilterTabPreset presetWithFilters(String id, String label,
			TabSelectionBehavior selectionBehavior, List<FlexiTableFilterValue> valueFilters) {
		FlexiFilterTabPreset preset = new FlexiFilterTabPreset(id, label, selectionBehavior);
		preset.implicitFilters = new ArrayList<>();
		preset.filtersValues = valueFilters;
		return preset;
	}
	
	/**
	 * 
	 * @param preset A tab to copy
	 * @return The configuration of a tab
	 */
	public static FlexiFilterTabPreset copyOf(FlexiFilterTabPreset preset) {
		FlexiFilterTabPreset copy = new FlexiFilterTabPreset(preset.getId(), preset.getLabel());
		copy.setElementCssClass(preset.getElementCssClass());

		List<String> enabled = preset.enabledFilters == null ? null : new ArrayList<>(preset.enabledFilters);
		copy.setEnabledFilters(enabled);
		List<String> impFilters = preset.implicitFilters == null ? null : new ArrayList<>(preset.implicitFilters);
		copy.setImplicitFilters(impFilters);
		List<FlexiTableFilterValue> values = preset.filtersValues == null ? null : new ArrayList<>(preset.filtersValues);
		copy.setDefaultFiltersValues(values);
		return copy;
	}

	@Override
	public List<String> getEnabledFilters() {
		return enabledFilters;
	}
	
	public void setEnabledFilters(List<String> enabledFilters) {
		this.enabledFilters = enabledFilters;
	}

	@Override
	public List<String> getImplicitFilters() {
		return implicitFilters;
	}

	public void setImplicitFilters(List<String> implicitFilters) {
		this.implicitFilters = implicitFilters;
	}

	@Override
	public List<FlexiTableFilterValue> getDefaultFiltersValues() {
		return filtersValues;
	}
	
	public void setDefaultFiltersValues(List<FlexiTableFilterValue> filtersValues) {
		this.filtersValues = filtersValues;
	}
}
