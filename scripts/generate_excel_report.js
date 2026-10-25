/**
 * CivicBin Security Report Generator (Node.js)
 * Generates findings.xlsx using ExcelJS
 * Run: npm install exceljs && node generate_excel_report.js
 */

const path = require('path');
const fs = require('fs');

// Auto-install exceljs if missing
function tryRequire(pkg) {
  try { return require(pkg); }
  catch(e) {
    const { execSync } = require('child_process');
    console.log(`Installing ${pkg}...`);
    execSync(`npm install ${pkg}`, { stdio: 'inherit' });
    return require(pkg);
  }
}

const ExcelJS = tryRequire('exceljs');

const now = new Date().toISOString().replace('T', ' ').substring(0, 19) + ' UTC';

const COLORS = {
  CRITICAL: 'C0392B', HIGH: 'E67E22', MEDIUM: 'F1C40F',
  LOW: '3498DB', GOOD: '27AE60', header: '1A2332',
  subhdr: '2C3E50', alt: 'F8F9FA', white: 'FFFFFF',
};

const FINDINGS = [
  ['SEC-001','CRITICAL','SQL Injection','backend/user_login.php','POST /backend/user_login.php',14,
   'String-interpolated SQL: WHERE email=\'$email\'. Vulnerable to authentication bypass.',
   'Authentication bypass; full DB read access; login as any user without password',
   'Use PDO prepared statements: $stmt=$pdo->prepare(\'SELECT...WHERE email=?\'); $stmt->execute([$email]);'],
  ['SEC-002','CRITICAL','SQL Injection','backend/user_signup.php','POST /backend/user_signup.php',18,
   'INSERT built via string interpolation. Second-order injection risk.',
   'Data manipulation; privilege escalation; downstream query corruption',
   'Replace all string-interpolated queries with PDO prepared statements.'],
  ['SEC-003','CRITICAL','SQL Injection (UNION-based)','backend/get_organizers.php','GET /backend/get_organizers.php?city=...',7,
   'LIKE clause uses concatenated $city variable. UNION-based injection possible.',
   'Full database dump; extraction of all user/org password hashes',
   'Prepared statement: $stmt->execute([\'%\'.$city.\'%\']);'],
  ['SEC-004','CRITICAL','Unauthenticated Password Reset','backend/user_reset_password.php','POST /backend/user_reset_password.php',28,
   'Password reset accepts only email with no OTP/token. Anyone can reset any account.',
   'Complete account takeover for any user whose email is known',
   'Generate time-limited OTP via email. Verify before changing password.'],
  ['SEC-005','CRITICAL','Unauthenticated Password Reset','backend/org_reset_password.php','POST /backend/org_reset_password.php',28,
   'Same critical password reset vulnerability for organization accounts.',
   'Full takeover of civic/government organization accounts',
   'Same OTP/magic-link implementation as SEC-004.'],
  ['SEC-006','CRITICAL','No Session Management — Universal Impersonation','backend/db.php','ALL endpoints',1,
   'Backend trusts client-supplied user_id/org_id with no session/JWT validation.',
   'Full horizontal privilege escalation — any user can impersonate any other',
   'Implement PHP session_start(). Store user_id in $_SESSION. Never trust client-supplied IDs.'],
  ['SEC-007','HIGH','IDOR — Delete Any Report','backend/delete_report.php','POST /backend/delete_report.php',14,
   'No ownership check on deletion. Anyone can delete any report by guessing report_id.',
   'Mass data deletion; civic reporting integrity destroyed',
   'Add: DELETE FROM reports WHERE id=? AND user_id=? using session user_id.'],
  ['SEC-008','HIGH','IDOR — Edit Any Report','backend/update_report.php','POST /backend/update_report.php',16,
   'Report updates do not verify ownership. Any caller can edit any report.',
   'Civic data integrity violation; misinformation in public reports',
   'Add AND user_id=? to UPDATE WHERE clause using session user_id.'],
  ['SEC-009','HIGH','IDOR — Change Status of Any Report','backend/update_report_status.php','POST /backend/update_report_status.php',18,
   'Unauthenticated callers can change any report status. org_id from request body.',
   'Fraudulent report resolution; civic accountability bypass',
   'Validate org_id from server-side session only.'],
  ['SEC-010','HIGH','Wildcard CORS','backend/db.php','ALL endpoints',8,
   'Access-Control-Allow-Origin: * allows any website cross-origin requests.',
   'CSRF; data exfiltration from victim browser',
   'Restrict to known origins: header(\'Access-Control-Allow-Origin: https://yourcivicapp.com\');'],
  ['SEC-011','HIGH','Hardcoded Database Credentials','backend/db.php','N/A',20,
   'MySQL root credentials hardcoded and committed to public GitHub repo.',
   'Complete database compromise with root-level MySQL access',
   'Use getenv(\'DB_USER\') and getenv(\'DB_PASS\'). Use a non-root MySQL user.'],
  ['SEC-012','HIGH','No Rate Limiting — Brute Force','backend/user_login.php','POST /backend/user_login.php',1,
   'No rate limiting, CAPTCHA, or account lockout on any endpoint.',
   'Password brute-force; credential stuffing from data breaches',
   'Redis rate limiting: max 5/min per IP. Account lockout after 10 failures.'],
  ['SEC-013','HIGH','File Upload — RCE Risk','backend/upload_image.php','POST /backend/upload_image.php',28,
   'Extension from filename without strict whitelist. MIME check spoofable. Uploads in web root.',
   'Remote Code Execution on the web server',
   'Whitelist extensions; use hash-only filenames; .htaccess to deny PHP in uploads/.'],
  ['SEC-014','HIGH','DB Errors in API Responses','backend/submit_report.php','Multiple POST endpoints','Various',
   '$conn->error echoed directly exposing table names, column names, query structure.',
   'Database schema reconnaissance for precision SQL injection',
   'Log errors server-side. Return only generic error messages to clients.'],
  ['SEC-015','HIGH','IDOR — PII Enumeration','backend/get_user_details.php','GET /backend/get_user_details.php?user_id=X',14,
   'Returns name+email for any user_id with no authentication.',
   'Mass PII harvesting; GDPR violation; phishing list generation',
   'Require authenticated session. Return only requesting user\'s own data.'],
  ['SEC-016','HIGH','Missing Security Headers','backend/db.php','ALL endpoints',7,
   'No X-Frame-Options, X-Content-Type-Options, CSP, HSTS on any response.',
   'XSS amplification; clickjacking; protocol downgrade',
   'Add X-Frame-Options: DENY, X-Content-Type-Options: nosniff, HSTS to db.php.'],
  ['SEC-017','MEDIUM','No Password Strength Policy','backend/user_signup.php','POST /backend/user_signup.php',12,
   'No minimum password length or complexity requirements.',
   'Weak credentials; increased brute-force success rate',
   'Enforce min 8 chars, 1 uppercase, 1 number, 1 special char server-side.'],
  ['SEC-018','MEDIUM','No Email Format Validation','backend/user_signup.php','POST /backend/user_signup.php',11,
   'Email not validated with filter_var(FILTER_VALIDATE_EMAIL).',
   'Polluted database; potential injection surface',
   'Add: if (!filter_var($data[\'email\'], FILTER_VALIDATE_EMAIL)) { return error; }'],
  ['SEC-019','MEDIUM','No CSRF Protection','backend/db.php','ALL POST endpoints',1,
   'No CSRF tokens on any state-changing endpoint.',
   'Unauthorized data modification via CSRF attacks',
   'Implement synchronizer token pattern or SameSite=Strict cookie.'],
  ['SEC-020','MEDIUM','LIKE Wildcard Injection','backend/get_organizers.php','GET /backend/get_organizers.php?city=...',7,
   '% and _ wildcards not escaped in LIKE clause.',
   'Full organization list disclosure bypassing intended filtering',
   'Escape wildcards: str_replace([\'%\',\'_\'],[\'\\\\%\',\'\\\\_\'],$city)'],
  ['SEC-021','MEDIUM','Credentials Committed to Git','backend/db.php','N/A',20,
   'MySQL root credentials in db.php committed to public GitHub repository.',
   'Database takeover if server is internet-accessible',
   'Use environment variables. Rotate credentials immediately.'],
  ['SEC-022','MEDIUM','Unauthenticated Private Chat Access','backend/get_messages.php','GET /backend/get_messages.php',1,
   'Private messages accessible to any caller knowing user_id + org_id.',
   'Mass exposure of private user-organization communications',
   'Validate server-side session against requested user_id/org_id.'],
  ['SEC-023','MEDIUM','Fraudulent Report Resolution Credit','backend/update_report_status.php','POST /backend/update_report_status.php',14,
   'org_id accepted from request body. Any caller claims credit for any org.',
   'Fraudulent completion credits; civic accountability compromised',
   'Extract org_id from server-side session only.'],
  ['SEC-024','LOW','User Enumeration via Error Messages','backend/user_login.php','POST /backend/user_login.php',22,
   'Login returns distinct messages: "User not found" vs "Invalid password".',
   'User enumeration enables targeted attacks',
   'Return generic: "Invalid email or password" for all failure cases.'],
  ['SEC-025','LOW','Missing HTTPS Enforcement','backend/upload_image.php','POST /backend/upload_image.php',40,
   'File URL built dynamically — HTTP URLs possible if HTTPS not configured.',
   'Image tampering in transit; data interception',
   'Force $protocol = \'https\'; or enforce redirect in .htaccess.'],
];

