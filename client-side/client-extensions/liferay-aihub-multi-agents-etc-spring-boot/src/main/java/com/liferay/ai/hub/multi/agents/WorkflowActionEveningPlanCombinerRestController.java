/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.multi.agents;

import com.liferay.ai.hub.multi.agents.service.TaskResultService;
import com.liferay.ai.hub.multi.agents.service.WorkflowService;
import com.liferay.client.extension.util.spring.boot3.BaseRestController;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

/**
 * @author Feliphe Marinho
 */
@RequestMapping("/workflow/action/evening-plan-combiner")
@RestController
public class WorkflowActionEveningPlanCombinerRestController
	extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(@RequestBody String json) {
		if (_log.isDebugEnabled()) {
			_log.debug(json);
		}

		JSONObject jsonObject = new JSONObject(json);

		String eveningPlanExternalReferenceCode = jsonObject.getString(
			"eveningPlanExternalReferenceCode");

		// Read the outputs of the parallel branches from their task results
		// because both branches write the same output key of the workflow
		// context

		List<String> meals = _getLines(
			"L_FOOD_EXPERT", eveningPlanExternalReferenceCode);
		List<String> movies = _getLines(
			"L_MOVIE_EXPERT", eveningPlanExternalReferenceCode);

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < Math.min(meals.size(), movies.size()); i++) {
			sb.append("<p>");
			sb.append(i + 1);
			sb.append(". Movie: ");
			sb.append(HtmlUtils.htmlEscape(movies.get(i)));
			sb.append(", Meal: ");
			sb.append(HtmlUtils.htmlEscape(meals.get(i)));
			sb.append("</p>");
		}

		_taskResultService.addTaskResult(
			_ASSIGNEE, sb.toString(),
			"r_eveningPlanTaskResults_c_eveningPlanERC",
			eveningPlanExternalReferenceCode);

		_workflowService.completeWorkflowTask(jsonObject);

		return ResponseEntity.ok(
		).build();
	}

	private List<String> _getLines(
		String agentDefinitionExternalReferenceCode,
		String eveningPlanExternalReferenceCode) {

		JSONObject taskResultJSONObject =
			_taskResultService.getLatestTaskResult(
				agentDefinitionExternalReferenceCode,
				"r_eveningPlanTaskResults_c_eveningPlanERC",
				eveningPlanExternalReferenceCode);

		List<String> lines = new ArrayList<>();

		Matcher matcher = _paragraphPattern.matcher(
			taskResultJSONObject.optString("output"));

		while (matcher.find()) {
			lines.add(HtmlUtils.htmlUnescape(matcher.group(1)));
		}

		return lines;
	}

	private static final String _ASSIGNEE = "EVENING_PLANNER";

	private static final Log _log = LogFactory.getLog(
		WorkflowActionEveningPlanCombinerRestController.class);

	private static final Pattern _paragraphPattern = Pattern.compile(
		"<p>(.*?)</p>");

	@Autowired
	private TaskResultService _taskResultService;

	@Autowired
	private WorkflowService _workflowService;

}