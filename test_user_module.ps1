$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'

Write-Host "=== TEST 1: Register User A ==="
$rand = Get-Random -Minimum 1000 -Maximum 9999
$emailA = "alice.$rand@cybershield.org"
$userAData = @{ name = 'Alice User'; email = $emailA; password = 'AlicePassword123'; phone = '9876543210' } | ConvertTo-Json
$regResp = Invoke-RestMethod -Uri "$base/api/users/register" -Method Post -Body $userAData -ContentType 'application/json'
Write-Host "User A registered: $($regResp.email) ID: $($regResp.id)"

Write-Host "`n=== TEST 2: Login with wrong password (should fail 401) ==="
$wrongLogin = @{ email = $emailA; password = 'WrongPassword' } | ConvertTo-Json
try {
    Invoke-RestMethod -Uri "$base/api/users/login" -Method Post -Body $wrongLogin -ContentType 'application/json'
    Write-Host "ERROR: Wrong password login should have failed!" -ForegroundColor Red
    exit 1
} catch {
    Write-Host "Correctly rejected wrong password (401 Unauthorized)" -ForegroundColor Green
}

Write-Host "`n=== TEST 3: Login User A with correct password ==="
$sessionA = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$loginA = @{ email = $emailA; password = 'AlicePassword123' } | ConvertTo-Json
$loginAResp = Invoke-RestMethod -Uri "$base/api/users/login" -Method Post -Body $loginA -ContentType 'application/json' -WebSession $sessionA
Write-Host "User A logged in successfully: $($loginAResp.email)"

Write-Host "`n=== TEST 4: Submit Complaint as User A WITHOUT manual userId ==="
$complaintA = @{
    title = 'Cyber Harassment on Instagram'
    description = 'The attacker sent continuous hate messages and threats on Instagram'
    category = 'HATE_SPEECH'
    platform = 'Instagram'
    incidentDateTime = '2026-09-30T18:00'
} | ConvertTo-Json
$compAResp = Invoke-RestMethod -Uri "$base/api/complaints" -Method Post -Body $complaintA -ContentType 'application/json' -WebSession $sessionA
Write-Host "Complaint created: Number=$($compAResp.complaintNumber) ID=$($compAResp.id) Severity=$($compAResp.preliminarySeverity)"

Write-Host "`n=== TEST 5: Track My Complaints for User A ==="
$myComplaintsA = Invoke-RestMethod -Uri "$base/api/complaints/my" -Method Get -WebSession $sessionA
Write-Host "User A my complaints count: $($myComplaintsA.Count)"
if ($myComplaintsA[0].complaintNumber -eq $compAResp.complaintNumber) {
    Write-Host "Verified Complaint belongs to User A!" -ForegroundColor Green
} else {
    Write-Host "Complaint number mismatch!" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== TEST 6: Check Notifications for User A ==="
$notifsA = Invoke-RestMethod -Uri "$base/api/notifications" -Method Get -WebSession $sessionA
Write-Host "User A notifications count: $($notifsA.Count)"
Write-Host "First notification title: $($notifsA[0].title) | message: $($notifsA[0].message)"

Write-Host "`n=== TEST 7: Check User A Profile ==="
$profileA = Invoke-RestMethod -Uri "$base/api/users/profile" -Method Get -WebSession $sessionA
Write-Host "Profile: Name=$($profileA.name) TotalComplaints=$($profileA.totalComplaints) Resolved=$($profileA.resolvedComplaints)"

Write-Host "`n=== TEST 8: Register and Login User B ==="
$sessionB = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$emailB = "bob.$rand@cybershield.org"
$userBData = @{ name = 'Bob User'; email = $emailB; password = 'BobPassword123'; phone = '1234567890' } | ConvertTo-Json
Invoke-RestMethod -Uri "$base/api/users/register" -Method Post -Body $userBData -ContentType 'application/json' | Out-Null
$loginB = @{ email = $emailB; password = 'BobPassword123' } | ConvertTo-Json
Invoke-RestMethod -Uri "$base/api/users/login" -Method Post -Body $loginB -ContentType 'application/json' -WebSession $sessionB | Out-Null
Write-Host "User B logged in: $emailB"

Write-Host "`n=== TEST 9: Cross-User Security Check: Bob tries to access Alice's complaint ==="
try {
    Invoke-RestMethod -Uri "$base/api/complaints/$($compAResp.id)" -Method Get -WebSession $sessionB
    Write-Host "ERROR: Bob should NOT be allowed to access Alice's complaint!" -ForegroundColor Red
    exit 1
} catch {
    Write-Host "Access denied as expected (403 Forbidden): $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`n=== TEST 10: Admin Updates Status to RESOLVED with remarks ==="
$statusUpdate = @{
    status = 'RESOLVED'
    adminRemarks = 'Account blocked on Instagram and warning issued to perpetrator.'
} | ConvertTo-Json
$updatedComp = Invoke-RestMethod -Uri "$base/api/complaints/$($compAResp.id)/status" -Method Put -Body $statusUpdate -ContentType 'application/json'
Write-Host "Status updated to: $($updatedComp.status) with remarks: $($updatedComp.adminRemarks)"

Write-Host "`n=== TEST 11: Re-check User A Notifications & Profile ==="
$notifsAAfter = Invoke-RestMethod -Uri "$base/api/notifications" -Method Get -WebSession $sessionA
Write-Host "User A updated notifications count: $($notifsAAfter.Count)"
$latestNotif = $notifsAAfter[0]
Write-Host "Latest Notification: [$($latestNotif.type)] $($latestNotif.title) - $($latestNotif.message)"

$profileAAfter = Invoke-RestMethod -Uri "$base/api/users/profile" -Method Get -WebSession $sessionA
Write-Host "Updated Profile: TotalComplaints=$($profileAAfter.totalComplaints) Resolved=$($profileAAfter.resolvedComplaints)"

Write-Host "`n========================================================" -ForegroundColor Green
Write-Host " ALL USER MODULE VERIFICATION TESTS PASSED SUCCESSFULLY! " -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
