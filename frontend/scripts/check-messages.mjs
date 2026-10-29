// Both languages ship complete: a key present in one file and missing from the other fails
// the check, so an untranslated string never reaches a release.
import { readFileSync } from "node:fs";

const load = (locale) => JSON.parse(readFileSync(new URL(`../messages/${locale}.json`, import.meta.url)));

const keys = (node, prefix = "") =>
  Object.entries(node).flatMap(([key, value]) =>
    typeof value === "object" && value !== null ? keys(value, `${prefix}${key}.`) : [`${prefix}${key}`],
  );

const [fr, en] = [new Set(keys(load("fr"))), new Set(keys(load("en")))];
const missing = [
  ...[...en].filter((key) => !fr.has(key)).map((key) => `fr.json is missing ${key}`),
  ...[...fr].filter((key) => !en.has(key)).map((key) => `en.json is missing ${key}`),
];

if (missing.length > 0) {
  console.error(missing.join("\n"));
  process.exit(1);
}
console.log(`messages: ${en.size} keys, fr and en complete`);
