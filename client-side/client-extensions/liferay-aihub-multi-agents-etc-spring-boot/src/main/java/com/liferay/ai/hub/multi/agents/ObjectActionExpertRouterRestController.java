/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.multi.agents;

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
@RequestMapping("/object/action/expert-router")
@RestController
public class ObjectActionExpertRouterRestController extends BaseRestController {

	@PostMapping
	public ResponseEntity<String> post(@RequestBody String json) {
		if (_log.isDebugEnabled()) {
			_log.debug(json);
		}

		JSONObject objectEntryJSONObject = new JSONObject(
			json
		).getJSONObject(
			"objectEntry"
		);

		JSONObject valuesJSONObject = objectEntryJSONObject.getJSONObject(
			"values");

		return ResponseEntity.ok(
			_workflowService.startWorkflowInstance(
				"Expert Router",
				new JSONObject(
				).put(
					"expertRequestExternalReferenceCode",
					objectEntryJSONObject.getString("externalReferenceCode")
				).put(
					"request", valuesJSONObject.optString("request")
				)));
	}

	private static final Log _log = LogFactory.getLog(
		ObjectActionExpertRouterRestController.class);

	@Autowired
	private WorkflowService _workflowService;

}