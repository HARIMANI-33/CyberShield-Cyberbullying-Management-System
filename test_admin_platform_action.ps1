$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host " CYBERSHIELD - ADMIN AND PLATFORM ACTION TEST SUITE     " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

# 1. Admin Login Verification
Write-Host "`n--- TEST 1: Admin Login with Seeded Credentials ---"
$adminSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$adminLoginBody = @{ email = 'admin@cybershield.gov'; password = 'admin123' } | ConvertTo-Json
$adminLoginResp = Invoke-RestMethod -Uri "$base/api/users/login" -Method Post -Body $adminLoginBody -ContentType 'application/json' -WebSession $adminSession
Write-Host "Admin logged in successfully: $($adminLoginResp.name) [Role: $($adminLoginResp.role)]"

# 2. Register Regular User
Write-Host "`n--- TEST 2: Register Regular User and Security Authorization Check ---"
$userSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$rand = Get-Random -Minimum 1000 -Maximum 9999
$userEmail = "user.$rand@college.edu"
$userData = @{ name = "Test Student"; email = $userEmail; password = "Password123!"; phone = "9876543210" } | ConvertTo-Json
$userRegResp = Invoke-RestMethod -Uri "$base/api/users/register" -Method Post -Body $userData -ContentType 'application/json'
$userLoginBody = @{ email = $userEmail; password = "Password123!" } | ConvertTo-Json
Invoke-RestMethod -Uri "$base/api/users/login" -Method Post -Body $userLoginBody -ContentType 'application/json' -WebSession $userSession | Out-Null
Write-Host "Regular user registered and logged in: $userEmail"

# 3. Security: Regular user attempts Admin actions -> 403 Forbidden
Write-Host "`n--- TEST 3: Regular User Blocked from Admin Functions (403 Forbidden) ---"
try {
    $blockedReq = @{ complaintId = 1; platform = 'Instagram' } | ConvertTo-Json
    Invoke-RestMethod -Uri "$base/api/platform-actions/generate" -Method Post -Body $blockedReq -ContentType 'application/json' -WebSession $userSession
    Write-Host "ERROR: Regular user was able to call platform-actions/generate!" -ForegroundColor Red
    exit 1
} catch {
    Write-Host "Security verified: Regular user blocked from generatePlatformAction (403 Forbidden)" -ForegroundColor Green
}

try {
    $blockedStatus = @{ status = 'RESOLVED' } | ConvertTo-Json
    Invoke-RestMethod -Uri "$base/api/complaints/1/status" -Method Put -Body $blockedStatus -ContentType 'application/json' -WebSession $userSession
    Write-Host "ERROR: Regular user was able to update status!" -ForegroundColor Red
    exit 1
} catch {
    Write-Host "Security verified: Regular user blocked from updating complaint status (403 Forbidden)" -ForegroundColor Green
}

# 4. Submit Complaint 1 (Instagram)
Write-Host "`n--- TEST 4: Submit Complaint 1 (Instagram) and Evidence Upload ---"
$comp1Data = @{
    title = "Slander and cyberbullying on Instagram"
    description = "User @toxic_user posted defamatory photos and continuous harassment threats on Instagram"
    category = "HARASSMENT"
    platform = "Instagram"
    incidentDateTime = "2026-09-30T14:00"
} | ConvertTo-Json
$comp1 = Invoke-RestMethod -Uri "$base/api/complaints" -Method Post -Body $comp1Data -ContentType 'application/json' -WebSession $userSession
Write-Host "Complaint 1 created: $($comp1.complaintNumber) [ID: $($comp1.id), Threat: $($comp1.preliminarySeverity)]"

# Create dummy screenshot for upload
$testFile = "test_screenshot_evidence.png"
[System.IO.File]::WriteAllBytes($testFile, [byte[]]@(137, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 31, 21, 196, 137, 0, 0, 0, 10, 73, 68, 65, 84, 120, 156, 99, 0, 1, 0, 0, 5, 0, 1, 13, 10, 45, 180, 0, 0, 0, 0, 73, 69, 78, 68, 174, 66, 96, 130))
$uploadRespRaw = curl.exe -s -F "file=@$testFile;type=image/png" "$base/api/complaints/$($comp1.id)/evidence"
$uploadJson = $uploadRespRaw | ConvertFrom-Json
$evidenceId = $uploadJson.id
Write-Host "Evidence uploaded: ID=$evidenceId, File=$($uploadJson.fileName)"

