/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.xd.dirt.jms;

import java.util.Properties;
import java.util.Scanner;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.util.Assert;

/**
 * Runs an ActiveMQ broker on the configured URL; after the broker
 * has started you can enter test messages on the command line;
 * terminate by entering 'quit', which stops the broker.
 * <p>Used to test the source module.
 * <p>Requires a parameter containing the location of the XD
 * home directory to find the configuration for the source
 * module in the config direcrtory.
 * <p>A second parameter is used to specify the stream name (source
 * queue). If omitted, defaults to 'jmsTest'.
 * @author Gary Russell
 * @since 1.0
 *
 */
public class AmqBrokerAndTest {

	public static void main(String[] args) throws Exception {
		String xdHome = null;
		if (args.length > 0) {
			xdHome = args[0];
		}
		Assert.notNull(xdHome, "need an xd_home argument");
		String queueName = "jmsTest";
		if (args.length > 1) {
			queueName = args[1];
		}
		BrokerService broker = new BrokerService();
		Properties props = new Properties();
		PropertiesLoaderUtils.fillProperties(props,
				new FileSystemResource(xdHome + "/config/jms-activemq.properties"));
		String brokerURL = props.getProperty("amq.url");
		broker.addConnector(brokerURL);
		broker.start();

		ConnectionFactory cf = new ActiveMQConnectionFactory(brokerURL);
		CachingConnectionFactory ccf = new CachingConnectionFactory(cf);

		ActiveMQQueue queue = new ActiveMQQueue(queueName);

		JmsTemplate template = new JmsTemplate(ccf);
		template.setDefaultDestination(queue);

		System.out.println("Enter test messages for destination " +
				queueName + ", 'quit' to end");
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.equalsIgnoreCase("quit")) {
				break;
			}
			template.convertAndSend(line);
		}
		scanner.close();
		broker.stop();
	}

}
