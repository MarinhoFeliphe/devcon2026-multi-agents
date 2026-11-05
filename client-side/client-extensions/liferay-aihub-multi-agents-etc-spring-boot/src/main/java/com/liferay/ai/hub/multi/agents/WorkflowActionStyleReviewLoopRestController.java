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

/**
 * @author Feliphe Marinho
 */
@RequestMapping("/workflow/action/style-review-loop")
@RestController
public class WorkflowActionStyleReviewLoopRestController
	extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(@RequestBody String json) {
		if (_log.isDebugEnabled()) {
			_log.debug(json);
		}

		JSONObject jsonObject = new JSONObject(json);

		String agentDefinitionExternalReferenceCode = jsonObject.optString(
			"agentDefinitionExternalReferenceCode");
		String output = jsonObject.optString("output");
		String styledStoryExternalReferenceCode = jsonObject.getString(
			"styledStoryExternalReferenceCode");

		_taskResultService.addTaskResult(
			agentDefinitionExternalReferenceCode, output,
			"r_styledStoryTaskResults_c_styledStoryERC",
			styledStoryExternalReferenceCode);

		double score = 0;

		try {
			score = Double.parseDouble(output.trim());
		}
		catch (NumberFormatException numberFormatException) {
			throw new IllegalArgumentException(
				"Output \"" + output + "\" is not a score",
				numberFormatException);
		}

		int iteration = _taskResultService.getTaskResultsCount(
			agentDefinitionExternalReferenceCode,
			"r_styledStoryTaskResults_c_styledStoryERC",
			styledStoryExternalReferenceCode);

		String transitionName = "styleEditor";

		if ((score >= _MINIMUM_SCORE) || (iteration >= _MAXIMUM_ITERATIONS)) {
			transitionName = "end";
		}

		_log.info(
			"Chose transition " + transitionName + " for score " + score +
				" at iteration " + iteration);

		_workflowService.completeWorkflowTask(jsonObject, transitionName);

		return ResponseEntity.ok(
		).build();
	}

	private static final int _MAXIMUM_ITERATIONS = 5;

	private static final double _MINIMUM_SCORE = 0.8;

	private static final Log _log = LogFactory.getLog(
		WorkflowActionStyleReviewLoopRestController.class);

	@Autowired
	private TaskResultService _taskResultService;

	@Autowired
	private WorkflowService _workflowService;

}