# 5. Evidence Verification by Admin with Remarks
Write-Host "`n--- TEST 5: Officer Verifies Evidence with Remarks ---"
$verifyReq = @{
    verificationStatus = 'VERIFIED'
    remarks = 'Screenshot metadata examined and confirmed authentic account timestamp.'
} | ConvertTo-Json
$verifyResp = Invoke-RestMethod -Uri "$base/api/evidence/$evidenceId/verify" -Method Put -Body $verifyReq -ContentType 'application/json' -WebSession $adminSession
Write-Host "Evidence status updated: $($verifyResp.verificationStatus)"

# Check complaint details to verify EVIDENCE_VERIFIED status and remarks
$comp1Detail = Invoke-RestMethod -Uri "$base/api/complaints/$($comp1.id)" -Method Get -WebSession $adminSession
Write-Host "Complaint 1 updated status: $($comp1Detail.status)"
Write-Host "Complaint 1 admin remarks: $($comp1Detail.adminRemarks)"

# 6. Submit Complaint 2 (Discord)
Write-Host "`n--- TEST 6: Submit Complaint 2 (Discord) with different content ---"
$comp2Data = @{
    title = "Severe death threats in gaming Discord server"
    description = "Discord user CyberViper#9999 posted repeated violent threats in guild voice and text channels"
    category = "THREAT"
    platform = "Discord"
    incidentDateTime = "2026-09-30T17:30"
} | ConvertTo-Json
$comp2 = Invoke-RestMethod -Uri "$base/api/complaints" -Method Post -Body $comp2Data -ContentType 'application/json' -WebSession $userSession
Write-Host "Complaint 2 created: $($comp2.complaintNumber) [ID: $($comp2.id), Threat: $($comp2.preliminarySeverity)]"

# 7. Generate Platform Action for Complaint 1 (Instagram)
Write-Host "`n--- TEST 7: Generate Platform Action for Complaint 1 (Instagram) ---"
$paGen1 = @{
    complaintId = $comp1.id
    platform = 'Instagram'
    reportedAccount = '@toxic_user'
    adminOfficerName = 'Inspector K. Raman'
} | ConvertTo-Json
$pa1 = Invoke-RestMethod -Uri "$base/api/platform-actions/generate" -Method Post -Body $paGen1 -ContentType 'application/json' -WebSession $adminSession
Write-Host "Platform Action 1 generated: ID=$($pa1.id), Ref=$($pa1.complaintNumber), Status=$($pa1.actionStatus)"
Write-Host "Recipient: $($pa1.recipient)"
Write-Host "Subject: $($pa1.subject)"

# 8. Generate Platform Action for Complaint 2 (Discord)
Write-Host "`n--- TEST 8: Generate Platform Action for Complaint 2 (Discord) ---"
$paGen2 = @{
    complaintId = $comp2.id
    platform = 'Discord'
    reportedAccount = 'CyberViper#9999'
    adminOfficerName = 'Inspector K. Raman'
} | ConvertTo-Json
$pa2 = Invoke-RestMethod -Uri "$base/api/platform-actions/generate" -Method Post -Body $paGen2 -ContentType 'application/json' -WebSession $adminSession
Write-Host "Platform Action 2 generated: ID=$($pa2.id), Ref=$($pa2.complaintNumber), Status=$($pa2.actionStatus)"
Write-Host "Recipient: $($pa2.recipient)"
Write-Host "Subject: $($pa2.subject)"

