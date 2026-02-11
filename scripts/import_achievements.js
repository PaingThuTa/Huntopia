const admin = require("firebase-admin");
const fs = require("fs");
const path = require("path");

const serviceKeyPath = path.join(__dirname, "serviceAccountKey.json");
const dataPath = path.join(__dirname, "achievements.json");

admin.initializeApp({
  credential: admin.credential.cert(require(serviceKeyPath)),
});

const db = admin.firestore();

async function run() {
  const raw = fs.readFileSync(dataPath, "utf8");
  const items = JSON.parse(raw);

  if (!Array.isArray(items)) {
    throw new Error("achievements.json must be an array of objects");
  }

  const batch = db.batch();
  let count = 0;

  for (const item of items) {
    const code = String(item.url || "").trim();
    if (!code) {
      console.warn("Skipping item with missing url/code:", item);
      continue;
    }

    const docRef = db.collection("achievements").doc(code);

    // Only store catalog fields (not user progress)
    batch.set(
      docRef,
      {
        code,
        imageName: item.imageName ?? "",
        unfoundTitle: item.unfoundTitle ?? "",
        unfoundDescription: item.unfoundDescription ?? "",
        foundTitle: item.foundTitle ?? "",
        foundDescription: item.foundDescription ?? "",
      },
      { merge: true } // safe to re-run without wiping extra fields
    );

    count++;
  }

  await batch.commit();
  console.log(`✅ Imported/updated ${count} achievements into /achievements`);
}

run().catch((err) => {
  console.error("❌ Import failed:", err);
  process.exit(1);
});
