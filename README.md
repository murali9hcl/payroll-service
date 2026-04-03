# Payroll Service

Spring Boot payroll workflow service for:
- timesheet submission
- manager approval / rejection
- deterministic payroll calculation
- anomaly scan placeholder output
- compliance lock
- payslip + ACH placeholder generation
- employee payroll Q&A placeholder

## Main endpoints

- `POST /payroll/calculate`
- `POST /payroll/timesheets`
- `GET /payroll/timesheets`
- `GET /payroll/timesheets/{id}`
- `POST /payroll/timesheets/{id}/approve`
- `POST /payroll/timesheets/{id}/reject`
- `POST /payroll/runs`
- `GET /payroll/runs/{id}`
- `POST /payroll/runs/{id}/scan-anomalies`
- `POST /payroll/runs/{id}/lock`
- `POST /payroll/runs/{id}/generate-payslips`
- `GET /payroll/runs/{id}/payslips`
- `POST /payroll/questions`
- `GET /payroll/questions/{employeeId}`

## Notes

- Production configuration currently points at MySQL via `src/main/resources/application.properties`.
- Tests use H2 in-memory database via `src/test/resources/application.properties`.
- Payslip storage and employee Q&A are placeholders that return deterministic demo values matching the workflow table.

## Try it

```powershell
.\mvnw.cmd spring-boot:run
```

Then call the workflow in order:
1. submit a timesheet
2. approve it
3. create payroll run
4. scan anomalies
5. lock payroll run
6. generate payslips
7. ask an employee question

## Sample payloads

Use the returned `id` values from earlier responses in the later steps below.

### 1. Preview payroll calculation

Endpoint: `POST /payroll/calculate`

```json
{
  "hoursWorked": 40.00,
  "overtimeHours": 8.00,
  "hourlyRate": 25.00
}
```

### 2. Submit timesheet

Endpoint: `POST /payroll/timesheets`

```json
{
  "employeeId": "EMP-100",
  "payrollPeriod": "2026-W14",
  "weekStart": "2026-03-30",
  "weekEnd": "2026-04-03",
  "hoursWorked": 40.00,
  "overtimeHours": 8.00,
  "hourlyRate": 25.00
}
```

### 3. List all submitted timesheets

Endpoint: `GET /payroll/timesheets`

No request body required.

### 4. Get one timesheet

Endpoint: `GET /payroll/timesheets/{id}`

No request body required.

### 5. Approve timesheet

Endpoint: `POST /payroll/timesheets/{id}/approve`

```json
{
  "managerId": "mgr-01",
  "notes": "Approved for payroll processing"
}
```

### 6. Reject timesheet

Endpoint: `POST /payroll/timesheets/{id}/reject`

```json
{
  "managerId": "mgr-01",
  "notes": "Rejected due to missing project allocation details"
}
```

### 7. Create payroll run

Endpoint: `POST /payroll/runs`

```json
{
  "payrollPeriod": "2026-W14"
}
```

### 8. Get payroll run details

Endpoint: `GET /payroll/runs/{id}`

No request body required.

### 9. Scan anomalies

Endpoint: `POST /payroll/runs/{id}/scan-anomalies`

No request body required.

### 10. Compliance lock payroll run

Endpoint: `POST /payroll/runs/{id}/lock`

```json
{
  "reviewedBy": "hr-01",
  "reason": "Reviewed anomaly flags and approved payroll for release"
}
```

### 11. Generate payslips and bank file placeholders

Endpoint: `POST /payroll/runs/{id}/generate-payslips`

No request body required.

### 12. List generated payslips

Endpoint: `GET /payroll/runs/{id}/payslips`

No request body required.

### 13. Ask employee payroll question

Endpoint: `POST /payroll/questions`

```json
{
  "employeeId": "EMP-100",
  "payrollRunId": 1,
  "question": "Why is my pay different?"
}
```

### 14. List questions for an employee

Endpoint: `GET /payroll/questions/{employeeId}`

No request body required.