const ENDPOINTS = [
  [1,'/backend/user_login.php','POST','No','Public','backend/user_login.php','HIGH'],
  [2,'/backend/user_signup.php','POST','No','Public','backend/user_signup.php','HIGH'],
  [3,'/backend/org_login.php','POST','No','Public','backend/org_login.php','HIGH'],
  [4,'/backend/org_signup.php','POST','No','Public','backend/org_signup.php','MEDIUM'],
  [5,'/backend/get_reports.php','GET','No','Org (unenforced)','backend/get_reports.php','HIGH'],
  [6,'/backend/get_user_reports.php','GET','No','User (unenforced)','backend/get_user_reports.php','HIGH'],
  [7,'/backend/get_user_details.php','GET','No','User (unenforced)','backend/get_user_details.php','HIGH'],
  [8,'/backend/get_org_details.php','GET','No','Org (unenforced)','backend/get_org_details.php','MEDIUM'],
  [9,'/backend/get_organizers.php','GET','No','Public','backend/get_organizers.php','CRITICAL'],
  [10,'/backend/get_cleanliness_score.php','GET','No','Public','backend/get_cleanliness_score.php','LOW'],
  [11,'/backend/get_messages.php','GET','No','User/Org (unenf.)','backend/get_messages.php','HIGH'],
  [12,'/backend/get_chat_list.php','GET','No','Org (unenforced)','backend/get_chat_list.php','HIGH'],
  [13,'/backend/send_message.php','POST','No','User/Org (unenf.)','backend/send_message.php','HIGH'],
  [14,'/backend/submit_report.php','POST','No','User (unenforced)','backend/submit_report.php','HIGH'],
  [15,'/backend/upload_image.php','POST','No','User (unenforced)','backend/upload_image.php','CRITICAL'],
  [16,'/backend/delete_report.php','POST','No','User (unenforced)','backend/delete_report.php','CRITICAL'],
  [17,'/backend/update_report.php','POST','No','User (unenforced)','backend/update_report.php','HIGH'],
  [18,'/backend/update_report_status.php','POST','No','Org (unenforced)','backend/update_report_status.php','CRITICAL'],
  [19,'/backend/user_reset_password.php','POST','No','Public','backend/user_reset_password.php','CRITICAL'],
  [20,'/backend/org_reset_password.php','POST','No','Public','backend/org_reset_password.php','CRITICAL'],
  [21,'/backend/options.php','GET','No','Public','backend/options.php','LOW'],
];

