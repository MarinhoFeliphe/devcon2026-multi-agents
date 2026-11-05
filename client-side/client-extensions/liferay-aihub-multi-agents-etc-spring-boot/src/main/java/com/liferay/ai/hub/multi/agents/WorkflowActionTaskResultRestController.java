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
@RequestMapping("/workflow/action/task-result")
@RestController
public class WorkflowActionTaskResultRestController extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(@RequestBody String json) {
		if (_log.isDebugEnabled()) {
			_log.debug(json);
		}

		JSONObject jsonObject = new JSONObject(json);

		_taskResultService.addTaskResult(
			jsonObject.optString("agentDefinitionExternalReferenceCode"),
			jsonObject.optString("output"), "r_novelTaskResults_c_novelERC",
			jsonObject.getString("novelExternalReferenceCode"));

		_workflowService.completeWorkflowTask(jsonObject);

		return ResponseEntity.ok(
		).build();
	}

	private static final Log _log = LogFactory.getLog(
		WorkflowActionTaskResultRestController.class);

	@Autowired
	private TaskResultService _taskResultService;

	@Autowired
	private WorkflowService _workflowService;

}