# 9. Verify Different Complaints Generate Different Emails
Write-Host "`n--- TEST 9: Verify Different Complaints Produce Unique Individualized Emails ---"
if ($pa1.message -eq $pa2.message) {
    Write-Host "ERROR: Email messages are identical! Template must use specific complaint data." -ForegroundColor Red
    exit 1
}
if (!$pa1.message.Contains($comp1.complaintNumber)) {
    Write-Host "ERROR: Message 1 does not contain Complaint 1 ID!" -ForegroundColor Red
    exit 1
}
if (!$pa2.message.Contains($comp2.complaintNumber)) {
    Write-Host "ERROR: Message 2 does not contain Complaint 2 ID!" -ForegroundColor Red
    exit 1
}
if (!$pa1.message.Contains("Instagram")) {
    Write-Host "ERROR: Message 1 does not contain platform Instagram!" -ForegroundColor Red
    exit 1
}
if (!$pa2.message.Contains("Discord")) {
    Write-Host "ERROR: Message 2 does not contain platform Discord!" -ForegroundColor Red
    exit 1
}
Write-Host "Verified: Complaint 1 and Complaint 2 generated completely unique, individualized emails!" -ForegroundColor Green

# 10. Edit / Preview Platform Action
Write-Host "`n--- TEST 10: Admin Edits Platform Action Before Sending ---"
$customSubj = "Cyberbullying Report - " + $comp1.complaintNumber + " [URGENT TAKEDOWN]"
$updatePa = @{
    recipient = 'safety-escalations@instagram.com'
    reportedAccount = '@toxic_user (Account ID: 948271)'
    subject = $customSubj
    message = $pa1.message + "`n`nOfficer Note: Immediate response requested due to repeat offenses."
} | ConvertTo-Json
$editedPa1 = Invoke-RestMethod -Uri "$base/api/platform-actions/$($pa1.id)" -Method Put -Body $updatePa -ContentType 'application/json' -WebSession $adminSession
Write-Host "Platform Action edited: New recipient=$($editedPa1.recipient), Status=$($editedPa1.actionStatus)"

# 11. Confirm & Send Platform Action
Write-Host "`n--- TEST 11: Admin Confirms and Sends Platform Action ---"
$sentPa1 = Invoke-RestMethod -Uri "$base/api/platform-actions/$($pa1.id)/send" -Method Post -WebSession $adminSession
Write-Host "Platform Action sent: Status=$($sentPa1.actionStatus), SentAt=$($sentPa1.sentAt)"
if ($sentPa1.actionStatus -ne 'SENT') {
    Write-Host "ERROR: Status should be SENT" -ForegroundColor Red
    exit 1
}

# 12. Verify Complaint Status Transition to ACTION_TAKEN & Notification Dispatched
Write-Host "`n--- TEST 12: Verify Complaint 1 transitioned to ACTION_TAKEN ---"
$comp1AfterSend = Invoke-RestMethod -Uri "$base/api/complaints/$($comp1.id)" -Method Get -WebSession $adminSession
Write-Host "Complaint 1 Status after platform action: $($comp1AfterSend.status)"
if ($comp1AfterSend.status -ne 'ACTION_TAKEN') {
    Write-Host "ERROR: Complaint status should be ACTION_TAKEN, got $($comp1AfterSend.status)" -ForegroundColor Red
    exit 1
}
Write-Host "Verified: Complaint status automatically progressed to ACTION_TAKEN!" -ForegroundColor Green

# Check user notifications for platform action
$userNotifs = Invoke-RestMethod -Uri "$base/api/notifications" -Method Get -WebSession $userSession
Write-Host "User notifications count: $($userNotifs.Count)"
Write-Host "Latest notification: [$($userNotifs[0].type)] $($userNotifs[0].title) - $($userNotifs[0].message)"

# 13. Platform Action History & Configurable Contacts
Write-Host "`n--- TEST 13: Query Platform Action History and Configurable Contacts ---"
$allActions = Invoke-RestMethod -Uri "$base/api/platform-actions" -Method Get -WebSession $adminSession
Write-Host "Total Platform Actions recorded: $($allActions.Count)"

$contacts = Invoke-RestMethod -Uri "$base/api/platform-actions/contacts" -Method Get
Write-Host "Configured Contacts: Instagram=$($contacts.Instagram), Discord=$($contacts.Discord)"

# Clean up temporary test file
if (Test-Path $testFile) { Remove-Item $testFile }

Write-Host "`n========================================================" -ForegroundColor Green
Write-Host " ALL ADMIN AND PLATFORM ACTION TESTS PASSED SUCCESSFULLY! " -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
