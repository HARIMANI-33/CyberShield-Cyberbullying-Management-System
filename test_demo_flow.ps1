# Complete 23-point Demo Verification Script for CyberShield

Write-Host '========================================================'
Write-Host ' CYBERSHIELD - COMPREHENSIVE END-TO-END DEMO TEST SUITE'
Write-Host '========================================================'

# Health Check
Write-Host '`n--- HEALTH ENDPOINT CHECK ---'
$healthRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/health' -Method GET
if ($healthRes.message -eq 'CyberShield backend is running') {
    Write-Host "  Health Check passed: $($healthRes.message)"
} else {
    throw 'Health check failed!'
}

Write-Host '`n--- USER SIDE WORKFLOW ---'

# 1. Open the application
Write-Host 'Point 1: Open application home page'
$homeRes = Invoke-WebRequest -Uri 'http://localhost:8080/' -UseBasicParsing
if ($homeRes.StatusCode -eq 200 -and $homeRes.Content -match 'CyberShield') {
    Write-Host '  Home page loaded with CyberShield branding (HTTP 200)'
} else {
    throw 'Home page failed to load'
}

# 2. Register a new user
Write-Host 'Point 2: Register a new user'
$randomNum = Get-Random
$userEmail = "demo.student.$randomNum@college.edu"
$userPayload = @{
    name = 'Aravind Kumar'
    email = $userEmail
    phone = '9840123456'
    password = 'demoPassword2026'
} | ConvertTo-Json
$userRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/users/register' -Method POST -Body $userPayload -ContentType 'application/json'
$userId = $userRes.id
if ($userId -and $userRes.name -eq 'Aravind Kumar' -and $userRes.role -eq 'USER') {
    Write-Host "  User registered successfully: ID=$userId, Name=$($userRes.name), Role=$($userRes.role)"
} else {
    throw 'User registration failed!'
}

# 3 and 4. Submit complaint with description
Write-Host 'Points 3 and 4: Submit complaint with description'
$complaintPayload = @{
    userId = $userId
    title = 'Defamation and Death Threats on Social Media'
    category = 'Threatening Messages'
    platform = 'Instagram'
    description = 'The attacker posted abusive harassment saying I will kill you and attack you tomorrow. Constant threat.'
} | ConvertTo-Json
$complaintRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/complaints' -Method POST -Body $complaintPayload -ContentType 'application/json'
$complaintId = $complaintRes.id

# 5. Upload screenshot evidence
Write-Host 'Point 5: Upload screenshot evidence'
$sampleImg = 'evidence_demo_screenshot.png'
[System.IO.File]::WriteAllBytes($sampleImg, [byte[]]@(137, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 31, 21, 196, 137, 0, 0, 0, 10, 73, 68, 65, 84, 120, 156, 99, 0, 1, 0, 0, 5, 0, 1, 13, 10, 45, 180, 0, 0, 0, 0, 73, 69, 78, 68, 174, 66, 96, 130))

$uploadResJson = curl.exe -s -F "file=@$sampleImg;type=image/png" "http://localhost:8080/api/complaints/$complaintId/evidence"
$uploadRes = $uploadResJson | ConvertFrom-Json
$evidenceId = $uploadRes.id

# 6. Submit confirmation
Write-Host 'Point 6: Submit confirmed'
Write-Host "  Complaint submission and evidence upload completed for ID: $complaintId"

# 7. Confirm complaint number generated
Write-Host 'Point 7: Confirm complaint number generated'
if ($complaintRes.complaintNumber -match '^CS-\d+') {
    Write-Host "  Complaint Number generated: $($complaintRes.complaintNumber)"
} else {
    throw "Invalid complaint number: $($complaintRes.complaintNumber)"
}

# 8. Confirm preliminary severity appears
Write-Host 'Point 8: Confirm preliminary severity appears'
if ($complaintRes.detectedSeverity -in @('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')) {
    Write-Host "  Preliminary Severity: $($complaintRes.detectedSeverity)"
} else {
    throw "Invalid severity: $($complaintRes.detectedSeverity)"
}

# 9. Confirm risk score appears
Write-Host 'Point 9: Confirm risk score appears'
if ($complaintRes.riskScore -ge 0) {
    Write-Host "  Risk Score: $($complaintRes.riskScore)"
} else {
    throw "Invalid risk score: $($complaintRes.riskScore)"
}

# 10. Confirm priority appears
Write-Host 'Point 10: Confirm priority appears'
if ($complaintRes.priority -in @('IMMEDIATE_ATTENTION', 'NORMAL_REVIEW')) {
    Write-Host "  Priority: $($complaintRes.priority)"
} else {
    throw "Invalid priority: $($complaintRes.priority)"
}

# 11. Confirm evidence is saved
Write-Host 'Point 11: Confirm evidence is saved'
if ($uploadRes.fileName -eq $sampleImg -and $uploadRes.verificationStatus -eq 'PENDING') {
    Write-Host "  Evidence saved: ID=$evidenceId, File=$($uploadRes.fileName), Status=$($uploadRes.verificationStatus)"
} else {
    throw 'Evidence upload verification failed!'
}

