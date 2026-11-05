/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.multi.agents;

import com.liferay.client.extension.util.spring.boot3.ClientExtensionUtilSpringBootComponentScan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * @author Feliphe Marinho
 */
@Import(ClientExtensionUtilSpringBootComponentScan.class)
@SpringBootApplication
public class AIHubMultiAgentsSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(
			AIHubMultiAgentsSpringBootApplication.class, args);
	}

}