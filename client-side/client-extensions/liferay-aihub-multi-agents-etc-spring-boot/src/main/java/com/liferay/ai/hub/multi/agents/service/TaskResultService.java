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

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Feliphe Marinho
 */
@Service
public class TaskResultService extends BaseService {

	public JSONObject addTaskResult(
		String agentDefinitionExternalReferenceCode, String output,
		String relationshipObjectFieldName, String externalReferenceCode) {

		_log.info(
			"Creating task result for agent " +
				agentDefinitionExternalReferenceCode + " and " +
					relationshipObjectFieldName + " " + externalReferenceCode);

		JSONObject taskResultJSONObject = new JSONObject(
			post(
				_getAuthorization(),
				new JSONObject(
				).put(
					relationshipObjectFieldName, externalReferenceCode
				).put(
					"assignee", agentDefinitionExternalReferenceCode
				).put(
					"output", output
				).toString(),
				URI.create("/o/c/taskresults")));

		_log.info(
			"Created task result " + taskResultJSONObject.getLong("id") +
				" for agent " + agentDefinitionExternalReferenceCode + " and " +
					relationshipObjectFieldName + " " + externalReferenceCode);

		return taskResultJSONObject;
	}

	public JSONObject getLatestTaskResult(
		String agentDefinitionExternalReferenceCode,
		String relationshipObjectFieldName, String externalReferenceCode) {

		_log.info(
			"Getting the latest task result for agent " +
				agentDefinitionExternalReferenceCode + " and " +
					relationshipObjectFieldName + " " + externalReferenceCode);

		JSONArray itemsJSONArray = _getTaskResultsPageJSONObject(
			agentDefinitionExternalReferenceCode, relationshipObjectFieldName,
			externalReferenceCode
		).getJSONArray(
			"items"
		);

		if (itemsJSONArray.isEmpty()) {
			throw new IllegalStateException(
				"No task result for agent " +
					agentDefinitionExternalReferenceCode + " and " +
						relationshipObjectFieldName + " " +
							externalReferenceCode);
		}

		JSONObject taskResultJSONObject = itemsJSONArray.getJSONObject(0);

		_log.info(
			"Got task result " + taskResultJSONObject.getLong("id") +
				" for agent " + agentDefinitionExternalReferenceCode + " and " +
					relationshipObjectFieldName + " " + externalReferenceCode);

		return taskResultJSONObject;
	}

	public int getTaskResultsCount(
		String agentDefinitionExternalReferenceCode,
		String relationshipObjectFieldName, String externalReferenceCode) {

		_log.info(
			"Counting task results for agent " +
				agentDefinitionExternalReferenceCode + " and " +
					relationshipObjectFieldName + " " + externalReferenceCode);

		int count = _getTaskResultsPageJSONObject(
			agentDefinitionExternalReferenceCode, relationshipObjectFieldName,
			externalReferenceCode
		).getInt(
			"totalCount"
		);

		_log.info(
			"Counted " + count + " task results for agent " +
				agentDefinitionExternalReferenceCode + " and " +
					relationshipObjectFieldName + " " + externalReferenceCode);

		return count;
	}

	private String _getAuthorization() {
		return _liferayOAuth2AccessTokenManager.getAuthorization(
			"liferay-aihub-multi-agents-etc-spring-boot-oahs");
	}

	private JSONObject _getTaskResultsPageJSONObject(
		String agentDefinitionExternalReferenceCode,
		String relationshipObjectFieldName, String externalReferenceCode) {

		return new JSONObject(
			get(
				_getAuthorization(),
				UriComponentsBuilder.fromPath(
					"/o/c/taskresults"
				).queryParam(
					"filter", "{filter}"
				).queryParam(
					"pageSize", 1
				).queryParam(
					"sort", "id:desc"
				).buildAndExpand(
					relationshipObjectFieldName + " eq '" +
						externalReferenceCode + "' and assignee eq '" +
							agentDefinitionExternalReferenceCode + "'"
				).encode(
				).toUri()));
	}

	private static final Log _log = LogFactory.getLog(TaskResultService.class);

	@Autowired
	private LiferayOAuth2AccessTokenManager _liferayOAuth2AccessTokenManager;

}