const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_sd_123";
const BOB_UID = "bob_sd_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid, timestamp = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    displayName: "Budi Santoso",
    totalXp: 0,
    totalStars: 0,
    totalQuizzes: 0,
    totalQuestionsAnswered: 0,
    totalCorrectAnswers: 0,
    multiplicationCorrect: 0,
    divisionCorrect: 0,
    perfectQuizzes: 0,
    timedHighAccuracyQuizzes: 0,
    bestCombo: 0,
    currentStreakDays: 0,
    bestStreakDays: 0,
    lastPracticeDate: "",
    createdAt: timestamp,
    updatedAt: timestamp,
  };
}

function validSessionPayload(uid, timestamp = new Date(Date.now() - 1000)) {
  return {
    userId: uid,
    operationType: "MULTIPLICATION",
    gameMode: "PRACTICE",
    tableNumber: 7,
    totalQuestions: 10,
    correctAnswers: 9,
    starsEarned: 3,
    xpEarned: 110,
    maxCombo: 6,
    durationSeconds: 45,
    createdAt: timestamp,
  };
}

function validMasteryPayload(uid, timestamp = new Date(Date.now() - 1000)) {
  return {
    userId: uid,
    operationType: "MULTIPLICATION",
    tableNumber: 7,
    attempts: 10,
    correct: 9,
    stars: 3,
    createdAt: timestamp,
    updatedAt: timestamp,
  };
}

function validAchievementPayload(uid, badgeId = "langkah_pertama", timestamp = new Date(Date.now() - 1000)) {
  return {
    userId: uid,
    badgeId,
    xpReward: 50,
    unlockedAt: timestamp,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- 1. UNAUTHENTICATED DENIAL ---
test("Unauthenticated users are blocked from all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("quizSessions").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("tableMastery").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("achievements").get()
  );
});

// --- 2. USER PROFILE CRUD & SECURITY ---
test("Owner can create, read, rename, and record progress on UserProfile; rejects ghost/immutable tampering", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Create matches MathProgressRepository.ensureUserProfile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created))
  );

  // Read own profile succeeds; listing /users is denied
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(aliceDb.collection("users").get());

  // Action 1: Update displayName succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Siti Hebat",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Action 2: Record quiz progress succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      totalXp: 160,
      totalStars: 3,
      totalQuizzes: 1,
      totalQuestionsAnswered: 10,
      totalCorrectAnswers: 9,
      multiplicationCorrect: 9,
      divisionCorrect: 0,
      perfectQuizzes: 0,
      timedHighAccuracyQuizzes: 0,
      bestCombo: 6,
      currentStreakDays: 1,
      bestStreakDays: 1,
      lastPracticeDate: "2026-11-23",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field ("Shadow Update")
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Siti",
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject immutable createdAt tampering
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Siti",
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject future timestamp tampering
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Siti",
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Reject invalid displayName size (> 40 chars)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "A".repeat(45),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject @DocumentId "id" field on create
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      ...validProfilePayload(BOB_UID, created),
      id: BOB_UID,
    })
  );
});

// --- 3. CROSS-USER ISOLATION ---
test("Cross-user reads and writes are strictly denied across all paths", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(BOB_UID).set(validProfilePayload(BOB_UID));
    await db.collection("users").doc(BOB_UID).collection("quizSessions").doc("s1").set(validSessionPayload(BOB_UID));
    await db.collection("users").doc(BOB_UID).collection("tableMastery").doc("mul_7").set(validMasteryPayload(BOB_UID));
    await db.collection("users").doc(BOB_UID).collection("achievements").doc("langkah_pertama").set(validAchievementPayload(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).update({ displayName: "Hacked", updatedAt: new Date(Date.now() - 1000) }));
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("quizSessions").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("tableMastery").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("achievements").get());
});

// --- 4. QUIZ SESSIONS SUBCOLLECTION ---
test("Owner can create and list quizSessions ordered by createdAt; updates are forbidden", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const sessionsCol = aliceDb.collection("users").doc(ALICE_UID).collection("quizSessions");

  await assertSucceeds(sessionsCol.doc("session_1").set(validSessionPayload(ALICE_UID)));

  // Matches Kotlin query: orderBy("createdAt", DESCENDING).limit(25)
  await assertSucceeds(sessionsCol.orderBy("createdAt", "desc").limit(25).get());

  // Immutable: update must fail
  await assertFails(sessionsCol.doc("session_1").update({ correctAnswers: 10 }));

  // Invalid bounds: correctAnswers > totalQuestions must fail
  await assertFails(
    sessionsCol.doc("session_bad").set({
      ...validSessionPayload(ALICE_UID),
      correctAnswers: 15,
      totalQuestions: 10,
    })
  );
});

// --- 5. TABLE MASTERY SUBCOLLECTION ---
test("Owner can create, update, and list tableMastery; immutable fields cannot change", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const masteryCol = aliceDb.collection("users").doc(ALICE_UID).collection("tableMastery");
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(masteryCol.doc("mul_7").set(validMasteryPayload(ALICE_UID, created)));
  await assertSucceeds(masteryCol.get());

  // Partial update matches MathProgressRepository
  await assertSucceeds(
    masteryCol.doc("mul_7").update({
      attempts: 15,
      correct: 14,
      stars: 3,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject mutating tableNumber
  await assertFails(
    masteryCol.doc("mul_7").update({
      tableNumber: 8,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject out-of-range tableNumber
  await assertFails(
    masteryCol.doc("mul_13").set({
      ...validMasteryPayload(ALICE_UID, created),
      tableNumber: 13,
    })
  );
});

// --- 6. ACHIEVEMENTS SUBCOLLECTION ---
test("Owner can unlock and list achievements; docId must match badgeId; updates forbidden", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const achCol = aliceDb.collection("users").doc(ALICE_UID).collection("achievements");

  await assertSucceeds(
    achCol.doc("langkah_pertama").set(validAchievementPayload(ALICE_UID, "langkah_pertama"))
  );
  await assertSucceeds(achCol.get());

  // Mismatched docId vs badgeId must fail
  await assertFails(
    achCol.doc("wrong_id").set(validAchievementPayload(ALICE_UID, "langkah_pertama"))
  );

  // Immutable: update must fail
  await assertFails(achCol.doc("langkah_pertama").update({ xpReward: 999 }));
});