Write-Host '`n--- OFFICER SIDE WORKFLOW ---'

# 12. Open officer dashboard
Write-Host 'Point 12: Open officer dashboard'
$officerPage = Invoke-WebRequest -Uri 'http://localhost:8080/officer' -UseBasicParsing
if ($officerPage.StatusCode -eq 200) {
    Write-Host '  Officer dashboard loaded (HTTP 200)'
} else {
    throw 'Failed to open officer dashboard'
}

# 13. Confirm the submitted complaint appears
Write-Host 'Point 13: Confirm submitted complaint appears in feed'
$feed = Invoke-RestMethod -Uri 'http://localhost:8080/api/complaints' -Method GET
$foundComplaint = $feed | Where-Object { $_.id -eq $complaintId }
if ($foundComplaint -and $foundComplaint.complaintNumber -eq $complaintRes.complaintNumber) {
    Write-Host "  Found in feed: $($foundComplaint.complaintNumber) by $($foundComplaint.userName)"
} else {
    throw 'Complaint not found in officer feed!'
}

# 14. Open complaint details
Write-Host 'Point 14: Open complaint details'
$details = Invoke-RestMethod -Uri "http://localhost:8080/api/complaints/$complaintId" -Method GET
if ($details.id -eq $complaintId) {
    Write-Host "  Complaint details retrieved for ID: $complaintId"
} else {
    throw 'Failed to retrieve complaint details'
}

# 15. Confirm user information appears
Write-Host 'Point 15: Confirm user information appears'
if ($details.userName -eq 'Aravind Kumar' -and $details.userEmail -eq $userEmail) {
    Write-Host "  User info verified: $($details.userName) ($($details.userEmail))"
} else {
    throw 'User information mismatch!'
}

# 16. Confirm complaint description appears
Write-Host 'Point 16: Confirm complaint description appears'
if ($details.description -match 'kill you and attack you') {
    Write-Host "  Complaint description verified: $($details.description.Substring(0, 45))..."
} else {
    throw 'Complaint description missing or altered!'
}

# 17. Confirm severity appears
Write-Host 'Point 17: Confirm severity appears'
if ($details.threatLevel -eq $complaintRes.detectedSeverity) {
    Write-Host "  Severity verified: $($details.threatLevel)"
} else {
    throw 'Severity mismatch in details!'
}

# 18. Confirm risk score appears
Write-Host 'Point 18: Confirm risk score appears'
if ($details.preliminaryAnalysis.riskScore -eq $complaintRes.riskScore) {
    Write-Host "  Risk score verified: $($details.preliminaryAnalysis.riskScore)"
} else {
    throw 'Risk score mismatch!'
}

# 19. Confirm priority appears
Write-Host 'Point 19: Confirm priority appears'
if ($details.priority -eq $complaintRes.priority) {
    Write-Host "  Priority verified: $($details.priority)"
} else {
    throw 'Priority mismatch!'
}

# 20. Confirm screenshot/evidence appears
Write-Host 'Point 20: Confirm screenshot/evidence appears'
if ($details.evidence.Count -ge 1 -and $details.evidence[0].fileName -eq $sampleImg) {
    Write-Host "  Evidence verified: $($details.evidence[0].fileName), Path=$($details.evidence[0].filePath)"
} else {
    throw 'Evidence attachment missing!'
}

# 21. Update complaint status
Write-Host 'Point 21: Update complaint status'
$statusPayload = @{ status = 'INVESTIGATION' } | ConvertTo-Json
$statusRes = Invoke-RestMethod -Uri "http://localhost:8080/api/complaints/$complaintId/status" -Method PUT -Body $statusPayload -ContentType 'application/json'
if ($statusRes.status -eq 'INVESTIGATION') {
    Write-Host "  Complaint status updated to: $($statusRes.status)"
} else {
    throw 'Complaint status update failed!'
}

# 22. Verify or reject evidence
Write-Host 'Point 22: Verify evidence by officer'
$verifyPayload = @{ verificationStatus = 'VERIFIED' } | ConvertTo-Json
$verifyRes = Invoke-RestMethod -Uri "http://localhost:8080/api/evidence/$evidenceId/verify" -Method PUT -Body $verifyPayload -ContentType 'application/json'
if ($verifyRes.verificationStatus -eq 'VERIFIED') {
    Write-Host "  Evidence status updated to: $($verifyRes.verificationStatus)"
} else {
    throw 'Evidence verification update failed!'
}

# 23. Confirm changes are saved
Write-Host 'Point 23: Confirm changes are saved in database'
$finalDetails = Invoke-RestMethod -Uri "http://localhost:8080/api/complaints/$complaintId" -Method GET
if ($finalDetails.status -eq 'INVESTIGATION' -and $finalDetails.evidence[0].verificationStatus -eq 'VERIFIED') {
    Write-Host "  Verified persistent state: Case Status=$($finalDetails.status), Evidence Status=$($finalDetails.evidence[0].verificationStatus)"
} else {
    throw 'Final persistent verification failed!'
}

Write-Host '`n========================================================'
Write-Host ' ALL 23 VERIFICATION POINTS COMPLETED SUCCESSFULLY! '
Write-Host '========================================================'
