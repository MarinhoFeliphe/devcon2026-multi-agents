/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.multi.agents;

import com.liferay.ai.hub.multi.agents.service.TaskResultService;
import com.liferay.ai.hub.multi.agents.service.WorkflowService;
import com.liferay.client.extension.util.spring.boot3.BaseRestController;

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
@RequestMapping("/workflow/action/evening-plan-task-result")
@RestController
public class WorkflowActionEveningPlanTaskResultRestController
	extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(@RequestBody String json) {
		if (_log.isDebugEnabled()) {
			_log.debug(json);
		}

		JSONObject jsonObject = new JSONObject(json);

		_taskResultService.addTaskResult(
			jsonObject.optString("agentDefinitionExternalReferenceCode"),
			_toParagraphs(jsonObject.optString("output")),
			"r_eveningPlanTaskResults_c_eveningPlanERC",
			jsonObject.getString("eveningPlanExternalReferenceCode"));

		_workflowService.completeWorkflowTask(jsonObject);

		return ResponseEntity.ok(
		).build();
	}

	private String _toParagraphs(String output) {

		// Task result output is rich text, which turns line breaks into
		// spaces, so keep each line of the output as its own paragraph

		StringBuilder sb = new StringBuilder();

		for (String line : output.split("\n")) {
			String trimmedLine = line.trim();

			if (!trimmedLine.isEmpty()) {
				sb.append("<p>");
				sb.append(HtmlUtils.htmlEscape(trimmedLine));
				sb.append("</p>");
			}
		}

		return sb.toString();
	}

	private static final Log _log = LogFactory.getLog(
		WorkflowActionEveningPlanTaskResultRestController.class);

	@Autowired
	private TaskResultService _taskResultService;

	@Autowired
	private WorkflowService _workflowService;

}