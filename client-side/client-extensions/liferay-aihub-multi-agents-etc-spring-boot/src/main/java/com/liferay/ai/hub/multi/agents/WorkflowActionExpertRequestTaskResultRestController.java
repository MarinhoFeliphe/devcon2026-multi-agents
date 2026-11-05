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

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Feliphe Marinho
 */
@RequestMapping("/workflow/action/expert-request-task-result")
@RestController
public class WorkflowActionExpertRequestTaskResultRestController
	extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(@RequestBody String json) {
		if (_log.isDebugEnabled()) {
			_log.debug(json);
		}

		JSONObject jsonObject = new JSONObject(json);

		String output = jsonObject.optString("output");

		_taskResultService.addTaskResult(
			jsonObject.optString("agentDefinitionExternalReferenceCode"),
			output, "r_expertRequestTaskResults_c_expertRequestERC",
			jsonObject.getString("expertRequestExternalReferenceCode"));

		_workflowService.completeWorkflowTask(
			jsonObject,
			_getTransitionName(
				jsonObject.getJSONArray("nextTransitionNames"), output));

		return ResponseEntity.ok(
		).build();
	}

	private String _getTransitionName(
		JSONArray nextTransitionNamesJSONArray, String output) {

		if (nextTransitionNamesJSONArray.length() == 1) {
			return nextTransitionNamesJSONArray.getString(0);
		}

		// The request analyst answers with the name of the transition to the
		// chosen expert

		String trimmedOutput = output.trim();

		for (int i = 0; i < nextTransitionNamesJSONArray.length(); i++) {
			String nextTransitionName = nextTransitionNamesJSONArray.getString(
				i);

			if (nextTransitionName.equalsIgnoreCase(trimmedOutput)) {
				return nextTransitionName;
			}
		}

		throw new IllegalArgumentException(
			"Output \"" + output + "\" does not match any of the transitions " +
				nextTransitionNamesJSONArray);
	}

	private static final Log _log = LogFactory.getLog(
		WorkflowActionExpertRequestTaskResultRestController.class);

	@Autowired
	private TaskResultService _taskResultService;

	@Autowired
	private WorkflowService _workflowService;

}