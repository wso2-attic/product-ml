package org.wso2.carbon.ml.ui.helper;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.wso2.carbon.ml.db.xsd.Feature;
import org.wso2.carbon.ml.db.xsd.FeatureType;
import org.wso2.carbon.ml.db.xsd.ImputeOperation;

public class DatatableHelper {

	public void populateDatatable(HttpServletResponse response,
			HttpServletRequest request, Feature[] features) throws IOException {
		StringBuilder jsonResponse = new StringBuilder();
		System.out.println("Starting....");
		jsonResponse.append("{");
		jsonResponse.append("sEcho:"+Integer.parseInt(request.getParameter("sEcho"))+",");
		jsonResponse.append("iTotalRecords:"+features.length+",");
		jsonResponse.append("iTotalDisplayRecords:"+features.length+",");		
		
		jsonResponse.append("[");
		for (Feature feature : features) {
			StringBuilder jsonArray = new StringBuilder();
			jsonArray.append("[");

			// adding features
			jsonArray.append("<span class=\"feature\">" + feature.getFieldName()
					+ "</span>,");

			// adding include/exclude check box
			jsonArray.append(buildInputCheckBox(feature.isInputSpecified())+",");

			// adding data type drop-down
			jsonArray.append(buildDataTypeSectionBox(FeatureType.class.getEnumConstants(),
					feature.getType().toString())+",");

			// adding summary statistics
			jsonArray
					.append("<div class=\"summaryStatistics\">{\"graph\":{\"type\":\"bar\", \"r\":\"50\"}}</div>,");

			// adding impute method
			jsonArray.append(buildImputeSectionBox(ImputeOperation.class.getEnumConstants(),
					feature.getImputeOperation().toString()));

			// create a JSON array with above HTML elements
			jsonResponse.append("],");
			jsonResponse.append(jsonArray.toString());
		}
		
		jsonResponse.append("]");
		response.setContentType("application/Json");
		System.out.println(jsonResponse.toString());
		response.getWriter().print(jsonResponse.toString());
	}
	
	private String buildInputCheckBox(boolean value) {
		String control = "<input type=\"checkbox\" "
				+ "class=\"includeFeature\" value=\"includeFeature\"";
		if (value) {
			control += " checked />";
		} else {
			control += "/>";
		}

		return control;

	}

	// TODO: replace these two with a parameterized method
	private String buildDataTypeSectionBox(FeatureType[] types,
			String selected) {
		StringBuilder selection = new StringBuilder();
		selection.append("<select class=\"fieldType\">");
		for (FeatureType ft : types) {
			if (ft.toString().equals(selected)) {
				selection.append("<option selected value=\"" + ft.toString()
						+ "\">" + ft.toString() + "</option>");
			} else {
				selection.append("<option value=\"" + ft.toString() + "\">"
						+ ft.toString() + "</option>");
			}

		}
		selection.append("</select>");
		return selection.toString();
	}

	private String buildImputeSectionBox(ImputeOperation[] types,
			String selected) {
		StringBuilder selection = new StringBuilder();
		selection.append("<select class=\"imputeMethod\">");
		for (ImputeOperation ft : types) {
			if (ft.toString().equals(selected)) {
				selection.append("<option selected value=\"" + ft.toString()
						+ "\">" + ft.toString() + "</option>");
			} else {
				selection.append("<option value=\"" + ft.toString() + "\">"
						+ ft.toString() + "</option>");
			}

		}
		selection.append("</select>");
		return selection.toString();
	}

}
