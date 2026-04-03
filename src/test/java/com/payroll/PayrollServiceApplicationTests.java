package com.payroll;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
class PayrollServiceApplicationTests {
	private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

	@Autowired
 	private WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
	}

	@Test
	void payrollWorkflowRunsEndToEnd() throws Exception {
		seedStateRule("FL", "2026-01-01", null, "14.00");

		String timesheetPayload = """
				{
				  "employeeId": "EMP-100",
				  "stateCode": "FL",
				  "payrollPeriod": "2026-W14",
				  "weekStart": "2026-03-30",
				  "weekEnd": "2026-04-03",
				  "hoursWorked": 40.00,
				  "overtimeHours": 8.00,
				  "hourlyRate": 25.00
				}
				""";

		MvcResult submitResult = mockMvc.perform(post("/payroll/timesheets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(timesheetPayload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUBMITTED"))
				.andExpect(jsonPath("$.aiRiskWarning").value(true))
				.andReturn();

		Long timesheetId = readId(submitResult);

		mockMvc.perform(post("/payroll/timesheets/{id}/approve", timesheetId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "managerId": "mgr-01",
								  "notes": "Approved for payroll"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED"))
				.andExpect(jsonPath("$.overtimeFlag").value(true));

		MvcResult runResult = mockMvc.perform(post("/payroll/runs")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "payrollPeriod": "2026-W14"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CALCULATED"))
				.andReturn();

		Long payrollRunId = readId(runResult);

		mockMvc.perform(post("/payroll/runs/{id}/scan-anomalies", payrollRunId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].severity").value("FLAG"));

		mockMvc.perform(post("/payroll/runs/{id}/lock", payrollRunId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "reviewedBy": "hr-01",
								  "reason": "Reviewed flags and approved payroll"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("LOCKED"));

		mockMvc.perform(post("/payroll/runs/{id}/generate-payslips", payrollRunId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].employeeId").value("EMP-100"))
				.andExpect(jsonPath("$[0].netPay").value(1027.65));

		mockMvc.perform(post("/payroll/questions")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "employeeId": "EMP-100",
								  "payrollRunId": %d,
								  "question": "Why is my pay different?"
								}
								""".formatted(payrollRunId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("Your net pay is")));
	}

	@Test
	void calculateEndpointReturnsStructuredMinimumWageResult() throws Exception {
		mockMvc.perform(post("/payroll/calculate")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "hoursWorked": 40.00,
								  "overtimeHours": 0.00,
								  "hourlyRate": 12.00
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.minimumWageValid").value(false))
				.andExpect(jsonPath("$.grossPay").value(480.0));
	}

	private Long readId(MvcResult result) throws Exception {
		Matcher matcher = ID_PATTERN.matcher(result.getResponse().getContentAsString());
		if (!matcher.find()) {
			throw new IllegalStateException("Could not find id in response: " + result.getResponse().getContentAsString());
		}
		return Long.parseLong(matcher.group(1));
	}

	private void seedStateRule(String stateCode, String effectiveStart, String effectiveEnd, String minimumWage) throws Exception {
		String payload = """
				{
				  "stateCode": "%s",
				  "effectiveStart": "%s",
				  "effectiveEnd": %s,
				  "overtimeRateMultiplier": 1.5,
				  "ficaRate": 0.0765,
				  "futaRate": 0.006,
				  "sutaRate": 0.027,
				  "federalTaxRate": 0.10,
				  "minimumWage": %s,
				  "standardWeekHours": 40.00,
				  "excessiveHoursFlag": 45.00,
				  "excessiveHoursBlock": 60.00,
				  "overtimeWarningHours": 15.00,
				  "aiWarningThreshold": 32.00,
				  "overtimeThreshold": 40.00
				}
				""".formatted(
						stateCode,
						effectiveStart,
						effectiveEnd == null ? "null" : "\"" + effectiveEnd + "\"",
						minimumWage);

		mockMvc.perform(post("/payroll/admin/state-rules")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk());
	}

}
