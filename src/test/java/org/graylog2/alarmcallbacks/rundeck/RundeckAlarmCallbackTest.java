package org.graylog2.alarmcallbacks.rundeck;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class RundeckAlarmCallbackTest {
    private static final ImmutableMap<String, Object> VALID_CONFIG_SOURCE = ImmutableMap.<String, Object>builder()
            .put("rundeck_url", "http://rundeck.example.com")
            .put("job_id", "test-job-id")
            .put("api_token", "test_api_token")
            .put("args", "-test arg")
            .put("filter_include_node_name", "test_node1")
            .put("filter_include_hostname", "test.node.1")
            .put("filter_include_tags", "test-tag")
            .put("filter_exclude_node_name", "test_node2")
            .put("filter_exclude_hostname", "test.node.2")
            .put("filter_exclude_tags", "test-tag")
            .put("exclude_precedence", true)
            .build();
    private RundeckAlarmCallback alarmCallback;

    @Before
    public void setUp() {
        alarmCallback = new RundeckAlarmCallback();
    }

    @Test
    public void testInitialize() throws AlarmCallbackConfigurationException {
        final Configuration configuration = new Configuration(VALID_CONFIG_SOURCE);
        alarmCallback.initialize(configuration);
    }

    @Test
    public void testGetAttributes() throws AlarmCallbackConfigurationException {
        final Configuration configuration = new Configuration(VALID_CONFIG_SOURCE);
        alarmCallback.initialize(configuration);

        final Map<String, Object> attributes = alarmCallback.getAttributes();
        assertThat(attributes.keySet(), hasItems("rundeck_url", "job_id", "api_token", "args",
                "filter_include_node_name", "filter_include_hostname", "filter_include_tags",
                "filter_exclude_node_name", "filter_exclude_hostname", "filter_exclude_tags",
                "exclude_precedence"));
        assertThat((String) attributes.get("api_token"), equalTo("****"));
    }

    @Test
    public void checkConfigurationSucceedsWithValidConfiguration()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(new Configuration(VALID_CONFIG_SOURCE));
        alarmCallback.checkConfiguration();
    }

    @Test(expected = ConfigurationException.class)
    public void checkConfigurationFailsIfJobIdIsMissing()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(validConfigurationWithout("job_id"));
        alarmCallback.checkConfiguration();
    }

    @Test(expected = ConfigurationException.class)
    public void checkConfigurationFailsIfApiTokenIsMissing()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        alarmCallback.initialize(validConfigurationWithout("api_token"));
        alarmCallback.checkConfiguration();
    }

    @Test(expected = ConfigurationException.class)
    public void checkConfigurationFailsIfArgsContainsInvalidCharacters()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        final Map<String, Object> configSource = ImmutableMap.<String, Object>builder()
                .put("job_id", "TEST-job-id")
                .put("api_token", "TEST_api_token")
                .put("args", "&-invalid arg")
                .build();

        alarmCallback.initialize(new Configuration(configSource));
        alarmCallback.checkConfiguration();
    }

    @Test(expected = ConfigurationException.class)
    public void checkConfigurationFailsIfRundeckUrlIsInvalid()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        final Map<String, Object> configSource = ImmutableMap.<String, Object>builder()
                .put("job_id", "TEST-job-id")
                .put("api_token", "TEST_api_token")
                .put("rundeck_url", "Definitely$$Not#A!!URL")
                .build();

        alarmCallback.initialize(new Configuration(configSource));
        alarmCallback.checkConfiguration();
    }

    @Test(expected = ConfigurationException.class)
    public void checkConfigurationFailsIfRundeckUrlIsNotHttpOrHttps()
            throws AlarmCallbackConfigurationException, ConfigurationException {
        final Map<String, Object> configSource = ImmutableMap.<String, Object>builder()
                .put("job_id", "TEST-job-id")
                .put("api_token", "TEST_api_token")
                .put("rundeck_url", "ftp://example.net")
                .build();

        alarmCallback.initialize(new Configuration(configSource));
        alarmCallback.checkConfiguration();
    }

    @Test
    public void testGetRequestedConfiguration() {
        assertThat(alarmCallback.getRequestedConfiguration().asList().keySet(),
                hasItems("rundeck_url", "job_id", "api_token", "args",
                        "filter_include_node_name", "filter_include_hostname", "filter_include_tags",
                        "filter_exclude_node_name", "filter_exclude_hostname", "filter_exclude_tags",
                        "exclude_precedence"));
    }

    @Test
    public void testGetName() {
        assertThat(alarmCallback.getName(), equalTo("Rundeck alarm callback"));
    }

    private Configuration validConfigurationWithout(final String key) {
        return new Configuration(Maps.filterEntries(VALID_CONFIG_SOURCE, new Predicate<Map.Entry<String, Object>>() {
            @Override
            public boolean apply(Map.Entry<String, Object> input) {
                return key.equals(input.getKey());
            }
        }));
    }
}