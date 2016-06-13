package com.swrve.sdk;

import com.swrve.sdk.config.SwrveConfig;
import com.swrve.sdk.config.SwrveConfigBase;
import com.swrve.sdk.messaging.SwrveButton;
import com.swrve.sdk.messaging.SwrveCampaign;
import com.swrve.sdk.messaging.SwrveMessageFormat;
import com.swrve.sdk.messaging.model.Arg;
import com.swrve.sdk.messaging.model.Conditions;
import com.swrve.sdk.messaging.model.Trigger;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TriggerTest extends SwrveBaseTest {

    private ISwrveCampaignManager testCampaignManger = new ISwrveCampaignManager() {
        @Override
        public Date getNow() {
            return new Date();
        }
        @Override
        public Date getInitialisedTime() {
            return new Date();
        }
        @Override
        public File getCacheDir() {
            return new File("");
        }
        @Override
        public Set<String> getAssetsOnDisk() {
            Set<String> set = new HashSet<>();
            set.add("asset1");
            return set;
        }
        @Override
        public SwrveConfigBase getConfig() {
            return new SwrveConfig();
        }
        @Override
        public String getAppStoreURLForApp(int appId) {
            return "";
        }
        @Override
        public void buttonWasPressedByUser(SwrveButton button) {
            // empty
        }
        @Override
        public void messageWasShownToUser(SwrveMessageFormat messageFormat) {
            // empty
        }
    };

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testTriggerModelV5ExceptionHandled() {
        String json = "[ \"song1.played\", \"song2.played\", \"song3.played\"]";
        List<Trigger> triggers = Trigger.fromJson(json, 1);
        assertNull(triggers); // asserting that the invalid json exception is caught
    }

    @Test
    public void testTriggerModelWithConditions() throws Exception {

        String json = SwrveTestUtils.getAssetAsText(mActivity, "triggers.json");
        List<Trigger> triggers = Trigger.fromJson(json, 1);

        assertNotNull(triggers);
        assertEquals(3, triggers.size());

        Trigger trigger1 = triggers.get(0);
        assertEquals("music.condition1", trigger1.getEventName());
        assertNotNull(trigger1.getConditions());
        Conditions conditions = trigger1.getConditions();
        assertEquals(Conditions.Op.AND, conditions.getOp());
        assertEquals(2, conditions.getArgs().size());
        assertEquals("artist", conditions.getArgs().get(0).getKey());
        assertEquals(Arg.Op.EQ, conditions.getArgs().get(0).getOp());
        assertEquals("prince", conditions.getArgs().get(0).getValue());
        assertEquals("song", conditions.getArgs().get(1).getKey());
        assertEquals(Arg.Op.EQ, conditions.getArgs().get(1).getOp());
        assertEquals("purple rain", conditions.getArgs().get(1).getValue());

        Trigger trigger2 = triggers.get(1);
        assertEquals("music.condition2", trigger2.getEventName());
        assertNotNull(trigger2.getConditions());
        assertNull(trigger2.getConditions().getArgs());
        assertEquals(Conditions.Op.EQ, trigger2.getConditions().getOp());
        assertEquals("artist", trigger2.getConditions().getKey());
        assertEquals("queen", trigger2.getConditions().getValue());

        Trigger trigger3 = triggers.get(2);
        assertEquals("music.condition3", trigger3.getEventName());
        assertNull(trigger3.getConditions().getArgs());
        assertNull(trigger3.getConditions().getOp());
    }

    @Test
    public void testTriggerModelWithInvalidConditions() {
        // if any trigger is invalid, then all triggers are null'ed.

        String unsupportedOp = "[{\n" +
                "    \"event_name\": \"invalid.trigger\",\n" +
                "    \"conditions\": {\n" +
                "        \"key\": \"artist\",\n" +
                "        \"op\": \"unsupported_op\",\n" +
                "        \"value\": \"queen\"\n" +
                "    }\n" +
                "}]";
        List<Trigger> triggers = Trigger.fromJson(unsupportedOp, 1);
        assertNull(triggers);

        String nullKey = "[{\n" +
                "    \"event_name\": \"invalid.trigger\",\n" +
                "    \"conditions\": {\n" +
                "        \"key\": null,\n" +
                "        \"op\": \"eq\",\n" +
                "        \"value\": \"queen\"\n" +
                "    }\n" +
                "}]";
        triggers = Trigger.fromJson(nullKey, 1);
        assertNull(triggers);

        String nullValue = "[{\n" +
                "    \"event_name\": \"invalid.trigger\",\n" +
                "    \"conditions\": {\n" +
                "        \"key\": \"artist\",\n" +
                "        \"op\": \"eq\",\n" +
                "        \"value\": null\n" +
                "    }\n" +
                "}]";
        triggers = Trigger.fromJson(nullValue, 1);
        assertNull(triggers);

        String missingArgs = "[{\n" +
                "    \"event_name\": \"invalid.trigger\",\n" +
                "    \"conditions\": {\n" +
                "        \"key\": \"artist\",\n" +
                "        \"op\": \"and\",\n" +
                "        \"value\": \"queen\"\n" +
                "    }\n" +
                "}]";
        triggers = Trigger.fromJson(missingArgs, 1);
        assertNull(triggers);

        String nullArg = "[{\n" +
                "    \"event_name\": \"invalid.trigger\",\n" +
                "    \"conditions\": {\n" +
                "        \"op\": \"and\",\n" +
                "        \"args\": null" +
                "    }\n" +
                "}]";
        triggers = Trigger.fromJson(nullArg, 1);
        assertNull(triggers);

        String nullArgKey = "[{\n" +
                "    \"event_name\": \"invalid.trigger\",\n" +
                "    \"conditions\": {\n" +
                "        \"op\": \"and\",\n" +
                "        \"args\": [{\n" +
                "            \"key\": null,\n" +
                "            \"op\": \"eq\",\n" +
                "            \"value\": \"prince\"\n" +
                "        }]" +
                "    }\n" +
                "}]";
        triggers = Trigger.fromJson(nullArgKey, 1);
        assertNull(triggers);

        String nullArgOp = "[{\n" +
                "    \"event_name\": \"invalid.trigger\",\n" +
                "    \"conditions\": {\n" +
                "        \"op\": \"and\",\n" +
                "        \"args\": [{\n" +
                "            \"key\": \"artist\",\n" +
                "            \"op\": null,\n" +
                "            \"value\": \"prince\"\n" +
                "        }]" +
                "    }\n" +
                "}]";
        triggers = Trigger.fromJson(nullArgOp, 1);
        assertNull(triggers);

        String nullArgValue = "[{\n" +
                "    \"event_name\": \"invalid.trigger\",\n" +
                "    \"conditions\": {\n" +
                "        \"op\": \"and\",\n" +
                "        \"args\": [{\n" +
                "            \"key\": \"artist\",\n" +
                "            \"op\": \"eq\",\n" +
                "            \"value\": null\n" +
                "        }]" +
                "    }\n" +
                "}]";
        triggers = Trigger.fromJson(nullArgValue, 1);
        assertNull(triggers);

        String unsupportedArgOp = "[{\n" +
                "    \"event_name\": \"invalid.trigger\",\n" +
                "    \"conditions\": {\n" +
                "        \"op\": \"and\",\n" +
                "        \"args\": [{\n" +
                "            \"key\": \"artist\",\n" +
                "            \"op\": \"unsupported_op\",\n" +
                "            \"value\": null\n" +
                "        }]" +
                "    }\n" +
                "}]";
        triggers = Trigger.fromJson(unsupportedArgOp, 1);
        assertNull(triggers);
    }

    @Test
    public void testCampaignTriggerCondition() throws Exception {

        String text = SwrveTestUtils.getAssetAsText(mActivity, "campaign_trigger_condition.json");
        assertNotNull(text);
        JSONObject jsonObject = new JSONObject(text);
        SwrveCampaign campaign = new SwrveCampaign(testCampaignManger, new SwrveCampaignDisplayer(null), jsonObject, new HashSet<String>());
        assertNotNull(campaign);

        Map<Integer, SwrveCampaignDisplayer.Result> campaignDisplayResults =  new HashMap<>();
        Map<String, String> payload = new HashMap<>();
        payload.put("artist", "prince");
        payload.put("song", "PuRpLe RaIn"); // mixed case on purpose
        payload.put("extra", "unused");
        assertNotNull(campaign.getMessageForEvent("music.condition1", payload, new Date(), campaignDisplayResults));
        assertEquals(1, campaignDisplayResults.size());
        assertEquals(SwrveCampaignDisplayer.DisplayResult.MATCH, campaignDisplayResults.get(campaign.getId()).resultCode);

        campaignDisplayResults =  new HashMap<>();
        payload = new HashMap<>();
        payload.put("artist", "this should not match");
        assertNull(campaign.getMessageForEvent("music.condition1", payload, new Date(), campaignDisplayResults));
        assertEquals(1, campaignDisplayResults.size());
        assertEquals(SwrveCampaignDisplayer.DisplayResult.NO_MATCH, campaignDisplayResults.get(campaign.getId()).resultCode);

        campaignDisplayResults =  new HashMap<>();
        payload = new HashMap<>();
        payload.put("artist", "queen");
        assertNotNull(campaign.getMessageForEvent("music.condition2", payload, new Date(), campaignDisplayResults));
        assertEquals(1, campaignDisplayResults.size());
        assertEquals(SwrveCampaignDisplayer.DisplayResult.MATCH, campaignDisplayResults.get(campaign.getId()).resultCode);

        campaignDisplayResults =  new HashMap<>();
        payload = new HashMap<>();
        payload.put("artist", "this should not match");
        assertNull(campaign.getMessageForEvent("music.condition2", payload, new Date(), campaignDisplayResults));
        assertEquals(1, campaignDisplayResults.size());
        assertEquals(SwrveCampaignDisplayer.DisplayResult.NO_MATCH, campaignDisplayResults.get(campaign.getId()).resultCode);

        campaignDisplayResults =  new HashMap<>();
        payload = new HashMap<>();
        payload.put("extra", "unused");
        assertNotNull(campaign.getMessageForEvent("music.condition3", payload, new Date(), campaignDisplayResults));
        assertEquals(1, campaignDisplayResults.size());
        assertEquals(SwrveCampaignDisplayer.DisplayResult.MATCH, campaignDisplayResults.get(campaign.getId()).resultCode);

        campaignDisplayResults =  new HashMap<>();
        assertNull(campaign.getMessageForEvent("random.event", null, new Date(), campaignDisplayResults));
        assertEquals(1, campaignDisplayResults.size());
        assertEquals(SwrveCampaignDisplayer.DisplayResult.NO_MATCH, campaignDisplayResults.get(campaign.getId()).resultCode);

        // match the event name but null payload
        campaignDisplayResults =  new HashMap<>();
        assertNull(campaign.getMessageForEvent("music.condition1", null, new Date(), campaignDisplayResults));
        assertEquals(1, campaignDisplayResults.size());
        assertEquals(SwrveCampaignDisplayer.DisplayResult.NO_MATCH, campaignDisplayResults.get(campaign.getId()).resultCode);

        // match the event name but null payload
        campaignDisplayResults =  new HashMap<>();
        assertNull(campaign.getMessageForEvent("music.condition2", null, new Date(), campaignDisplayResults));
        assertEquals(1, campaignDisplayResults.size());
        assertEquals(SwrveCampaignDisplayer.DisplayResult.NO_MATCH, campaignDisplayResults.get(campaign.getId()).resultCode);

        // null event name
        campaignDisplayResults =  new HashMap<>();
        assertNull(campaign.getMessageForEvent(null, null, new Date(), campaignDisplayResults));
        assertEquals(1, campaignDisplayResults.size());
        assertEquals(SwrveCampaignDisplayer.DisplayResult.NO_MATCH, campaignDisplayResults.get(campaign.getId()).resultCode);
    }
}