const DEPS = [
  ['PHP','8.x (XAMPP)','HIGH','CVE-2024-2756 / CVE-2023-3823','Cookie bypass + XXE in libxml','PHP 8.1.28+ / 8.2.18+'],
  ['MySQL','8.0.x (XAMPP)','MEDIUM','CVE-2024-21096','MySQL Cluster vulnerability','MySQL 8.0.37+'],
  ['Apache','2.4.x (XAMPP)','HIGH','CVE-2023-45802','mod_http2 memory-pressure DoS','Apache 2.4.58+'],
  ['OpenSSL','Bundled','HIGH','CVE-2023-0464','Certificate chain verification DoS','OpenSSL 3.0.9+'],
  ['k6','0.49.0','LOW','N/A','Test tool — non-production','Latest stable'],
  ['selenium-webdriver','4.x','LOW','N/A','Test tool — non-production','Latest stable'],
  ['No Composer','N/A','MEDIUM','N/A','No PHP dependency manager — cannot audit PHP libs','Add Composer'],
];

function cellFill(color) {
  return { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF' + color } };
}

function applyCell(cell, value, opts = {}) {
  cell.value = value;
  if (opts.fill) cell.fill = cellFill(opts.fill);
  if (opts.bold || opts.color || opts.size) {
    cell.font = { name: 'Calibri', bold: opts.bold || false,
                  color: { argb: 'FF' + (opts.color || '000000') },
                  size: opts.size || 10 };
  }
  cell.alignment = {
    horizontal: opts.halign || 'left',
    vertical: opts.valign || 'top',
    wrapText: opts.wrap !== false,
  };
  cell.border = {
    top: { style: 'thin', color: { argb: 'FFBDC3C7' } },
    left: { style: 'thin', color: { argb: 'FFBDC3C7' } },
    bottom: { style: 'thin', color: { argb: 'FFBDC3C7' } },
    right: { style: 'thin', color: { argb: 'FFBDC3C7' } },
  };
}

async function generate() {
  const wb = new ExcelJS.Workbook();
  wb.creator = 'CivicBin Security Pipeline';
  wb.created = new Date();

  // ─── SHEET 1: Security Findings ───────────────────────────────
  const ws1 = wb.addWorksheet('Security Findings', { views: [{ state: 'frozen', ySplit: 2 }] });

  ws1.mergeCells('A1:I1');
  applyCell(ws1.getCell('A1'), `🔐 CivicBin Security Findings — ${now}`,
    { fill: COLORS.header, bold: true, color: COLORS.white, size: 13, halign: 'center', valign: 'middle' });
  ws1.getRow(1).height = 34;

  const h1 = ['ID','Severity','Vulnerability Type','File Path','Endpoint','Line','Description','Impact','Recommended Fix'];
  h1.forEach((h, i) => {
    applyCell(ws1.getCell(2, i+1), h,
      { fill: COLORS.subhdr, bold: true, color: COLORS.white, size: 10, halign: 'center', valign: 'middle' });
  });
  ws1.getRow(2).height = 24;
  ws1.autoFilter = { from: 'A2', to: 'I2' };

  FINDINGS.forEach((f, idx) => {
    const [id, sev, vtype, fpath, ep, line, desc, impact, fix] = f;
    const r = idx + 3;
    const rowFill = idx % 2 === 0 ? COLORS.white : 'F8F9FA';
    const sevColor = COLORS[sev] || '95A5A6';

    [id, sev, vtype, fpath, ep, String(line), desc, impact, fix].forEach((val, ci) => {
      const cell = ws1.getCell(r, ci + 1);
      if (ci === 1) {
        applyCell(cell, val, { fill: sevColor, bold: true, color: COLORS.white, size: 9, halign: 'center', valign: 'middle' });
      } else {
        applyCell(cell, val, { fill: rowFill, size: 9 });
      }
    });
    ws1.getRow(r).height = 65;
  });

  [10,12,28,38,42,6,65,45,60].forEach((w, i) => {
    ws1.getColumn(i+1).width = w;
  });

  // ─── SHEET 2: Endpoint Inventory ──────────────────────────────
  const ws2 = wb.addWorksheet('Endpoint Inventory', { views: [{ state: 'frozen', ySplit: 2 }] });

  ws2.mergeCells(`A1:G1`);
  applyCell(ws2.getCell('A1'), `📡 API Endpoint Inventory — CivicBin (${ENDPOINTS.length} Endpoints)`,
    { fill: COLORS.header, bold: true, color: COLORS.white, size: 13, halign: 'center', valign: 'middle' });
  ws2.getRow(1).height = 34;

  const h2 = ['#','Endpoint','Method','Auth Required','Expected Roles','Controller File','Risk Level'];
  h2.forEach((h, i) => {
    applyCell(ws2.getCell(2, i+1), h,
      { fill: COLORS.subhdr, bold: true, color: COLORS.white, size: 10, halign: 'center', valign: 'middle' });
  });
  ws2.getRow(2).height = 24;
  ws2.autoFilter = { from: 'A2', to: 'G2' };

  const methodColors = { GET: '2980B9', POST: '8E44AD', PUT: '16A085', DELETE: 'C0392B' };

  ENDPOINTS.forEach((ep, idx) => {
    const [num, endpoint, method, auth, roles, ctrl, risk] = ep;
    const r = idx + 3;
    const rowFill = idx % 2 === 0 ? COLORS.white : 'F8F9FA';

    [num, endpoint, method, auth, roles, ctrl, risk].forEach((val, ci) => {
      const cell = ws2.getCell(r, ci+1);
      if (ci === 2) {
        applyCell(cell, val, { fill: methodColors[val] || '7F8C8D', bold: true, color: COLORS.white, size: 9, halign: 'center', valign: 'middle' });
      } else if (ci === 3) {
        applyCell(cell, val, { fill: val === 'No' ? 'C0392B' : '27AE60', bold: true, color: COLORS.white, size: 9, halign: 'center', valign: 'middle' });
      } else if (ci === 6) {
        applyCell(cell, val, { fill: COLORS[val] || '95A5A6', bold: true, color: COLORS.white, size: 9, halign: 'center', valign: 'middle' });
      } else {
        applyCell(cell, val, { fill: rowFill, size: 9, wrap: false });
      }
    });
    ws2.getRow(r).height = 22;
  });

  const warningRow = ENDPOINTS.length + 4;
  ws2.mergeCells(`A${warningRow}:G${warningRow}`);
  applyCell(ws2.getCell(`A${warningRow}`),
    '⚠️  ALL 21 endpoints have ZERO server-side authentication. Application trusts client-side localStorage values.',
    { fill: 'C0392B', bold: true, color: COLORS.white, size: 11, halign: 'center', valign: 'middle' });
  ws2.getRow(warningRow).height = 28;

  [5, 48, 10, 15, 22, 45, 13].forEach((w, i) => ws2.getColumn(i+1).width = w);

  // ─── SHEET 3: Dependency Vulnerabilities ──────────────────────
  const ws3 = wb.addWorksheet('Dependency Vulnerabilities', { views: [{ state: 'frozen', ySplit: 2 }] });

  ws3.mergeCells('A1:F1');
  applyCell(ws3.getCell('A1'), '📦 Dependency Vulnerability Report — CivicBin',
    { fill: COLORS.header, bold: true, color: COLORS.white, size: 13, halign: 'center', valign: 'middle' });
  ws3.getRow(1).height = 34;

  const h3 = ['Package','Current Version','Severity','CVE / Advisory','Description','Fix / Upgrade To'];
  h3.forEach((h, i) => {
    applyCell(ws3.getCell(2, i+1), h,
      { fill: COLORS.subhdr, bold: true, color: COLORS.white, size: 10, halign: 'center', valign: 'middle' });
  });
  ws3.getRow(2).height = 24;

  DEPS.forEach((dep, idx) => {
    const [pkg, ver, sev, cve, desc, fix] = dep;
    const r = idx + 3;
    const rowFill = idx % 2 === 0 ? COLORS.white : 'F8F9FA';

    [pkg, ver, sev, cve, desc, fix].forEach((val, ci) => {
      const cell = ws3.getCell(r, ci+1);
      if (ci === 2) {
        applyCell(cell, val, { fill: COLORS[val] || '95A5A6', bold: true, color: COLORS.white, size: 9, halign: 'center', valign: 'middle' });
      } else {
        applyCell(cell, val, { fill: rowFill, size: 9 });
      }
    });
    ws3.getRow(r).height = 28;
  });

  [32, 20, 12, 28, 65, 22].forEach((w, i) => ws3.getColumn(i+1).width = w);

  // ─── SHEET 4: Risk Summary ────────────────────────────────────
  const ws4 = wb.addWorksheet('Risk Summary');

  ws4.mergeCells('A1:D1');
  applyCell(ws4.getCell('A1'), '📊 Risk Summary Dashboard — CivicBin Security Assessment',
    { fill: COLORS.header, bold: true, color: COLORS.white, size: 13, halign: 'center', valign: 'middle' });
  ws4.getRow(1).height = 34;

  const counts = { CRITICAL: 0, HIGH: 0, MEDIUM: 0, LOW: 0 };
  FINDINGS.forEach(f => { counts[f[1]] = (counts[f[1]] || 0) + 1; });
  const total = Object.values(counts).reduce((a, b) => a + b, 0);
  const score = Math.max(0, 100 - counts.CRITICAL*15 - counts.HIGH*8 - counts.MEDIUM*3 - counts.LOW);
  const scoreColor = score < 40 ? COLORS.CRITICAL : score < 60 ? COLORS.HIGH : score < 80 ? COLORS.MEDIUM : COLORS.GOOD;

  ws4.mergeCells('A3:D3');
  applyCell(ws4.getCell('A3'), 'Severity Breakdown',
    { fill: COLORS.subhdr, bold: true, color: COLORS.white, size: 12, halign: 'center', valign: 'middle' });
  ws4.getRow(3).height = 26;

  [['🔴 CRITICAL', counts.CRITICAL, COLORS.CRITICAL],
   ['🟠 HIGH', counts.HIGH, COLORS.HIGH],
   ['🟡 MEDIUM', counts.MEDIUM, COLORS.MEDIUM],
   ['🔵 LOW', counts.LOW, COLORS.LOW]].forEach(([label, count, color], i) => {
    const r = 4 + i;
    ws4.mergeCells(`A${r}:B${r}`);
    applyCell(ws4.getCell(`A${r}`), label, { fill: color, bold: true, color: COLORS.white, size: 12, halign: 'center', valign: 'middle' });
    applyCell(ws4.getCell(r, 3), count, { fill: color, bold: true, color: COLORS.white, size: 16, halign: 'center', valign: 'middle' });
    const pct = total > 0 ? `${Math.round(count/total*100)}%` : '0%';
    applyCell(ws4.getCell(r, 4), pct, { bold: true, size: 11, halign: 'center', valign: 'middle' });
    ws4.getRow(r).height = 30;
  });

  ws4.mergeCells('A8:B8');
  applyCell(ws4.getCell('A8'), 'TOTAL FINDINGS', { fill: COLORS.subhdr, bold: true, color: COLORS.white, size: 12, halign: 'center', valign: 'middle' });
  applyCell(ws4.getCell('C8'), total, { bold: true, size: 16, halign: 'center', valign: 'middle' });
  ws4.getRow(8).height = 30;

  ws4.mergeCells('A10:B10');
  applyCell(ws4.getCell('A10'), 'Security Score', { fill: COLORS.subhdr, bold: true, color: COLORS.white, size: 13, halign: 'center', valign: 'middle' });
  applyCell(ws4.getCell('C10'), `${score} / 100`, { fill: scoreColor, bold: true, color: COLORS.white, size: 16, halign: 'center', valign: 'middle' });
  const grade = score < 40 ? '🔴 CRITICAL RISK' : score < 60 ? '🟠 HIGH RISK' : score < 80 ? '🟡 MEDIUM RISK' : '🟢 LOW RISK';
  applyCell(ws4.getCell('D10'), grade, { fill: scoreColor, bold: true, color: COLORS.white, size: 11, halign: 'center', valign: 'middle' });
  ws4.getRow(10).height = 36;

  ws4.mergeCells('A12:D12');
  applyCell(ws4.getCell('A12'), 'Security Category Assessment',
    { fill: COLORS.subhdr, bold: true, color: COLORS.white, size: 12, halign: 'center', valign: 'middle' });
  ws4.getRow(12).height = 26;

  [['Authentication', '🔴 CRITICAL', COLORS.CRITICAL],
   ['Authorization / IDOR', '🔴 CRITICAL', COLORS.CRITICAL],
   ['SQL Injection', '🔴 CRITICAL', COLORS.CRITICAL],
   ['Password Reset', '🔴 CRITICAL', COLORS.CRITICAL],
   ['Password Hashing', '🟢 GOOD', COLORS.GOOD],
   ['File Upload', '🟠 HIGH', COLORS.HIGH],
   ['CORS Policy', '🟠 HIGH', COLORS.HIGH],
   ['Secret Management', '🟠 HIGH', COLORS.HIGH],
   ['Security Headers', '🟠 HIGH', COLORS.HIGH],
   ['Rate Limiting', '🟠 HIGH', COLORS.HIGH],
   ['Error Handling', '🟡 MEDIUM', COLORS.MEDIUM],
   ['Input Validation', '🟡 MEDIUM', COLORS.MEDIUM],
   ['CSRF Protection', '🟡 MEDIUM', COLORS.MEDIUM],
   ['Dependency Security', '🟡 MEDIUM', COLORS.MEDIUM],
  ].forEach(([cat, risk, color], i) => {
    const r = 13 + i;
    ws4.mergeCells(`A${r}:C${r}`);
    applyCell(ws4.getCell(`A${r}`), cat, { bold: true, size: 10 });
    applyCell(ws4.getCell(r, 4), risk, { fill: color, bold: true, color: COLORS.white, size: 10, halign: 'center', valign: 'middle' });
    ws4.getRow(r).height = 22;
  });

  ws4.getColumn(1).width = 28;
  ws4.getColumn(2).width = 18;
  ws4.getColumn(3).width = 12;
  ws4.getColumn(4).width = 18;

  // Save
  const outDir = path.join(__dirname, '..', 'Vulnerability Test Results');
  if (!fs.existsSync(outDir)) fs.mkdirSync(outDir, { recursive: true });
  const outPath = path.join(outDir, 'findings.xlsx');
  await wb.xlsx.writeFile(outPath);

  console.log(`\n✅ Excel report generated: ${outPath}`);
  console.log(`   Sheets: Security Findings | Endpoint Inventory | Dependency Vulnerabilities | Risk Summary`);
  console.log(`   Findings: ${total} total (${counts.CRITICAL} Critical, ${counts.HIGH} High, ${counts.MEDIUM} Medium, ${counts.LOW} Low)`);
  console.log(`   Security Score: ${score}/100 — ${grade}`);
}

generate().catch(err => {
  console.error('Error generating report:', err);
  process.exit(1);
});
