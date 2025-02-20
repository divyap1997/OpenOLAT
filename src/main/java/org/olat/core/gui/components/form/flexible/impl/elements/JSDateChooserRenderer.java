/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * <P>
 * Initial Date: 19.01.2007 <br>
 * 
 * @author patrickb
 */
class JSDateChooserRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		JSDateChooserComponent jsdcc = (JSDateChooserComponent) source;
		String exDate = jsdcc.getExampleDateString();
		int maxlength = exDate.length() + 4;

		//add pop js for date chooser, if componente is enabled
		sb.append("<div");
		if(!jsdcc.isDomReplacementWrapperRequired() && !jsdcc.isDomLayoutWrapper()) {
			sb.append(" id='o_c").append(jsdcc.getDispatchID()).append("'");
		}
		sb.append(" class='o_date form-inline'>");
		if (source.isEnabled()) {
			renderDateElement(sb, jsdcc, maxlength, translator);
		} else{
			renderTextElementReadonly(sb, jsdcc, maxlength, translator);
		}
		sb.append("</div>");
	}
	
	private void renderDateElement(StringOutput sb, JSDateChooserComponent jsdcc, int maxlength, Translator translator) {
		String receiverId = jsdcc.getTextElementComponent().getFormDispatchId();
		
		if (!jsdcc.isTimeOnlyEnabled()) {
			renderDateChooser(sb, jsdcc, receiverId, receiverId, jsdcc.getValue(), "o_first_date", maxlength, translator);
		}
		//input fields for hour and minute
		if (jsdcc.isDateChooserTimeEnabled() || jsdcc.isTimeOnlyEnabled()) {
			String timeOnlyCss = jsdcc.isTimeOnlyEnabled() ? " o_time_only" : "";
			renderTime(sb, jsdcc.getHour(), jsdcc.getMinute(), jsdcc.isDefaultTimeAtEndOfDay(),
					receiverId, jsdcc, "o_first_ms".concat(timeOnlyCss));
			if(jsdcc.isSecondDate() && jsdcc.isSameDay()) {
				String separator;
				if(jsdcc.getSeparator() != null) {
					separator = translator.translate(jsdcc.getSeparator());
				} else {
					separator = " - ";
				}
				renderSeparator(sb, separator);
				renderTime(sb, jsdcc.getSecondHour(), jsdcc.getSecondMinute(), jsdcc.isDefaultTimeAtEndOfDay(),
						receiverId.concat("_snd"), jsdcc, "o_second_ms".concat(timeOnlyCss));
			}
		}
		if(jsdcc.isSecondDate() && !jsdcc.isSameDay()) {
			if(jsdcc.getSeparator() != null) {
				renderSeparator(sb, translator.translate(jsdcc.getSeparator()));
			}
			if(!jsdcc.isTimeOnlyEnabled() || jsdcc.isTimeOnlyEnabled()) {
				renderDateChooser(sb, jsdcc, receiverId.concat("_snd"), receiverId, jsdcc.getSecondValue(), "o_second_date", maxlength, translator);
			}
			if (jsdcc.isDateChooserTimeEnabled()) {
				renderTime(sb, jsdcc.getSecondHour(), jsdcc.getSecondMinute(), jsdcc.isDefaultTimeAtEndOfDay(),
						receiverId.concat("_snd"), jsdcc, "o_second_ms");
			}
		}
	}
	
	private void renderSeparator(StringOutput sb, String sep) {
		sb.append("<div class='form-group o_date_separator'>").append(sep).append("</div>");
	}
	
	private void renderDateChooser(StringOutput sb, JSDateChooserComponent jsdcc, String receiverId, String onChangeId,
			String value, String cssClass, int maxlength, Translator translator) {
		TextElementComponent teC = jsdcc.getTextElementComponent();
		String format = jsdcc.getDateChooserDateFormat();
		TextElementImpl te = teC.getFormItem();
		if(value == null) {
			value = "";
		}
		
		String triggerId = "trigger_".concat(jsdcc.getFormDispatchId());
		Translator sourceTranslator = jsdcc.getElementTranslator();
		Translator dateTranslator = Util.createPackageTranslator(JSDateChooserRenderer.class, translator.getLocale());

		sb.append("<div class='form-group ").append(cssClass).append("'><div class='input-group o_date_picker'>");
		renderTextElement(sb, receiverId, onChangeId, value, jsdcc, teC, maxlength);
		//date chooser button
		sb.append("<span class='input-group-addon'>")
		  .append("<i class='o_icon o_icon_calendar' id=\"").append(triggerId).append("\" title=\"").appendHtmlEscaped(sourceTranslator.translate("calendar.choose")).append("\"")
		  .append(" onclick=\"jQuery('#").append(receiverId).append("').datepicker('show');\"")
		  .append(">\u00A0</i></span>")
		  .append("</div></div>");//input-group
		if (jsdcc.isButtonsEnabled()) {
			// date chooser javascript
			sb.append("<script>\n")
			.append("jQuery(function(){ jQuery('#").append(receiverId).append("').datepicker({\n")
			.append("  dateFormat:'").append(format).append("',\n")
			.append("  firstDay:1,\n")
			.append("  showOn:'focus',\n")
			.append("  showAnim: '',\n")
			.append("  monthNames:[")
			  .append("'").append(dateTranslator.translate("month.long.jan")).append("',")
			  .append("'").append(dateTranslator.translate("month.long.feb")).append("',")
			  .append("'").append(dateTranslator.translate("month.long.mar")).append("',")
			  .append("'").append(dateTranslator.translate("month.long.apr")).append("',")
			  .append("'").append(dateTranslator.translate("month.long.mai")).append("',")
			  .append("'").append(dateTranslator.translate("month.long.jun")).append("',")
			  .append("'").append(dateTranslator.translate("month.long.jul")).append("',")
			  .append("'").append(dateTranslator.translate("month.long.aug")).append("',")
			  .append("'").append(dateTranslator.translate("month.long.sep")).append("',")
			  .append("'").append(dateTranslator.translate("month.long.oct")).append("',")
			  .append("'").append(dateTranslator.translate("month.long.nov")).append("',")
			  .append("'").append(dateTranslator.translate("month.long.dec")).append("'")
			.append("],\n")
			.append("  dayNamesMin:[")
			  .append("'").append(dateTranslator.translate("day.short.so")).append("',")
			  .append("'").append(dateTranslator.translate("day.short.mo")).append("',")
			  .append("'").append(dateTranslator.translate("day.short.di")).append("',")
			  .append("'").append(dateTranslator.translate("day.short.mi")).append("',")
			  .append("'").append(dateTranslator.translate("day.short.do")).append("',")
			  .append("'").append(dateTranslator.translate("day.short.fr")).append("',")
			  .append("'").append(dateTranslator.translate("day.short.sa")).append("'")
			.append("],\n")
			.append("  showOtherMonths:true,\n");
			

			sb.append("  beforeShow:function(el, inst) {\n");
			if(jsdcc.getFormItem().getDefaultValue() != null) {
				String id = ((JSDateChooser)jsdcc.getFormItem().getDefaultValue()).getTextElementComponent().getFormDispatchId();
				sb.append("    var defDate = jQuery('#").append(id).append("').datepicker('getDate');\n")
				  .append("    jQuery('#").append(receiverId).append("').datepicker('option', 'defaultDate', defDate);\n");  
			}
			sb.append("    jQuery('#").append(receiverId).append("').attr('data-oo-validation','suspend');\n")
			  .append("  },\n");

			sb.append("  onSelect:function(){\n")
			  .append("    setFlexiFormDirty('").append(te.getRootForm().getDispatchFieldId()).append("');\n")
			  .append("    jQuery(this).focus();\n")
			  .append("    jQuery(this).change();\n");
			if(jsdcc.getFormItem().getPushDateValueTo() != null) {
				String pushId = ((JSDateChooser)jsdcc.getFormItem().getPushDateValueTo()).getTextElementComponent().getFormDispatchId();
				sb.append("    var val = jQuery('#").append(pushId).append("').val();\n")
				  .append("    if(val == null || val === '') {\n")
				  .append("      var cDate = jQuery('#").append(receiverId).append("').val();\n")
				  .append("      jQuery('#").append(pushId).append("').val(cDate);\n")
				  .append("    }");
			}
			sb.append("    jQuery('#").append(receiverId).append("').attr('data-oo-validation',null);\n")
			  .append("  }\n")
			  .append("})});")
			  .append("\n</script>");
		}
	}
	
	private void renderDateChooserDisabled(StringOutput sb, JSDateChooserComponent jsdcc, String value, String cssClass, int maxLength) {
		TextElementComponent teC = jsdcc.getTextElementComponent();
		TextElementImpl te = teC.getFormItem();
		//display size cannot be bigger the maxlenght given by dateformat

		String id = teC.getFormDispatchId();
		//read only view
		if(value == null){
			value = "";
		}
		value = StringHelper.escapeHtml(value);

		sb.append("<div class='form-group ").append(cssClass).append("'><div class='o_date_picker'>");
		sb.append("<span id='").append(id).append("_wp' ")
		  .append(FormJSHelper.getRawJSFor(te.getRootForm(), id, te.getAction()))
		  .append("title=\"").append(value).append("\">");
		sb.append("<input id='").append(id).append("' disabled='disabled' class='form-control o_disabled' size=\"")
		  .append(maxLength)
		  .append("\" value=\"").append(value).append("\" autocomplete=\"off\"></span></div></div>");
	}
	
	private void renderTime(StringOutput sb, int hour, int minute, boolean defaultEndOfDay, String receiverId, JSDateChooserComponent teC, String cssClass) {
		if(defaultEndOfDay && hour < 0) {
			hour = 23;
			minute = 59;
		} else if(hour < 0) {
			hour = 0;
			minute = 0;
		}
		
		sb.append("<div class='form-group o_date_ms ").append(cssClass).append("'>");
		String hId = "o_dch_".concat(receiverId);
		renderMS(sb, hId, receiverId, teC, hour);
		sb.append(" : ");
		String mId = "o_dcm_".concat(receiverId);
		renderMS(sb, mId, receiverId, teC, minute);
		sb.append("</div>");
		
		FormJSHelper.appendFlexiFormDirty(sb, teC.getFormItem().getRootForm(), hId);
		FormJSHelper.appendFlexiFormDirty(sb, teC.getFormItem().getRootForm(), mId);
		
		if(teC.getFormItem().getRootForm().isInlineValidationOn() || teC.getFormItem().isInlineValidationOn()) {
			FormJSHelper.appendValidationListeners(sb, teC.getFormItem().getRootForm(), hId, receiverId);
			FormJSHelper.appendValidationListeners(sb, teC.getFormItem().getRootForm(), mId, receiverId);
		}
	}
	
	private void renderTimeDisabled(StringOutput sb, Date date, String receiverId, String cssClass) {
		int hour;
		int minute;
		if(date == null) {
			hour = minute = 0;
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			hour = cal.get(Calendar.HOUR_OF_DAY);
			minute = cal.get(Calendar.MINUTE);
		}
		sb.append("<div class='form-group o_date_ms ").append(cssClass).append("'>");
		renderMSDisabled(sb, "o_dch_".concat(receiverId), hour);
		sb.append(" : ");
		renderMSDisabled(sb, "o_dcm_".concat(receiverId), minute);
		sb.append("</div>");
	}

	private void renderTextElementReadonly(StringOutput sb, JSDateChooserComponent jsdcc, int maxlength, Translator translator) {
		String receiverId = jsdcc.getTextElementComponent().getFormDispatchId();
		if (!jsdcc.isTimeOnlyEnabled()) {
			renderDateChooserDisabled(sb, jsdcc, jsdcc.getValue(), "o_first_date", maxlength);
		}
		//input fields for hour and minute
		if (jsdcc.isDateChooserTimeEnabled() || jsdcc.isTimeOnlyEnabled()) {
			String timeOnlyCss = jsdcc.isTimeOnlyEnabled() ? " o_time_only" : "";
			renderTimeDisabled(sb, jsdcc.getDate(), receiverId, "o_first_ms".concat(timeOnlyCss));
			if(jsdcc.isSecondDate() && jsdcc.isSameDay()) {
				String separator;
				if(jsdcc.getSeparator() != null) {
					separator = translator.translate(jsdcc.getSeparator());
				} else {
					separator = " - ";
				}
				renderSeparator(sb, separator);
				renderTimeDisabled(sb, jsdcc.getSecondDate(), receiverId.concat("_snd"), "o_second_ms");
			}
		}
		if(jsdcc.isSecondDate() && !jsdcc.isSameDay()) {
			if(jsdcc.getSeparator() != null) {
				renderSeparator(sb, translator.translate(jsdcc.getSeparator()));
			}
			renderDateChooserDisabled(sb, jsdcc, jsdcc.getSecondValue(), "o_second_date", maxlength);
			if (jsdcc.isDateChooserTimeEnabled()) {
				renderTimeDisabled(sb, jsdcc.getSecondDate(), receiverId.concat("_snd"), "o_second_ms");
			}
		}
	}
	
	private StringOutput renderMS(StringOutput dc, String id, String receiverId, JSDateChooserComponent teC, int time) {
		TextElementImpl te = teC.getTextElementComponent().getFormItem();
		boolean actionDateOnly = teC.getFormItem().isActionDateOnly();
		int action = actionDateOnly ? 0 : te.getAction();
		
		dc.append("<input class='form-control o_date_ms' type='text' id='").append(id).append("'")
	      .append(" name=\"").append(id).append("\" size='2'")
		  .append(" maxlength='4' value='").append(time > 9 ? "" + time : "0" + time).append("'")
		  .append(" data-oo-validation-group='").append(receiverId).append("' ")
		  .append(FormJSHelper.getRawJSFor(te.getRootForm(), receiverId, action, false, null, id))
		  .append(" autocomplete=\"off\">");
		return dc;
	}
	
	private StringOutput renderMSDisabled(StringOutput dc, String id, int time) {
		dc.append("<input class='form-control o_disabled o_date_ms' disabled='disabled' type='text' id='").append(id).append("'")
	      .append(" name=\"").append(id).append("\" size='2'")
		  .append(" maxlength='2' value='").append(time > 9 ? "" + time : "0" + time).append("'")
		  .append(" autocomplete=\"off\">");
		return dc;
	}
	
	private void renderTextElement(StringOutput sb, String receiverId, String onChangeId, String value, JSDateChooserComponent jsdcc, TextElementComponent teC, int maxlength) {
		TextElementImpl te = teC.getFormItem();
		
		//display size cannot be bigger the maxlenght given by dateformat
		te.displaySize = te.displaySize <= maxlength ? te.displaySize : maxlength;
	
		//read write view
		sb.append("<input type=\"text\" class='form-control o_date_day' id=\"")
		  .append(receiverId).append("\" name=\"").append(receiverId)
		  .append("\" size=\"").append(te.displaySize)
		  .append("\" maxlength=\"").append(maxlength)
		  .append("\" value=\"").append(StringHelper.escapeHtml(value))
		  .append("\" data-oo-validation-group=\"").append(receiverId).append("\" ")
		  .append(FormJSHelper.getRawJSFor(te.getRootForm(), onChangeId, te.getAction()))
		  .append(" autocomplete=\"off\">");
		//add set dirty form only if enabled
		FormJSHelper.appendFlexiFormDirty(sb, te.getRootForm(), receiverId);
		if(jsdcc.getFormItem().getRootForm().isInlineValidationOn() || jsdcc.getFormItem().isInlineValidationOn()) {
			FormJSHelper.appendValidationListeners(sb, jsdcc.getFormItem().getRootForm(), receiverId, receiverId);
		}
	}
}
