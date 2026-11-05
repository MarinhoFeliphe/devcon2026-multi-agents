/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.multi.agents.service;

import com.liferay.client.extension.util.spring.boot3.client.LiferayOAuth2AccessTokenManager;
import com.liferay.client.extension.util.spring.boot3.service.BaseService;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Feliphe Marinho
 */
@Service
public class WorkflowService extends BaseService {

	public void completeWorkflowTask(JSONObject payloadJSONObject) {
		completeWorkflowTask(
			payloadJSONObject,
			payloadJSONObject.getJSONArray(
				"nextTransitionNames"
			).getString(
				0
			));
	}

	public void completeWorkflowTask(
		JSONObject payloadJSONObject, String transitionName) {

		long workflowTaskId = payloadJSONObject.getLong("workflowTaskId");

		_log.info(
			"Completing workflow task " + workflowTaskId + " with transition " +
				transitionName);

		post(
			_getAuthorization(),
			new JSONObject(
			).put(
				"transitionName", transitionName
			).toString(),
			URI.create(payloadJSONObject.getString("transitionURL")));

		_log.info(
			"Completed workflow task " + workflowTaskId + " with transition " +
				transitionName);
	}

	public String startWorkflowInstance(
		String workflowDefinitionName, JSONObject contextJSONObject) {

		String authorization = _getAuthorization();

		_log.info(
			"Getting the latest " + workflowDefinitionName +
				" workflow definition");

		JSONObject workflowDefinitionJSONObject = new JSONObject(
			get(
				authorization,
				UriComponentsBuilder.fromPath(
					"/o/headless-admin-workflow/v1.0/workflow-definitions" +
						"/by-name/{name}"
				).buildAndExpand(
					workflowDefinitionName
				).encode(
				).toUri()));

		String workflowDefinitionVersion =
			workflowDefinitionJSONObject.getString("version");

		_log.info(
			"Got " + workflowDefinitionName + " workflow definition version " +
				workflowDefinitionVersion);

		_log.info(
			"Starting " + workflowDefinitionName + " workflow instance with " +
				"context " + contextJSONObject);

		String workflowInstanceJSON = post(
			authorization,
			new JSONObject(
			).put(
				"context", contextJSONObject
			).put(
				"workflowDefinitionName", workflowDefinitionName
			).put(
				"workflowDefinitionVersion", workflowDefinitionVersion
			).toString(),
			URI.create(
				"/o/headless-admin-workflow/v1.0/workflow-instances/submit"));

		JSONObject workflowInstanceJSONObject = new JSONObject(
			workflowInstanceJSON);

		_log.info(
			"Started " + workflowDefinitionName + " workflow instance " +
				workflowInstanceJSONObject.getLong("id"));

		return workflowInstanceJSON;
	}

	private String _getAuthorization() {
		return _liferayOAuth2AccessTokenManager.getAuthorization(
			"liferay-aihub-multi-agents-etc-spring-boot-oahs");
	}

	private static final Log _log = LogFactory.getLog(WorkflowService.class);

	@Autowired
	private LiferayOAuth2AccessTokenManager _liferayOAuth2AccessTokenManager;

}