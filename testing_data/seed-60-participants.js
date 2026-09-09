#!/usr/bin/env node
/**
 * Reproduces the Task 2 scenario (docs/task02_workflow.md) against a real,
 * running backend: a 40-seat programme receives 60 valid nominations, so
 * the first 40 (in arrival order) should confirm and the rest waitlist —
 * then cancelling one confirmed nomination should promote the next person
 * on the waiting list.
 *
 * Unlike NominationCapacityScenarioTest (backend/src/test/...), which rolls
 * its data back at the end, this script writes real, persisted rows through
 * the public REST API so the result is visible in the running app (the "All
 * Participants" page) and the database — useful for demoing/validating the
 * feature against a real deployment rather than just the test suite.
 *
 * Usage:
 *   node seed-60-participants.js                  # seed + cancel-and-promote demo
 *   node seed-60-participants.js --no-cancel-demo  # seed only, skip the cancel step
 *   API_BASE_URL=http://localhost:8080/api node seed-60-participants.js
 */

const fs = require('fs');
const path = require('path');

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080/api';
const MAX_PARTICIPANTS = 40;
const PROGRAMME_TITLE = 'Cybersecurity Awareness Programme';
const RUN_CANCEL_DEMO = !process.argv.includes('--no-cancel-demo');

const participants = JSON.parse(
  fs.readFileSync(path.join(__dirname, 'participants.json'), 'utf8')
);

async function apiGet(pathname) {
  const res = await fetch(`${API_BASE_URL}${pathname}`);
  if (!res.ok) {
    throw new Error(`GET ${pathname} failed: ${res.status} ${await res.text()}`);
  }
  return res.json();
}

async function apiPost(pathname, body) {
  const res = await fetch(`${API_BASE_URL}${pathname}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    throw new Error(`POST ${pathname} failed: ${res.status} ${JSON.stringify(data)}`);
  }
  return data;
}

async function apiDelete(pathname) {
  const res = await fetch(`${API_BASE_URL}${pathname}`, { method: 'DELETE' });
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    throw new Error(`DELETE ${pathname} failed: ${res.status} ${JSON.stringify(data)}`);
  }
  return data;
}

async function findOrCreateProgramme() {
  const programmes = await apiGet('/programmes');
  const existing = programmes.find((p) => p.title === PROGRAMME_TITLE);
  if (existing) {
    console.log(`Reusing existing programme "${PROGRAMME_TITLE}" (id ${existing.id}).`);
    return existing;
  }

  const trainingDate = new Date();
  trainingDate.setDate(trainingDate.getDate() + 30);

  const created = await apiPost('/programmes', {
    title: PROGRAMME_TITLE,
    trainingDate: trainingDate.toISOString().slice(0, 10),
    venue: 'Main Auditorium',
    trainer: 'Dr. C. Amarasinghe',
    maxParticipants: MAX_PARTICIPANTS,
  });
  console.log(`Created programme "${PROGRAMME_TITLE}" (id ${created.id}, max ${MAX_PARTICIPANTS}).`);
  return created;
}

async function findOrCreateOfficer(participant, departmentsByName) {
  const department = departmentsByName.get(participant.department);
  if (!department) {
    throw new Error(`Unknown department "${participant.department}" for ${participant.employeeNo}`);
  }

  const existing = await apiGet(`/officers?departmentId=${department.id}`);
  const match = existing.find((o) => o.employeeNo === participant.employeeNo);
  if (match) {
    return match;
  }

  return apiPost('/officers', {
    employeeNo: participant.employeeNo,
    name: participant.name,
    departmentId: department.id,
  });
}

async function main() {
  console.log(`API base URL: ${API_BASE_URL}`);
  console.log(`Seeding ${participants.length} participants against a ${MAX_PARTICIPANTS}-seat programme.\n`);

  const departments = await apiGet('/departments');
  const departmentsByName = new Map(departments.map((d) => [d.name, d]));

  const programme = await findOrCreateProgramme();

  const nominations = [];
  for (const participant of participants) {
    const officer = await findOrCreateOfficer(participant, departmentsByName);
    const nomination = await apiPost('/nominations', {
      programmeId: programme.id,
      officerId: officer.id,
      departmentId: officer.departmentId,
      submittedBy: 'testing_data/seed-60-participants.js',
    });
    nominations.push(nomination);
    process.stdout.write(
      `  [${String(nominations.length).padStart(2, '0')}/${participants.length}] ` +
        `${officer.employeeNo} ${officer.name} -> ${nomination.status}\n`
    );
  }

  const confirmed = nominations.filter((n) => n.status === 'CONFIRMED');
  const waitlisted = nominations.filter((n) => n.status === 'WAITLISTED');

  console.log('\n--- Result ---');
  console.log(`Confirmed:  ${confirmed.length}`);
  console.log(`Waitlisted: ${waitlisted.length}`);

  if (!RUN_CANCEL_DEMO) {
    console.log('\n(--no-cancel-demo passed; skipping the cancel-and-promote step.)');
    return;
  }

  if (confirmed.length === 0 || waitlisted.length === 0) {
    console.log('\nSkipping cancel-and-promote demo (need at least one confirmed and one waitlisted nomination).');
    return;
  }

  const toCancel = confirmed[0];
  const nextInLine = waitlisted[0];
  console.log(
    `\nCancelling confirmed nomination #${toCancel.id} (${toCancel.officerName})...`
  );
  await apiDelete(`/nominations/${toCancel.id}`);

  const after = await apiGet(`/nominations?programmeId=${programme.id}`);
  const promoted = after.find((n) => n.id === nextInLine.id);

  console.log(
    `Oldest waitlisted nomination #${nextInLine.id} (${nextInLine.officerName}) is now: ${promoted.status}`
  );
  console.log(
    promoted.status === 'CONFIRMED'
      ? 'Promotion worked as expected.'
      : 'UNEXPECTED: promotion did not happen — check the backend.'
  );

  const finalConfirmed = after.filter((n) => n.status === 'CONFIRMED').length;
  const finalWaitlisted = after.filter((n) => n.status === 'WAITLISTED').length;
  console.log(`\nFinal tally for programme ${programme.id}: ${finalConfirmed} confirmed, ${finalWaitlisted} waitlisted.`);
}

main().catch((err) => {
  console.error('\nSeed script failed:', err.message);
  process.exit(1);
});
